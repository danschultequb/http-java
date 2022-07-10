package qub;

public interface HttpServerTests
{
    public static final IPv4Address serverAddress = IPv4Address.localhost;
    public static final int serverPort = 18034;

    public static void test(TestRunner runner)
    {
        runner.testGroup(HttpServer.class, () ->
        {
            runner.testGroup("constructor(TCPServer)", () ->
            {
                runner.test("with null TCPServer",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    test.assertThrows(() -> HttpServer.create(null, process.getParallelAsyncRunner()),
                        new PreConditionFailure("tcpServer cannot be null."));
                });

                runner.test("with null AsyncRunner",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final TCPServer tcpServer = process.getNetwork().createTCPServer(23211).await())
                    {
                        test.assertThrows(() -> HttpServer.create(tcpServer, null),
                            new PreConditionFailure("asyncRunner cannot be null."));
                    }
                });

                runner.test("with disposed TCPServer",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final TCPServer tcpServer = process.getNetwork().createTCPServer(23211).await())
                    {
                        test.assertTrue(tcpServer.dispose().await());
                        test.assertThrows(() -> HttpServer.create(tcpServer, process.getParallelAsyncRunner()),
                            new PreConditionFailure("tcpServer.isDisposed() cannot be true."));
                    }
                });

                runner.test("with non-disposed TCPServer",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(process))
                    {
                        test.assertFalse(server.isDisposed());
                        test.assertEqual(HttpServerTests.serverAddress, server.getLocalIPAddress());
                        test.assertEqual(HttpServerTests.serverPort, server.getLocalPort());
                        test.assertEqual(Iterable.create(), server.iteratePaths().toList());

                        final Result<Void> serverTask = server.start();

                        final HttpClient client = HttpServerTests.createHttpClient(process);
                        final HttpRequest request = HttpRequest.get("https://" + server.getLocalIPAddress() + ":" + server.getLocalPort() + "/hello").await();
                        try (final HttpResponse response = client.send(request).await())
                        {
                            test.assertNotNull(response);
                            test.assertEqual("HTTP/1.1", response.getHttpVersion());
                            test.assertEqual(404, response.getStatusCode());
                            test.assertEqual("Not Found", response.getReasonPhrase());
                            test.assertEqual("404: Not Found", CharacterReadStream.create(response.getBody()).readEntireString().await());
                        }

                        test.assertTrue(server.dispose().await());
                        test.assertNull(serverTask.await());
                    }
                });
            });

            runner.testGroup("setPath(String,Function1<HttpRequest,HttpResponse>)", () ->
            {
                runner.test("with null path",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(process))
                    {
                        test.assertThrows(() -> server.setPath(null, (HttpRequest request) -> null),
                            new PreConditionFailure("pathString cannot be null."));
                        test.assertEqual(Iterable.create(), server.iteratePaths().toList());
                    }
                });

                runner.test("with empty path",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(process))
                    {
                        test.assertThrows(() -> server.setPath("", (HttpRequest request) -> null),
                            new PreConditionFailure("pathString cannot be empty."));
                        test.assertEqual(Iterable.create(), server.iteratePaths().toList());
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("/") + " and response is null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(process))
                    {
                        final HttpServer setPathResult = server.setPath("/", (HttpRequest request) -> null);
                        test.assertSame(server, setPathResult);
                        test.assertEqual(Iterable.create("/"), server.iteratePaths().toList().map(PathPattern::toString));

                        final Result<Void> serverTask = server.start();
                        try
                        {
                            final HttpClient client = HttpServerTests.createHttpClient(process);
                            try (final HttpResponse response = client.get("http://" + server.getLocalIPAddress() + ":" + server.getLocalPort() + "/").await())
                            {
                                test.assertNotNull(response);
                                test.assertEqual(500, response.getStatusCode());
                                test.assertEqual("500: Internal Server Error", CharacterReadStream.create(response.getBody()).readEntireString().await());
                            }
                        }
                        finally
                        {
                            test.assertTrue(server.dispose().await());
                            serverTask.await();
                        }
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("/") + " and response is not null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(process))
                    {
                        final HttpServer setPathResult = httpServer.setPath("/", (HttpRequest request) ->
                            HttpResponse.create()
                                .setStatusCode(200)
                                .setBody("Hello!"));
                        test.assertSame(httpServer, setPathResult);
                        test.assertEqual(Iterable.create("/"), httpServer.iteratePaths().toList().map(PathPattern::toString));

                        final Result<Void> serverTask = httpServer.start();
                        try
                        {
                            final HttpClient client = HttpServerTests.createHttpClient(process);
                            final HttpResponse response = client.get("http://" + httpServer.getLocalIPAddress() + ":" + httpServer.getLocalPort() + "/").await();
                            test.assertNotNull(response);
                            test.assertEqual(200, response.getStatusCode());
                        }
                        finally
                        {
                            test.assertTrue(httpServer.dispose().await());
                            serverTask.await();
                        }
                    }
                });

                runner.test("with an already existing path",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(process))
                    {
                        final HttpServer setPathResult1 = httpServer.setPath("/", (HttpRequest request) -> HttpResponse.create().setStatusCode(200));
                        test.assertSame(httpServer, setPathResult1);
                        test.assertEqual(Iterable.create("/"), httpServer.iteratePaths().toList().map(PathPattern::toString));

                        final HttpServer setPathResult2 = httpServer.setPath("/", (HttpRequest request) -> HttpResponse.create().setStatusCode(201));
                        test.assertSame(httpServer, setPathResult2);
                        test.assertEqual(Iterable.create("/"), httpServer.iteratePaths().toList().map(PathPattern::toString));

                        final Result<Void> serverTask = httpServer.start();
                        try
                        {
                            final HttpClient client = HttpServerTests.createHttpClient(process);
                            final HttpResponse response = client.get("http://" + httpServer.getLocalIPAddress() + ":" + httpServer.getLocalPort()).await();
                            test.assertNotNull(response);
                            test.assertEqual(201, response.getStatusCode());
                        }
                        finally
                        {
                            test.assertTrue(httpServer.dispose().await());
                            serverTask.await();
                        }
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("/redfish"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(process))
                    {
                        final HttpServer setPathResult = httpServer.setPath("/redfish", (HttpRequest request) -> null);
                        test.assertSame(httpServer, setPathResult);
                        test.assertEqual(Iterable.create("/redfish"), httpServer.iteratePaths().toList().map(PathPattern::toString));
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("onefish"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(process))
                    {
                        final HttpServer setPathResult = httpServer.setPath("onefish", (HttpRequest request) -> null);
                        test.assertSame(httpServer, setPathResult);
                        test.assertEqual(Iterable.create("/onefish"), httpServer.iteratePaths().toList().map(PathPattern::toString));
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("/a\\nice/path"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(process))
                    {
                        final HttpServer setPathResult = httpServer.setPath("/a\\nice/path", (HttpRequest request) -> null);
                        test.assertSame(httpServer, setPathResult);
                        test.assertEqual(Iterable.create("/a/nice/path"), httpServer.iteratePaths().toList().map(PathPattern::toString));
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("/a\\nice/"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(process))
                    {
                        final HttpServer setPathResult = httpServer.setPath("/a\\nice/", (HttpRequest request) -> null);
                        test.assertSame(httpServer, setPathResult);
                        test.assertEqual(Iterable.create("/a/nice"), httpServer.iteratePaths().toList().map(PathPattern::toString));
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("/a\\nice//"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(process))
                    {
                        final HttpServer setPathResult = httpServer.setPath("/a\\nice//", (HttpRequest request) -> null);
                        test.assertSame(httpServer, setPathResult);
                        test.assertEqual(Iterable.create("/a/nice"), httpServer.iteratePaths().toList().map(PathPattern::toString));
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("////"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(process))
                    {
                        final HttpServer setPathResult = httpServer.setPath("////", (HttpRequest request) -> null);
                        test.assertSame(httpServer, setPathResult);
                        test.assertEqual(Iterable.create("/"), httpServer.iteratePaths().toList().map(PathPattern::toString));
                    }
                });

                runner.test("with multiple paths",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(process))
                    {
                        httpServer
                            .setPath("/onefish", (HttpRequest request) -> HttpResponse.create()
                                                                                .setStatusCode(200)
                                                                                .setBody("Two Fish"))
                            .setPath("/redfish", (HttpRequest request) -> HttpResponse.create()
                                                                                .setStatusCode(201)
                                                                                .setBody("Blue Fish"));
                        final Result<Void> serverTask = httpServer.start();
                        try
                        {
                            final HttpClient client = HttpServerTests.createHttpClient(process);
                            final HttpResponse response1 = client.send(HttpRequest.get("http://" + httpServer.getLocalIPAddress() + ":" + httpServer.getLocalPort() + "/onefish").await()).await();
                            test.assertNotNull(response1);
                            test.assertEqual(200, response1.getStatusCode());
                            test.assertEqual("OK", response1.getReasonPhrase());
                            test.assertNotNull(response1.getBody());
                            test.assertEqual("Two Fish", CharacterReadStream.create(response1.getBody()).readEntireString().await());
                            test.assertEqual("", CharacterReadStream.create(response1.getBody()).readEntireString().await());

                            final HttpResponse response2 = client.send(HttpRequest.get("http://" + httpServer.getLocalIPAddress() + ":" + httpServer.getLocalPort() + "/redfish").await()).await();
                            test.assertNotNull(response2);
                            test.assertEqual(201, response2.getStatusCode());
                            test.assertEqual("Created", response2.getReasonPhrase());
                            test.assertNotNull(response2.getBody());
                            test.assertEqual("Blue Fish", CharacterReadStream.create(response2.getBody()).readEntireString().await());
                            test.assertEqual("", CharacterReadStream.create(response2.getBody()).readEntireString().await());
                        }
                        finally
                        {
                            test.assertTrue(httpServer.dispose().await());
                            serverTask.await();
                        }
                    }
                });
            });

            runner.testGroup("setPath(String,Function2<Indexable<String>,HttpRequest,HttpResponse>)", () ->
            {
                runner.test("with " + Strings.escapeAndQuote("/things/*"),
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(process))
                    {
                        final HttpServer setPathResult = httpServer.setPath("/things/*", (Indexable<String> trackedValues, HttpRequest request) ->
                             HttpResponse.create()
                                 .setStatusCode(200)
                                 .setBody("Hello, " + trackedValues.first() + "!"));
                        test.assertSame(httpServer, setPathResult);
                        test.assertEqual(Iterable.create("/things/*"), httpServer.iteratePaths().toList().map(PathPattern::toString));

                        final Result<Void> serverTask = httpServer.start();
                        try
                        {
                            final HttpClient client = HttpServerTests.createHttpClient(process);
                            final HttpResponse response = client.get("http://" + httpServer.getLocalIPAddress() + ":" + httpServer.getLocalPort() + "/things/catsanddogs").await();
                            test.assertNotNull(response);
                            test.assertEqual(200, response.getStatusCode());
                            test.assertEqual("Hello, catsanddogs!", CharacterReadStream.create(response.getBody()).readEntireString().await());
                        }
                        finally
                        {
                            test.assertTrue(httpServer.dispose().await());
                            serverTask.await();
                        }
                    }
                });
            });

            runner.testGroup("setNotFound()", () ->
            {
                runner.test("with null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(process))
                    {
                        test.assertThrows(() -> httpServer.setNotFound(null),
                            new PreConditionFailure("notFoundAction cannot be null."));
                    }
                });

                runner.test("with function that returns a non-null response",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(process))
                    {
                        final HttpServer setNotFoundResult = httpServer.setNotFound((HttpRequest request) ->
                        {
                            return HttpResponse.create()
                                .setStatusCode(456);
                        });
                        test.assertSame(httpServer, setNotFoundResult);

                        final Result<Void> serverTask = httpServer.start();
                        try
                        {
                            final HttpClient client = HttpServerTests.createHttpClient(process);
                            try (final HttpResponse response = client.get("http://" + httpServer.getLocalIPAddress() + ":" + httpServer.getLocalPort() + "/notfound").await())
                            {
                                test.assertNotNull(response);
                                test.assertEqual(456, response.getStatusCode());
                            }
                        }
                        finally
                        {
                            test.assertTrue(httpServer.dispose().await());
                            test.assertNull(serverTask.await());
                        }
                    }
                });

                runner.test("with function that returns a null response",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(process))
                    {
                        final HttpServer setNotFoundResult = httpServer.setNotFound((HttpRequest request) -> null);
                        test.assertSame(httpServer, setNotFoundResult);

                        final Result<Void> serverTask = httpServer.start();
                        try
                        {
                            final HttpClient client = HttpServerTests.createHttpClient(process);
                            try (final HttpResponse response = client.get("http://" + httpServer.getLocalIPAddress() + ":" + httpServer.getLocalPort() + "/").await())
                            {
                                test.assertNotNull(response);
                                test.assertEqual(500, response.getStatusCode());
                                test.assertEqual("500: Internal Server Error", CharacterReadStream.create(response.getBody()).readEntireString().await());
                            }
                        }
                        finally
                        {
                            test.assertTrue(httpServer.dispose().await());
                            serverTask.await();
                        }
                    }
                });
            });

            runner.testGroup("start()", () ->
            {
                runner.test("with TCPServer disposed before start()",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final Network network = process.getNetwork();
                    try (final TCPServer tcpServer = network.createTCPServer(HttpServerTests.serverAddress, HttpServerTests.serverPort).await())
                    {
                        try (final HttpServer httpServer = HttpServer.create(tcpServer, process.getParallelAsyncRunner()))
                        {
                            test.assertTrue(tcpServer.dispose().await());

                            test.assertThrows(() -> httpServer.start().await(),
                                new PreConditionFailure("this.isDisposed() cannot be true."));
                        }
                    }
                });

                runner.test("with TCPServer disposed during start()",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final Network network = process.getNetwork();
                    try (final TCPServer tcpServer = network.createTCPServer(HttpServerTests.serverAddress, HttpServerTests.serverPort).await())
                    {
                        try (final HttpServer httpServer = HttpServer.create(tcpServer, process.getParallelAsyncRunner()))
                        {
                            final Result<Void> serverTask = httpServer.start();

                            test.assertTrue(tcpServer.dispose().await());

                            test.assertNull(serverTask.await());
                        }
                    }
                });

                runner.test("with HttpServer disposed before start()",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(process))
                    {
                        test.assertTrue(httpServer.dispose().await());

                        test.assertThrows(httpServer::start,
                            new PreConditionFailure("this.isDisposed() cannot be true."));
                    }
                });

                runner.test("with HttpServer disposed during start()",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(process))
                    {
                        final Result<Void> serverTask = httpServer.start();

                        test.assertTrue(httpServer.dispose().await());

                        test.assertNull(serverTask.await());
                    }
                });

                runner.test("with one header and no body",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(process))
                    {
                        httpServer.setPath("/echo", (HttpRequest request) ->
                        {
                            final MutableHttpResponse result = HttpResponse.create();

                            result.setStatusCode(200);
                            result.setReasonPhrase("OK");
                            result.setHeaders(request.getHeaders());

                            PostCondition.assertNotNull(result, "result");

                            return result;
                        });

                        final Result<Void> serverTask = httpServer.start();
                        try
                        {
                            final HttpClient httpClient = HttpServerTests.createHttpClient(process);
                            try (final HttpResponse response = httpClient.get("http://" + httpServer.getLocalIPAddress() + ":" + httpServer.getLocalPort() + "/echo", HttpHeaders.create().set("a", "b")).await())
                            {
                                test.assertNotNull(response);
                                test.assertEqual(200, response.getStatusCode());
                                test.assertEqual("OK", response.getReasonPhrase());
                                test.assertEqual(HttpHeaders.create().set("a", "b"), response.getHeaders());
                                test.assertEqual(new byte[0], response.getBody().readAllBytes().await());
                            }
                        }
                        finally
                        {
                            httpServer.dispose().await();
                            serverTask.await();
                        }
                    }
                });

                runner.test("with content-length header but no body",
                    (TestResources resources) -> Tuple.create(resources.getNetwork(), resources.getParallelAsyncRunner()),
                    (Test test, Network network, AsyncRunner parallelAsyncRunner) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(network, parallelAsyncRunner))
                    {
                        httpServer.setPath("/echo", (HttpRequest request) ->
                        {
                            final MutableHttpResponse result = HttpResponse.create();

                            result.setStatusCode(200);
                            result.setReasonPhrase("OK");
                            result.setHeaders(request.getHeaders());

                            PostCondition.assertNotNull(result, "result");

                            return result;
                        });

                        final Result<Void> serverTask = httpServer.start();
                        try
                        {
                            final HttpClient httpClient = HttpServerTests.createHttpClient(network);
                            try (final HttpResponse response = httpClient.get("http://" + httpServer.getLocalIPAddress() + ":" + httpServer.getLocalPort() + "/echo", HttpHeaders.create().set("content-length", "5")).await())
                            {
                                test.assertNotNull(response);
                                test.assertEqual(200, response.getStatusCode());
                                test.assertEqual("OK", response.getReasonPhrase());
                                test.assertEqual(HttpHeaders.create().set("content-length", "5"), response.getHeaders());
                                test.assertEqual(new byte[0], response.getBody().readAllBytes().await());
                            }
                        }
                        finally
                        {
                            httpServer.dispose().await();
                            serverTask.await();
                        }
                    }
                });

                runner.test("with no request path",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(process))
                    {
                        httpServer.setPath("/", (HttpRequest request) ->
                        {
                            final MutableHttpResponse result = HttpResponse.create();

                            result.setStatusCode(200);
                            result.setReasonPhrase("OK");
                            result.setHeaders(request.getHeaders());
                            result.setBody("Hello");

                            PostCondition.assertNotNull(result, "result");

                            return result;
                        });

                        final Result<Void> serverTask = httpServer.start();
                        try
                        {
                            final HttpClient httpClient = HttpServerTests.createHttpClient(process);
                            try (final HttpResponse response = httpClient.get("http://" + httpServer.getLocalIPAddress() + ":" + httpServer.getLocalPort(), HttpHeaders.create().set("a", "b")).await())
                            {
                                test.assertNotNull(response);
                                test.assertEqual(200, response.getStatusCode());
                                test.assertEqual("OK", response.getReasonPhrase());
                                test.assertEqual(
                                    HttpHeaders.create()
                                        .set("a", "b")
                                        .set("Content-Length", 5),
                                    response.getHeaders());
                                test.assertEqual("Hello", CharacterReadStream.create(response.getBody()).readEntireString().await());
                            }
                        }
                        finally
                        {
                            httpServer.dispose().await();
                            serverTask.await();
                        }
                    }
                });
            });

            runner.testGroup("getReasonPhrase(int)", () ->
            {
                final Action2<Integer,String> getReasonPhraseTest = (Integer statusCode, String expected) ->
                {
                    runner.test("with " + statusCode, (Test test) ->
                    {
                        test.assertEqual(expected, HttpServer.getReasonPhrase(statusCode));
                    });
                };

                getReasonPhraseTest.run(-1, null);
                getReasonPhraseTest.run(0, null);
                getReasonPhraseTest.run(99, null);
                getReasonPhraseTest.run(100, "Continue");
                getReasonPhraseTest.run(101, "Switching Protocols");
                getReasonPhraseTest.run(200, "OK");
                getReasonPhraseTest.run(201, "Created");
                getReasonPhraseTest.run(202, "Accepted");
                getReasonPhraseTest.run(400, "Bad Request");
                getReasonPhraseTest.run(404, "Not Found");
                getReasonPhraseTest.run(500, "Internal Server Error");
            });
        });
    }

    static HttpServer createHttpServer(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        return HttpServerTests.createHttpServer(process.getNetwork(), process.getParallelAsyncRunner());
    }

    static HttpServer createHttpServer(Network network, AsyncRunner asyncRunner)
    {
        PreCondition.assertNotNull(network, "network");
        PreCondition.assertNotNull(asyncRunner, "asyncRunner");

        final TCPServer tcpServer = network.createTCPServer(HttpServerTests.serverAddress, HttpServerTests.serverPort).await();
        return HttpServer.create(tcpServer, asyncRunner);
    }

    static HttpClient createHttpClient(DesktopProcess process)
    {
        return HttpServerTests.createHttpClient(process.getNetwork());
    }

    static HttpClient createHttpClient(Network network)
    {
        return BasicHttpClient.create(network);
    }
}
