package qub;

/**
 * A type that can receive incoming HTTP requests.
 */
public class HttpServer implements Disposable
{
    private final TCPServer tcpServer;
    private final AsyncRunner asyncRunner;
    private boolean disposed;
    private final MutableMap<PathPattern,Function2<Indexable<String>,HttpRequest,HttpResponse>> paths;
    private Function1<HttpRequest,HttpResponse> notFoundAction;

    /**
     * Create a new HTTP server based on the provided TCPServer.
     * @param tcpServer The TCPServer that will accept incoming HTTP requests.
     */
    public HttpServer(TCPServer tcpServer, AsyncRunner asyncRunner)
    {
        PreCondition.assertNotNull(tcpServer, "tcpServer");
        PreCondition.assertNotDisposed(tcpServer, "tcpServer");
        PreCondition.assertNotNull(asyncRunner, "asyncRunner");

        this.tcpServer = tcpServer;
        this.asyncRunner = asyncRunner;
        this.paths = Map.create();
        this.notFoundAction = (HttpRequest request) ->
        {
            return new MutableHttpResponse()
                .setHTTPVersion(request.getHttpVersion())
                .setStatusCode(404)
                .setReasonPhrase("Not Found")
                .setBody("<html><body>404: Not Found</body></html>");
        };
    }

    /**
     * Get the local IP address that this HttpServer is bound to.
     * @return The local IP address that this HttpServer is bound to.
     */
    public IPv4Address getLocalIPAddress()
    {
        PreCondition.assertFalse(isDisposed(), "isDisposed()");

        return this.tcpServer.getLocalIPAddress();
    }

    /**
     * Get the local port that this HttpServer is bound to.
     * @return The local port that this HttpServer is bound to.
     */
    public int getLocalPort()
    {
        PreCondition.assertFalse(isDisposed(), "isDisposed()");

        return this.tcpServer.getLocalPort();
    }

    /**
     * Add a new pathString that this HTTP server will respond to.
     * @param pathString The pathString that this HTTP server will respond to.
     * @return The result of adding the provided path.
     */
    public Result<Void> addPath(String pathString, Function1<HttpRequest,HttpResponse> pathAction)
    {
        PreCondition.assertNotNullAndNotEmpty(pathString, "pathString");
        PreCondition.assertNotNull(pathAction, "pathAction");

        return this.addPath(pathString, (Indexable<String> pathMatches, HttpRequest request) -> pathAction.run(request));
    }

    /**
     * Add a new pathString that this HTTP server will respond to.
     * @param pathString The pathString that this HTTP server will respond to.
     * @return The result of adding the provided path.
     */
    public Result<Void> addPath(String pathString, Function2<Indexable<String>,HttpRequest,HttpResponse> pathAction)
    {
        PreCondition.assertNotNullAndNotEmpty(pathString, "pathString");
        PreCondition.assertNotNull(pathAction, "pathAction");

        return Result.create(() ->
        {
            String normalizedPathString = pathString;
            if (normalizedPathString.contains("\\"))
            {
                normalizedPathString = normalizedPathString.replaceAll("\\\\", "/");
            }
            if (!normalizedPathString.startsWith("/"))
            {
                normalizedPathString = '/' + normalizedPathString;
            }

            int endIndex = normalizedPathString.length();
            while (1 < endIndex && normalizedPathString.charAt(endIndex - 1) == '/')
            {
                --endIndex;
            }
            normalizedPathString = normalizedPathString.substring(0, endIndex);

            final PathPattern pathPattern = PathPattern.parse(normalizedPathString);

            if (paths.containsKey(pathPattern))
            {
                throw new AlreadyExistsException("The path " + Strings.escapeAndQuote(pathPattern) + " already exists.");
            }

            this.paths.set(pathPattern, pathAction);
        });
    }

    /**
     * Set the action that will be invoked when this HttpServer receives a request for a path that
     * isn't recognized.
     * @param notFoundAction The action that will be invoked when this HttpServer receives a request
     *                       for a path that isn't recognized.
     */
    public void setNotFound(Function1<HttpRequest,HttpResponse> notFoundAction)
    {
        PreCondition.assertNotNull(notFoundAction, "notFoundAction");

        this.notFoundAction = notFoundAction;
    }

    /**
     * Start listening on the current thread for incoming requests. This method will block until the
     * HttpServer is disposed.
     */
    public Result<Void> start()
    {
        return this.asyncRunner.schedule(() ->
        {
            while(!this.isDisposed())
            {
                final TCPClient acceptedClient = this.tcpServer.accept()
                    .catchError(java.net.SocketException.class, (java.net.SocketException error) ->
                    {
                        final String errorMessage = error.getMessage().toLowerCase();
                        if (!errorMessage.equals("socket closed"))
                        {
                            throw Exceptions.asRuntime(error);
                        }
                    })
                    .await();
                if (acceptedClient != null)
                {
                    try
                    {
                        final MutableHttpRequest request = new MutableHttpRequest();
                        final CharacterReadStream acceptedClientReadStream = CharacterReadStream.create(acceptedClient);

                        final String firstLine = acceptedClientReadStream.readLine().await();
                        final String[] firstLineParts = firstLine.split(" ");
                        request.setMethod(HttpMethod.valueOf(firstLineParts[0]));
                        request.setUrl(URL.parse(firstLineParts[1]).await());
                        request.setHttpVersion(firstLineParts[2]);

                        String headerLine = acceptedClientReadStream.readLine().await();
                        while (!Strings.isNullOrEmpty(headerLine))
                        {
                            final int firstColonIndex = headerLine.indexOf(':');
                            final String headerName = headerLine.substring(0, firstColonIndex);
                            final String headerValue = headerLine.substring(firstColonIndex + 1);
                            request.setHeader(headerName, headerValue);

                            headerLine = acceptedClientReadStream.readLine().await();
                        }

                        final Long requestContentLength = request.getContentLength()
                            .catchError(NotFoundException.class)
                            .await();
                        if (requestContentLength != null)
                        {
                            request.setBody(requestContentLength, acceptedClient);
                        }

                        HttpResponse response;
                        final String pathString = request.getURL().getPath();
                        final Path path = Path.parse(Strings.isNullOrEmpty(pathString) ? "/" : pathString);
                        Indexable<String> pathTrackedValues = null;
                        Function2<Indexable<String>,HttpRequest,HttpResponse> pathAction = null;
                        for (final MapEntry<PathPattern,Function2<Indexable<String>,HttpRequest,HttpResponse>> entry : paths)
                        {
                            final PathPattern pathPattern = entry.getKey();
                            final Iterable<Match> pathMatches = pathPattern.getMatches(path);
                            if (pathMatches.any())
                            {
                                final Match firstMatch = pathMatches.first();
                                final Iterable<Iterable<Character>> trackedCharacters = firstMatch.getTrackedValues();
                                final Iterable<String> trackedStrings = trackedCharacters.map(Characters::join);
                                pathTrackedValues = List.create(trackedStrings);
                                pathAction = entry.getValue();
                                break;
                            }
                        }

                        if (pathAction == null)
                        {
                            response = notFoundAction.run(request);
                        }
                        else
                        {
                            response = pathAction.run(pathTrackedValues, request);
                        }

                        if (response == null)
                        {
                            final MutableHttpResponse mutableResponse = new MutableHttpResponse();
                            mutableResponse.setHTTPVersion(request.getHttpVersion());
                            mutableResponse.setStatusCode(500);
                            mutableResponse.setBody("<html><body>" + mutableResponse.getStatusCode() + ": " + getReasonPhrase(mutableResponse.getStatusCode()) + "</body></html>");
                            response = mutableResponse;
                        }

                        String httpVersion = response.getHTTPVersion();
                        if (Strings.isNullOrEmpty(httpVersion))
                        {
                            httpVersion = "HTTP/1.1";
                        }

                        String reasonPhrase = response.getReasonPhrase();
                        if (Strings.isNullOrEmpty(reasonPhrase))
                        {
                            reasonPhrase = HttpServer.getReasonPhrase(response.getStatusCode());
                        }

                        final BufferedByteWriteStream acceptedClientBufferedWriteStream = BufferedByteWriteStream.create(acceptedClient);
                        final CharacterToByteWriteStream acceptedClientWriteStream = CharacterToByteWriteStream.create(acceptedClientBufferedWriteStream)
                            .setCharacterEncoding(CharacterEncoding.UTF_8)
                            .setNewLine("\r\n");
                        acceptedClientWriteStream.writeLine("%s %s %s", httpVersion, response.getStatusCode(), reasonPhrase).await();
                        for (final HttpHeader header : response.getHeaders())
                        {
                            acceptedClientWriteStream.writeLine("%s:%s", header.getName(), header.getValue()).await();
                        }
                        acceptedClientWriteStream.writeLine().await();

                        final ByteReadStream responseBody = response.getBody();
                        if (responseBody != null)
                        {
                            try
                            {
                                acceptedClientWriteStream.writeAll(responseBody).await();
                            }
                            finally
                            {
                                responseBody.dispose().await();
                            }
                        }
                        acceptedClientBufferedWriteStream.flush().await();
                    }
                    finally
                    {
                        acceptedClient.dispose().await();
                    }
                }
            }
        });
    }

    /**
     * Get all of the paths that have been registered with this server.
     * @return All of the paths that have been registered with this server.
     */
    public Iterable<PathPattern> getPaths()
    {
        return this.paths.getKeys();
    }

    @Override
    public boolean isDisposed()
    {
        return this.disposed;
    }

    @Override
    public Result<Boolean> dispose()
    {
        return Result.create(() ->
        {
            boolean result = !this.disposed;
            if (result)
            {
                this.disposed = true;
                result = this.tcpServer.dispose().await();
            }
            return result;
        });
    }

    /**
     * Get the default reason phrase for the provided status code. These default phrases come create
     * https://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html.
     * @param statusCode The status code to get the default reason phrase for.
     * @return The default reason phrase for the provided status code.
     */
    public static String getReasonPhrase(int statusCode)
    {
        String result = null;

        switch (statusCode)
        {
            case 100:
                result = "Continue";
                break;

            case 101:
                result = "Switching Protocols";
                break;

            case 200:
                result = "OK";
                break;

            case 201:
                result = "Created";
                break;

            case 202:
                result = "Accepted";
                break;

            case 400:
                result = "Bad Request";
                break;

            case 404:
                result = "Not Found";
                break;

            case 500:
                result = "Internal Server Error";
                break;
        }

        return result;
    }
}
