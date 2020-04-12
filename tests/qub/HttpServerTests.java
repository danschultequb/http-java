package qub;

public interface HttpServerTests
{
    IPv4Address serverAddress = IPv4Address.localhost;
    int serverPort = 18034;

    static void test(TestRunner runner)
    {
        runner.testGroup(HttpServer.class, () ->
        {
            runner.testGroup("constructor(TCPServer)", () ->
            {
                runner.test("with null TCPServer", (Test test) ->
                {
                    test.assertThrows(() -> new HttpServer(null, test.getParallelAsyncRunner()),
                        new PreConditionFailure("tcpServer cannot be null."));
                });

                runner.test("with null AsyncRunner", (Test test) ->
                {
                    try (final TCPServer tcpServer = test.getNetwork().createTCPServer(23211).await())
                    {
                        test.assertThrows(() -> new HttpServer(tcpServer, null),
                            new PreConditionFailure("asyncRunner cannot be null."));
                    }
                });

                runner.test("with disposed TCPServer", (Test test) ->
                {
                    try (final TCPServer tcpServer = test.getNetwork().createTCPServer(23211).await())
                    {
                        test.assertTrue(tcpServer.dispose().await());
                        test.assertThrows(() -> new HttpServer(tcpServer, test.getParallelAsyncRunner()),
                            new PreConditionFailure("tcpServer.isDisposed() cannot be true."));
                    }
                });

                runner.test("with non-disposed TCPServer", (Test test) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(test))
                    {
                        test.assertFalse(server.isDisposed());
                        test.assertEqual(HttpServerTests.serverAddress, server.getLocalIPAddress());
                        test.assertEqual(HttpServerTests.serverPort, server.getLocalPort());
                        test.assertEqual(Iterable.create(), server.getPaths());

                        final Result<Void> serverTask = server.start();

                        final HttpClient client = HttpServerTests.createHttpClient(test);
                        final HttpRequest request = HttpRequest.get("https://" + server.getLocalIPAddress() + ":" + server.getLocalPort() + "/hello").await();
                        try (final HttpResponse response = client.send(request).await())
                        {
                            test.assertNotNull(response);
                            test.assertEqual("HTTP/1.1", response.getHTTPVersion());
                            test.assertEqual(404, response.getStatusCode());
                            test.assertEqual("Not Found", response.getReasonPhrase());
                            test.assertEqual("<html><body>404: Not Found</body></html>", response.getBody().asCharacterReadStream().readEntireString().await());
                        }

                        test.assertTrue(server.dispose().await());
                        serverTask.await();
                    }
                });
            });

            runner.testGroup("addPath(String)", () ->
            {
                runner.test("with null path", (Test test) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(test))
                    {
                        test.assertThrows(() -> server.addPath(null, (HttpRequest request) -> null),
                            new PreConditionFailure("pathString cannot be null."));
                        test.assertEqual(Iterable.create(), server.getPaths());
                    }
                });

                runner.test("with empty path", (Test test) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(test))
                    {
                        test.assertThrows(() -> server.addPath("", (HttpRequest request) -> null),
                            new PreConditionFailure("pathString cannot be empty."));
                        test.assertEqual(Iterable.create(), server.getPaths());
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("/"), (Test test) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(test))
                    {
                        test.assertNull(server.addPath("/", (HttpRequest request) ->
                            new MutableHttpResponse()
                                .setStatusCode(200)
                                .setBody("Hello!")).await());
                        test.assertEqual(Array.create("/"), server.getPaths().map(PathPattern::toString));

                        final Result<Void> serverTask = server.start();
                        try
                        {
                            final HttpClient client = HttpServerTests.createHttpClient(test);
                            final HttpResponse response = client.get("http://" + server.getLocalIPAddress() + ":" + server.getLocalPort() + "/").await();
                            test.assertNotNull(response);
                            test.assertEqual(200, response.getStatusCode());
                        }
                        finally
                        {
                            test.assertTrue(server.dispose().await());
                            serverTask.await();
                        }
                    }
                });

                runner.test("with an already existing path", (Test test) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(test))
                    {
                        test.assertNull(server.addPath("/", (HttpRequest request) -> null).await());
                        test.assertThrows(() -> server.addPath("/", (HttpRequest request) -> null).await(),
                            new AlreadyExistsException("The path \"/\" already exists."));
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("/redfish"), (Test test) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(test))
                    {
                        test.assertNull(server.addPath("/redfish", (HttpRequest request) -> null).await());
                        test.assertEqual(Array.create("/redfish"), server.getPaths().map(PathPattern::toString));
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("onefish"), (Test test) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(test))
                    {
                        server.addPath("onefish", (HttpRequest request) -> null).await();
                        test.assertEqual(Array.create("/onefish"), server.getPaths().map(PathPattern::toString));
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("/a\\nice/path"), (Test test) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(test))
                    {
                        server.addPath("/a\\nice/path", (HttpRequest request) -> null).await();
                        test.assertEqual(Array.create("/a/nice/path"), server.getPaths().map(PathPattern::toString));
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("/a\\nice/"), (Test test) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(test))
                    {
                        server.addPath("/a\\nice/", (HttpRequest request) -> null).await();
                        test.assertEqual(Array.create("/a/nice"), server.getPaths().map(PathPattern::toString));
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("/a\\nice//"), (Test test) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(test))
                    {
                        server.addPath("/a\\nice//", (HttpRequest request) -> null).await();
                        test.assertEqual(Array.create("/a/nice"), server.getPaths().map(PathPattern::toString));
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("////"), (Test test) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(test))
                    {
                        server.addPath("////", (HttpRequest request) -> null).await();
                        test.assertEqual(Array.create("/"), server.getPaths().map(PathPattern::toString));
                    }
                });

                runner.test("with multiple paths", (Test test) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(test))
                    {
                        server.addPath("/onefish", (HttpRequest request) -> new MutableHttpResponse()
                                                                                .setStatusCode(200)
                                                                                .setBody("Two Fish")).await();
                        server.addPath("/redfish", (HttpRequest request) -> new MutableHttpResponse()
                                                                                .setStatusCode(201)
                                                                                .setBody("Blue Fish")).await();
                        final Result<Void> serverTask = server.start();
                        try
                        {
                            final HttpClient client = HttpServerTests.createHttpClient(test);
                            final HttpResponse response1 = client.send(HttpRequest.get("http://" + server.getLocalIPAddress() + ":" + server.getLocalPort() + "/onefish").await()).await();
                            test.assertNotNull(response1);
                            test.assertEqual(200, response1.getStatusCode());
                            test.assertEqual("OK", response1.getReasonPhrase());
                            test.assertNotNull(response1.getBody());
                            test.assertEqual("Two Fish", response1.getBody().asCharacterReadStream().readEntireString().await());
                            test.assertEqual("", response1.getBody().asCharacterReadStream().readEntireString().await());

                            final HttpResponse response2 = client.send(HttpRequest.get("http://" + server.getLocalIPAddress() + ":" + server.getLocalPort() + "/redfish").await()).await();
                            test.assertNotNull(response2);
                            test.assertEqual(201, response2.getStatusCode());
                            test.assertEqual("Created", response2.getReasonPhrase());
                            test.assertNotNull(response2.getBody());
                            test.assertEqual("Blue Fish", response2.getBody().asCharacterReadStream().readEntireString().await());
                            test.assertEqual("", response2.getBody().asCharacterReadStream().readEntireString().await());
                        }
                        finally
                        {
                            test.assertTrue(server.dispose().await());
                            serverTask.await();
                        }
                    }
                });

                runner.test("with " + Strings.escapeAndQuote("/things/*"), (Test test) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(test))
                    {
                        test.assertNull(server.addPath("/things/*", (Indexable<String> trackedValues, HttpRequest request) ->
                             new MutableHttpResponse()
                                 .setStatusCode(200)
                                 .setBody("Hello, " + trackedValues.first() + "!")).await());
                        test.assertEqual(Array.create("/things/*"), server.getPaths().map(PathPattern::toString));

                        final Result<Void> serverTask = server.start();
                        try
                        {
                            final HttpClient client = HttpServerTests.createHttpClient(test);
                            final HttpResponse response = client.get("http://" + server.getLocalIPAddress() + ":" + server.getLocalPort() + "/things/catsanddogs").await();
                            test.assertNotNull(response);
                            test.assertEqual(200, response.getStatusCode());
                            test.assertEqual("Hello, catsanddogs!", response.getBody().asCharacterReadStream().readEntireString().await());
                        }
                        finally
                        {
                            test.assertTrue(server.dispose().await());
                            serverTask.await();
                        }
                    }
                });
            });

            runner.testGroup("setNotFound()", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    try (final HttpServer server = HttpServerTests.createHttpServer(test))
                    {
                        test.assertThrows(() -> server.setNotFound(null),
                            new PreConditionFailure("notFoundAction cannot be null."));
                    }
                });

                runner.test("with non-null", (Test test) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(test))
                    {
                        httpServer.setNotFound((HttpRequest request) ->
                        {
                            return new MutableHttpResponse()
                                .setStatusCode(456);
                        });

                        final Result<Void> serverTask = httpServer.start();
                        try
                        {
                            final HttpClient client = HttpServerTests.createHttpClient(test);
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
            });

            runner.testGroup("start()", () ->
            {
                runner.test("with TCPServer disposed before start()", (Test test) ->
                {
                    final Network network = test.getNetwork();
                    try (final TCPServer tcpServer = network.createTCPServer(HttpServerTests.serverAddress, HttpServerTests.serverPort).await())
                    {
                        try (final HttpServer httpServer = new HttpServer(tcpServer, test.getParallelAsyncRunner()))
                        {
                            test.assertTrue(tcpServer.dispose().await());

                            test.assertThrows(() -> httpServer.start().await(),
                                new java.net.SocketException("Socket is closed"));
                        }
                    }
                });

                runner.test("with TCPServer disposed during start()", (Test test) ->
                {
                    final Network network = test.getNetwork();
                    try (final TCPServer tcpServer = network.createTCPServer(HttpServerTests.serverAddress, HttpServerTests.serverPort).await())
                    {
                        try (final HttpServer httpServer = new HttpServer(tcpServer, test.getParallelAsyncRunner()))
                        {
                            final Result<Void> serverTask = httpServer.start();

                            test.assertTrue(tcpServer.dispose().await());

                            test.assertThrows(serverTask::await,
                                new java.net.SocketException("Socket is closed"));
                        }
                    }
                });

                runner.test("with HttpServer disposed before start()", (Test test) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(test))
                    {
                        test.assertTrue(httpServer.dispose().await());

                        test.assertNull(httpServer.start().await());
                    }
                });

                runner.test("with HttpServer disposed during start()", (Test test) ->
                {
                    try (final HttpServer httpServer = HttpServerTests.createHttpServer(test))
                    {
                        final Result<Void> serverTask = httpServer.start();

                        test.assertTrue(httpServer.dispose().await());

                        test.assertNull(serverTask.await());
                    }
                });
            });
        });
    }

    static HttpServer createHttpServer(Test test)
    {
        final TCPServer tcpServer = test.getNetwork().createTCPServer(HttpServerTests.serverAddress, HttpServerTests.serverPort).await();
        return new HttpServer(tcpServer, test.getParallelAsyncRunner());
    }

    static HttpClient createHttpClient(Test test)
    {
        return BasicHttpClient.create(test.getNetwork());
    }
}
