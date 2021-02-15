package qub;

public interface HttpClientTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(HttpClient.class, () ->
        {
            runner.testGroup("create(Network)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> HttpClient.create((Network)null),
                        new PreConditionFailure("network cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final ManualClock clock = ManualClock.create();
                    final FakeNetwork network = new FakeNetwork(clock);
                    final HttpClient httpClient = HttpClient.create(network);
                    test.assertNotNull(httpClient);
                    test.assertInstanceOf(httpClient, JavaHttpClient.class);
                });
            });
        });
    }

    static void test(TestRunner runner, Function1<Network,HttpClient> creator)
    {
        runner.testGroup(HttpClient.class, () ->
        {
            runner.testGroup("send(HttpRequest)", () ->
            {
                final Action3<String,HttpRequest,Throwable> sendErrorTest = (String testName, HttpRequest request, Throwable expected) ->
                {
                    runner.test(testName, (Test test) ->
                    {
                        final HttpClient httpClient = creator.run(test.getNetwork());
                        test.assertThrows(() -> httpClient.send(request).await(), expected);
                    });
                };

                sendErrorTest.run(
                    "with null",
                    null,
                    new PreConditionFailure("request cannot be null."));
                sendErrorTest.run(
                    "with unknown host",
                    HttpRequest.create()
                        .setMethod(HttpMethod.GET)
                        .setUrl(URL.parse("http://www.idontexistbecauseimnotagoodurl.com").await()),
                    new HostNotFoundException("www.idontexistbecauseimnotagoodurl.com"));

                final Action1<Integer> sendStatusCodeTest = (Integer responseStatusCode) ->
                {
                    runner.test("with " + responseStatusCode + " status code", (Test test) ->
                    {
                        final Network network = test.getNetwork();
                        final AsyncRunner asyncRunner = test.getParallelAsyncRunner();

                        final IPv4Address serverAddress = IPv4Address.localhost;
                        final int serverPort = 80;

                        final TCPServer tcpServer = network.createTCPServer(serverAddress, serverPort).await();
                        try (final HttpServer result = HttpServer.create(tcpServer, asyncRunner))
                        {
                            result.setNotFound((HttpRequest request) ->
                            {
                                final String reasonPhrase = HttpServer.getReasonPhrase(responseStatusCode);
                                return HttpResponse.create()
                                    .setHttpVersion(request.getHttpVersion())
                                    .setStatusCode(responseStatusCode)
                                    .setReasonPhrase(reasonPhrase)
                                    .setBody(responseStatusCode + ": " + reasonPhrase);
                            });
                            result.start();

                            final HttpClient httpClient = creator.run(network);

                            final HttpRequest httpRequest = HttpRequest.create()
                                .setMethod(HttpMethod.GET)
                                .setUrl(URL.create().setScheme("http").setHost(serverAddress.toString()).setPort(serverPort).setPath("/unrecognized/path"));
                            try (final HttpResponse httpResponse = httpClient.send(httpRequest).await())
                            {
                                test.assertEqual("HTTP/1.1", httpResponse.getHttpVersion());
                                test.assertEqual(responseStatusCode, httpResponse.getStatusCode());
                                test.assertEqual(HttpServer.getReasonPhrase(responseStatusCode), httpResponse.getReasonPhrase());
                                test.assertEqual(responseStatusCode + ": " + HttpServer.getReasonPhrase(responseStatusCode), CharacterReadStream.create(httpResponse.getBody()).readEntireString().await());
                            }
                        }
                    });
                };

                sendStatusCodeTest.run(400);
                sendStatusCodeTest.run(404);
                sendStatusCodeTest.run(500);

                final Action2<HttpRequest,Action2<Test,HttpResponse>> sendTest = (HttpRequest request, Action2<Test,HttpResponse> validation) ->
                {
                    final CharacterList testName = CharacterList.create();
                    testName.addAll("with ")
                            .addAll(request.getMethod())
                            .addAll(" request to ")
                            .addAll(request.getURL().toString());

                    final HttpHeaders headers = request.getHeaders();
                    if (!Iterable.isNullOrEmpty(headers))
                    {
                        testName.addAll(" with headers ")
                                .addAll(headers.toString());
                    }

                    final ByteReadStream body = request.getBody();
                    if (body != null)
                    {
                        testName.addAll(" and non-null body");
                    }

                    runner.test(testName.toString(), (Test test) ->
                    {
                        final Network network = test.getNetwork();
                        try (final HttpServer httpServer = HttpClientTests.createHttpServer(request.getURL(), test))
                        {
                            final HttpClient httpClient = creator.run(network);
                            try (final HttpResponse httpResponse = httpClient.send(request).await())
                            {
                                test.assertEqual("HTTP/1.1", httpResponse.getHttpVersion());
                                test.assertEqual(200, httpResponse.getStatusCode());
                                test.assertEqual("OK", httpResponse.getReasonPhrase());

                                validation.run(test, httpResponse);
                            }
                        }
                    });
                };

                sendTest.run(
                    HttpRequest.create()
                        .setMethod(HttpMethod.HEAD)
                        .setUrl(HttpClientTests.createHttpServerUrl()),
                    (Test test, HttpResponse httpResponse) ->
                    {
                        test.assertEqual("HEAD", httpResponse.getHeaderValue("request-method").await());
                        final URL requestUrl = URL.parse(httpResponse.getHeaderValue("request-url").await()).await();
                        test.assertEqual(IPv4Address.localhost.toString(), requestUrl.getHost());
                        test.assertEqual(null, requestUrl.getPort());

                        test.assertEqual(new byte[0], httpResponse.getBody().readAllBytes().await());
                    });

                sendTest.run(
                    HttpRequest.create()
                        .setMethod(HttpMethod.HEAD)
                        .setUrl(HttpClientTests.createHttpServerUrl("localhost")),
                    (Test test, HttpResponse httpResponse) ->
                    {
                        test.assertEqual("HEAD", httpResponse.getHeaderValue("request-method").await());
                        final URL requestUrl = URL.parse(httpResponse.getHeaderValue("request-url").await()).await();
                        test.assertEqual(IPv4Address.localhost.toString(), requestUrl.getHost());
                        test.assertEqual(null, requestUrl.getPort());

                        test.assertEqual(new byte[0], httpResponse.getBody().readAllBytes().await());
                    });

                sendTest.run(
                    HttpRequest.create()
                        .setMethod(HttpMethod.HEAD)
                        .setUrl(HttpClientTests.createHttpServerUrl(8080)),
                    (Test test, HttpResponse httpResponse) ->
                    {
                        test.assertEqual("HEAD", httpResponse.getHeaderValue("request-method").await());
                        final URL requestUrl = URL.parse(httpResponse.getHeaderValue("request-url").await()).await();
                        test.assertEqual(IPv4Address.localhost.toString(), requestUrl.getHost());
                        test.assertEqual(8080, requestUrl.getPort());

                        test.assertEqual(new byte[0], httpResponse.getBody().readAllBytes().await());
                    });

                sendTest.run(
                    HttpRequest.create()
                        .setMethod(HttpMethod.HEAD)
                        .setUrl(HttpClientTests.createHttpServerUrl())
                        .setHeader("a", "b"),
                    (Test test, HttpResponse httpResponse) ->
                    {
                        test.assertEqual("HEAD", httpResponse.getHeaderValue("request-method").await());
                        final URL requestUrl = URL.parse(httpResponse.getHeaderValue("request-url").await()).await();
                        test.assertEqual(IPv4Address.localhost.toString(), requestUrl.getHost());
                        test.assertEqual(null, requestUrl.getPort());
                        test.assertEqual("b", httpResponse.getHeaderValue("request-header-a").await());

                        test.assertEqual(new byte[0], httpResponse.getBody().readAllBytes().await());
                    });

                sendTest.run(
                    HttpRequest.create()
                        .setMethod(HttpMethod.GET)
                        .setUrl(HttpClientTests.createHttpServerUrl()),
                    (Test test, HttpResponse httpResponse) ->
                    {
                        test.assertEqual("GET", httpResponse.getHeaderValue("request-method").await());
                        final URL headerRequestUrl = URL.parse(httpResponse.getHeaderValue("request-url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), headerRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), headerRequestUrl.getHost());
                        test.assertEqual(null, headerRequestUrl.getPort());

                        final JSONObject bodyJson = JSON.parseObject(httpResponse.getBody()).await();
                        test.assertEqual("GET", bodyJson.getString("method").await());
                        final URL bodyRequestUrl = URL.parse(bodyJson.getString("url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), bodyRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), bodyRequestUrl.getHost());
                        test.assertEqual(null, bodyRequestUrl.getPort());
                        final JSONObject headersJson = bodyJson.getObject("headers").await();
                        test.assertNotNull(headersJson);
                        test.assertNull(bodyJson.getNull("body").await());
                    });

                sendTest.run(
                    HttpRequest.create()
                        .setMethod(HttpMethod.GET)
                        .setUrl(HttpClientTests.createHttpServerUrl())
                        .setHeader("a", "b"),
                    (Test test, HttpResponse httpResponse) ->
                    {
                        test.assertEqual("GET", httpResponse.getHeaderValue("request-method").await());
                        final URL headerRequestUrl = URL.parse(httpResponse.getHeaderValue("request-url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), headerRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), headerRequestUrl.getHost());
                        test.assertEqual(null, headerRequestUrl.getPort());
                        test.assertEqual("b", httpResponse.getHeaderValue("request-header-a").await());

                        final JSONObject bodyJson = JSON.parseObject(httpResponse.getBody()).await();
                        test.assertEqual("GET", bodyJson.getString("method").await());
                        final URL bodyRequestUrl = URL.parse(bodyJson.getString("url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), bodyRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), bodyRequestUrl.getHost());
                        test.assertEqual(null, bodyRequestUrl.getPort());
                        final JSONObject headersJson = bodyJson.getObject("headers").await();
                        test.assertNotNull(headersJson);
                        test.assertEqual("b", headersJson.getString("a").await());
                        test.assertNull(bodyJson.getNull("body").await());
                    });

                sendTest.run(
                    HttpRequest.create()
                        .setMethod(HttpMethod.POST)
                        .setUrl(HttpClientTests.createHttpServerUrl()),
                    (Test test, HttpResponse httpResponse) ->
                    {
                        test.assertEqual("POST", httpResponse.getHeaderValue("request-method").await());
                        final URL headerRequestUrl = URL.parse(httpResponse.getHeaderValue("request-url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), headerRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), headerRequestUrl.getHost());
                        test.assertEqual(null, headerRequestUrl.getPort());

                        final JSONObject bodyJson = JSON.parseObject(httpResponse.getBody()).await();
                        test.assertEqual("POST", bodyJson.getString("method").await());
                        final URL bodyRequestUrl = URL.parse(bodyJson.getString("url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), bodyRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), bodyRequestUrl.getHost());
                        test.assertEqual(null, bodyRequestUrl.getPort());
                        final JSONObject headersJson = bodyJson.getObject("headers").await();
                        test.assertNotNull(headersJson);
                        test.assertNull(bodyJson.getNull("body").await());
                    });

                sendTest.run(
                    HttpRequest.create()
                        .setMethod(HttpMethod.POST)
                        .setUrl(HttpClientTests.createHttpServerUrl())
                        .setHeader("a", "b"),
                    (Test test, HttpResponse httpResponse) ->
                    {
                        test.assertEqual("POST", httpResponse.getHeaderValue("request-method").await());
                        final URL headerRequestUrl = URL.parse(httpResponse.getHeaderValue("request-url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), headerRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), headerRequestUrl.getHost());
                        test.assertEqual(null, headerRequestUrl.getPort());
                        test.assertEqual("b", httpResponse.getHeaderValue("request-header-a").await());

                        final JSONObject bodyJson = JSON.parseObject(httpResponse.getBody()).await();
                        test.assertEqual("POST", bodyJson.getString("method").await());
                        final URL bodyRequestUrl = URL.parse(bodyJson.getString("url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), bodyRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), bodyRequestUrl.getHost());
                        test.assertEqual(null, bodyRequestUrl.getPort());
                        final JSONObject headersJson = bodyJson.getObject("headers").await();
                        test.assertNotNull(headersJson);
                        test.assertEqual("b", headersJson.getString("a").await());
                        test.assertNull(bodyJson.getNull("body").await());
                    });

                sendTest.run(
                    HttpRequest.create()
                        .setMethod(HttpMethod.POST)
                        .setUrl(HttpClientTests.createHttpServerUrl())
                        .setBody(new byte[] { 1, 2, 3 }),
                    (Test test, HttpResponse httpResponse) ->
                    {
                        test.assertEqual("POST", httpResponse.getHeaderValue("request-method").await());
                        final URL headerRequestUrl = URL.parse(httpResponse.getHeaderValue("request-url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), headerRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), headerRequestUrl.getHost());
                        test.assertEqual(null, headerRequestUrl.getPort());

                        final JSONObject bodyJson = JSON.parseObject(httpResponse.getBody()).await();
                        test.assertEqual("POST", bodyJson.getString("method").await());
                        final URL bodyRequestUrl = URL.parse(bodyJson.getString("url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), bodyRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), bodyRequestUrl.getHost());
                        test.assertEqual(null, bodyRequestUrl.getPort());
                        final JSONObject headersJson = bodyJson.getObject("headers").await();
                        test.assertNotNull(headersJson);
                        test.assertEqual(
                            Iterable.create(1, 2, 3),
                            bodyJson.getArray("body").await()
                                .map((JSONSegment segment) -> ((JSONNumber)segment).getIntegerValue().await()));
                    });

                sendTest.run(
                    HttpRequest.create()
                        .setMethod(HttpMethod.PUT)
                        .setUrl(HttpClientTests.createHttpServerUrl()),
                    (Test test, HttpResponse httpResponse) ->
                    {
                        test.assertEqual("PUT", httpResponse.getHeaderValue("request-method").await());
                        final URL headerRequestUrl = URL.parse(httpResponse.getHeaderValue("request-url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), headerRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), headerRequestUrl.getHost());
                        test.assertEqual(null, headerRequestUrl.getPort());

                        final JSONObject bodyJson = JSON.parseObject(httpResponse.getBody()).await();
                        test.assertEqual("PUT", bodyJson.getString("method").await());
                        final URL bodyRequestUrl = URL.parse(bodyJson.getString("url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), bodyRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), bodyRequestUrl.getHost());
                        test.assertEqual(null, bodyRequestUrl.getPort());
                        final JSONObject headersJson = bodyJson.getObject("headers").await();
                        test.assertNotNull(headersJson);
                        test.assertNull(bodyJson.getNull("body").await());
                    });

                sendTest.run(
                    HttpRequest.create()
                        .setMethod(HttpMethod.PUT)
                        .setUrl(HttpClientTests.createHttpServerUrl())
                        .setHeader("a", "b"),
                    (Test test, HttpResponse httpResponse) ->
                    {
                        test.assertEqual("PUT", httpResponse.getHeaderValue("request-method").await());
                        final URL headerRequestUrl = URL.parse(httpResponse.getHeaderValue("request-url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), headerRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), headerRequestUrl.getHost());
                        test.assertEqual(null, headerRequestUrl.getPort());
                        test.assertEqual("b", httpResponse.getHeaderValue("request-header-a").await());

                        final JSONObject bodyJson = JSON.parseObject(httpResponse.getBody()).await();
                        test.assertEqual("PUT", bodyJson.getString("method").await());
                        final URL bodyRequestUrl = URL.parse(bodyJson.getString("url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), bodyRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), bodyRequestUrl.getHost());
                        test.assertEqual(null, bodyRequestUrl.getPort());
                        final JSONObject headersJson = bodyJson.getObject("headers").await();
                        test.assertNotNull(headersJson);
                        test.assertEqual("b", headersJson.getString("a").await());
                        test.assertNull(bodyJson.getNull("body").await());
                    });

                sendTest.run(
                    HttpRequest.create()
                        .setMethod(HttpMethod.PUT)
                        .setUrl(HttpClientTests.createHttpServerUrl())
                        .setBody(new byte[] { 1, 2, 3 }),
                    (Test test, HttpResponse httpResponse) ->
                    {
                        test.assertEqual("PUT", httpResponse.getHeaderValue("request-method").await());
                        final URL headerRequestUrl = URL.parse(httpResponse.getHeaderValue("request-url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), headerRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), headerRequestUrl.getHost());
                        test.assertEqual(null, headerRequestUrl.getPort());

                        final JSONObject bodyJson = JSON.parseObject(httpResponse.getBody()).await();
                        test.assertEqual("PUT", bodyJson.getString("method").await());
                        final URL bodyRequestUrl = URL.parse(bodyJson.getString("url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), bodyRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), bodyRequestUrl.getHost());
                        test.assertEqual(null, bodyRequestUrl.getPort());
                        final JSONObject headersJson = bodyJson.getObject("headers").await();
                        test.assertNotNull(headersJson);
                        test.assertEqual(
                            Iterable.create(1, 2, 3),
                            bodyJson.getArray("body").await()
                                .map((JSONSegment segment) -> ((JSONNumber)segment).getIntegerValue().await()));
                    });

                sendTest.run(
                    HttpRequest.create()
                        .setMethod(HttpMethod.DELETE)
                        .setUrl(HttpClientTests.createHttpServerUrl()),
                    (Test test, HttpResponse httpResponse) ->
                    {
                        test.assertEqual("DELETE", httpResponse.getHeaderValue("request-method").await());
                        final URL headerRequestUrl = URL.parse(httpResponse.getHeaderValue("request-url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), headerRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), headerRequestUrl.getHost());
                        test.assertEqual(null, headerRequestUrl.getPort());

                        final JSONObject bodyJson = JSON.parseObject(httpResponse.getBody()).await();
                        test.assertEqual("DELETE", bodyJson.getString("method").await());
                        final URL bodyRequestUrl = URL.parse(bodyJson.getString("url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), bodyRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), bodyRequestUrl.getHost());
                        test.assertEqual(null, bodyRequestUrl.getPort());
                        final JSONObject headersJson = bodyJson.getObject("headers").await();
                        test.assertNotNull(headersJson);
                        test.assertNull(bodyJson.getNull("body").await());
                    });

                sendTest.run(
                    HttpRequest.create()
                        .setMethod(HttpMethod.DELETE)
                        .setUrl(HttpClientTests.createHttpServerUrl())
                        .setHeader("a", "b"),
                    (Test test, HttpResponse httpResponse) ->
                    {
                        test.assertEqual("DELETE", httpResponse.getHeaderValue("request-method").await());
                        final URL headerRequestUrl = URL.parse(httpResponse.getHeaderValue("request-url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), headerRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), headerRequestUrl.getHost());
                        test.assertEqual(null, headerRequestUrl.getPort());
                        test.assertEqual("b", httpResponse.getHeaderValue("request-header-a").await());

                        final JSONObject bodyJson = JSON.parseObject(httpResponse.getBody()).await();
                        test.assertEqual("DELETE", bodyJson.getString("method").await());
                        final URL bodyRequestUrl = URL.parse(bodyJson.getString("url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), bodyRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), bodyRequestUrl.getHost());
                        test.assertEqual(null, bodyRequestUrl.getPort());
                        final JSONObject headersJson = bodyJson.getObject("headers").await();
                        test.assertNotNull(headersJson);
                        test.assertEqual("b", headersJson.getString("a").await());
                        test.assertNull(bodyJson.getNull("body").await());
                    });

                sendTest.run(
                    HttpRequest.create()
                        .setMethod(HttpMethod.DELETE)
                        .setUrl(HttpClientTests.createHttpServerUrl())
                        .setBody(new byte[] { 1, 2, 3 }),
                    (Test test, HttpResponse httpResponse) ->
                    {
                        test.assertEqual("DELETE", httpResponse.getHeaderValue("request-method").await());
                        final URL headerRequestUrl = URL.parse(httpResponse.getHeaderValue("request-url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), headerRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), headerRequestUrl.getHost());
                        test.assertEqual(null, headerRequestUrl.getPort());

                        final JSONObject bodyJson = JSON.parseObject(httpResponse.getBody()).await();
                        test.assertEqual("DELETE", bodyJson.getString("method").await());
                        final URL bodyRequestUrl = URL.parse(bodyJson.getString("url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), bodyRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), bodyRequestUrl.getHost());
                        test.assertEqual(null, bodyRequestUrl.getPort());
                        final JSONObject headersJson = bodyJson.getObject("headers").await();
                        test.assertNotNull(headersJson);
                        test.assertEqual(
                            Iterable.create(1, 2, 3),
                            bodyJson.getArray("body").await()
                                .map((JSONSegment segment) -> ((JSONNumber)segment).getIntegerValue().await()));
                    });
            });

            runner.testGroup("get(HttpRequest)", () ->
            {
                final Action2<URL,Throwable> getErrorTest = (URL url, Throwable expected) ->
                {
                    runner.test("with " + url, (Test test) ->
                    {
                        final HttpClient httpClient = creator.run(test.getNetwork());
                        test.assertThrows(() -> httpClient.get(url).await(), expected);
                    });
                };

                getErrorTest.run(
                    null,
                    new PreConditionFailure("url cannot be null."));
                getErrorTest.run(
                    URL.parse("http://www.idontexistbecauseimnotagoodurl.com").await(),
                    new HostNotFoundException("www.idontexistbecauseimnotagoodurl.com"));

                final Action2<URL,Action2<Test,HttpResponse>> getTest = (URL url, Action2<Test,HttpResponse> validation) ->
                {
                    runner.test("with " + url, (Test test) ->
                    {
                        final Network network = test.getNetwork();
                        try (final HttpServer httpServer = HttpClientTests.createHttpServer(url, test))
                        {
                            final HttpClient httpClient = creator.run(network);
                            try (final HttpResponse httpResponse = httpClient.get(url).await())
                            {
                                test.assertEqual("HTTP/1.1", httpResponse.getHttpVersion());
                                test.assertEqual(200, httpResponse.getStatusCode());
                                test.assertEqual("OK", httpResponse.getReasonPhrase());

                                validation.run(test, httpResponse);
                            }
                        }
                    });
                };

                getTest.run(
                    HttpClientTests.createHttpServerUrl(),
                    (Test test, HttpResponse httpResponse) ->
                    {
                        test.assertEqual("GET", httpResponse.getHeaderValue("request-method").await());
                        final URL headerRequestUrl = URL.parse(httpResponse.getHeaderValue("request-url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), headerRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), headerRequestUrl.getHost());
                        test.assertEqual(null, headerRequestUrl.getPort());

                        final JSONObject bodyJson = JSON.parseObject(httpResponse.getBody()).await();
                        test.assertEqual("GET", bodyJson.getString("method").await());
                        final URL bodyRequestUrl = URL.parse(bodyJson.getString("url").await()).await();
                        test.assertOneOf(Iterable.create("http", "https"), bodyRequestUrl.getScheme());
                        test.assertEqual(IPv4Address.localhost.toString(), bodyRequestUrl.getHost());
                        test.assertEqual(null, bodyRequestUrl.getPort());
                        final JSONObject headersJson = bodyJson.getObject("headers").await();
                        test.assertNotNull(headersJson);
                        test.assertNull(bodyJson.getNull("body").await());
                    });
            });
        });
    }

    static HttpServer createHttpServer(URL requestUrl, Test test)
    {
        PreCondition.assertNotNull(requestUrl, "requestUrl");
        PreCondition.assertNotNull(test, "test");

        final Network network = test.getNetwork();
        final AsyncRunner asyncRunner = test.getParallelAsyncRunner();

        final DNS dns = DNS.create();
        final IPv4Address serverAddress = dns.resolveHost(requestUrl.getHost()).await();
        final Integer requestPort = requestUrl.getPort();
        final int serverPort = requestPort != null ? requestPort : 80;

        final TCPServer tcpServer = network.createTCPServer(serverAddress, serverPort).await();
        final HttpServer result = HttpServer.create(tcpServer, asyncRunner)
            .setPath("**", (HttpRequest request) ->
            {
                final MutableHttpResponse httpResponse = MutableHttpResponse.create()
                    .setStatusCode(200)
                    .setReasonPhrase("OK")
                    .setHeader("request-method", request.getMethod())
                    .setHeader("request-url", request.getURL().toString());

                for (final HttpHeader requestHeader : request.getHeaders())
                {
                    httpResponse.setHeader("request-header-" + requestHeader.getName(), requestHeader.getValue());
                }

                if (!Comparer.equalIgnoreCase("head", request.getMethod()))
                {
                    httpResponse.setBody(JSONObject.create()
                        .setString("method", request.getMethod())
                        .setString("url", request.getURL().toString())
                        .setObject("headers", JSONObject.create()
                            .setAll(request.getHeaders().map((HttpHeader header) -> JSONProperty.create(header.getName(), header.getValue()))))
                        .setArrayOrNull("body", request.getBody() == null
                            ? null :
                            JSONArray.create(
                                ByteArray.create(request.getBody().readAllBytes().await()).map(JSONNumber::create)))
                        .toString());
                }

                return httpResponse;
            });
        result.start();

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    static MutableURL createHttpServerUrl()
    {
        return HttpClientTests.createHttpServerUrl(IPv4Address.localhost.toString());
    }

    static MutableURL createHttpServerUrl(String host)
    {
        return HttpClientTests.createHttpServerUrl(host, null);
    }

    static MutableURL createHttpServerUrl(Integer serverPort)
    {
        return HttpClientTests.createHttpServerUrl(IPv4Address.localhost.toString(), serverPort);
    }

    static MutableURL createHttpServerUrl(String host, Integer serverPort)
    {
        return MutableURL.create()
            .setScheme("http")
            .setHost(host)
            .setPort(serverPort);
    }
}
