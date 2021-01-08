package qub;

/**
 * The HTTP response sent create a HTTP server to a HTTP client as a result of a HTTP request.
 */
public class MutableHttpResponse implements HttpResponse
{
    private boolean disposed;
    private String httpVersion;
    private int statusCode;
    private String reasonPhrase;
    private final MutableHttpHeaders headers;
    private ByteReadStream body;

    /**
     * Create a new MutableHttpResponse object.
     */
    private MutableHttpResponse()
    {
        this.headers = HttpHeaders.create();
        this.body = InMemoryByteStream.create().endOfStream();
    }

    public static MutableHttpResponse create()
    {
        return new MutableHttpResponse();
    }

    /**
     * Set the HTTP version that this response was sent with.
     * @param httpVersion The HTTP version that this response was sent with.
     */
    public MutableHttpResponse setHttpVersion(String httpVersion)
    {
        PreCondition.assertNotNullAndNotEmpty(httpVersion, "httpVersion");

        this.httpVersion = httpVersion;

        return this;
    }

    @Override
    public String getHttpVersion()
    {
        return httpVersion;
    }

    /**
     * Set the status code of this HTTP response.
     * @param statusCode The status code of this HTTP response.
     */
    public MutableHttpResponse setStatusCode(int statusCode)
    {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * Get the status code sent create the HTTP server.
     * @return The status code sent create the HTTP server.
     */
    @Override
    public int getStatusCode()
    {
        return statusCode;
    }

    /**
     * Set the reason phrase of this HTTP response.
     * @param reasonPhrase The reason phrase of this HTTP response.
     */
    public MutableHttpResponse setReasonPhrase(String reasonPhrase)
    {
        this.reasonPhrase = reasonPhrase;

        return this;
    }

    @Override
    public String getReasonPhrase()
    {
        return reasonPhrase;
    }

    /**
     * Set the headers in this response to be the provided headers.
     * @param headers The new set of headers for this response.
     */
    public MutableHttpResponse setHeaders(Iterable<HttpHeader> headers)
    {
        PreCondition.assertNotNull(headers, "headers");

        for (final HttpHeader header : headers)
        {
            this.setHeader(header);
        }

        return this;
    }

    /**
     * Set the provided header in this response.
     * @param header The header to set in this response.
     */
    public MutableHttpResponse setHeader(HttpHeader header)
    {
        PreCondition.assertNotNull(header, "header");

        this.headers.set(header);

        return this;
    }

    /**
     * Set the provided header in this response.
     * @param headerName The name of the header to set.
     * @param headerValue The value of the header to set.
     */
    public MutableHttpResponse setHeader(String headerName, String headerValue)
    {
        PreCondition.assertNotNullAndNotEmpty(headerName, "headerName");

        this.headers.set(headerName, headerValue);

        return this;
    }

    /**
     * Set the provided header in this response.
     * @param headerName The name of the header to set.
     * @param headerValue The value of the header to set.
     */
    public MutableHttpResponse setHeader(String headerName, int headerValue)
    {
        PreCondition.assertNotNullAndNotEmpty(headerName, "headerName");

        this.headers.set(headerName, headerValue);

        return this;
    }

    /**
     * Set the provided header in this response.
     * @param headerName The name of the header to set.
     * @param headerValue The value of the header to set.
     */
    public MutableHttpResponse setHeader(String headerName, long headerValue)
    {
        PreCondition.assertNotNullAndNotEmpty(headerName, "headerName");

        this.headers.set(headerName, headerValue);

        return this;
    }

    /**
     * Get the HTTP headers that were sent create the HTTP server.
     * @return The HTTP headers that were sent create the HTTP server.
     */
    @Override
    public HttpHeaders getHeaders()
    {
        return headers;
    }

    /**
     * Get the body of this MutableHttpResponse.
     * @return The body of this MutableHttpResponse.
     */
    @Override
    public ByteReadStream getBody()
    {
        final ByteReadStream result = this.body;

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    /**
     * Set the body of this response.
     * @param body The body of this response.
     */
    public MutableHttpResponse setBody(ByteReadStream body)
    {
        PreCondition.assertNotNull(body, "body");

        this.body = body;

        return this;
    }

    /**
     * Set the body of this response.
     * @param body The body of this response.
     */
    public MutableHttpResponse setBody(String body)
    {
        PreCondition.assertNotNull(body, "body");

        final InMemoryCharacterToByteStream bodyStream = InMemoryCharacterToByteStream.create();
        if (!Strings.isNullOrEmpty(body))
        {
            bodyStream.write(body).await();
            this.setHeader(HttpHeader.ContentLengthName, bodyStream.getBytes().length);
        }
        return this.setBody(bodyStream.endOfStream());
    }

    @Override
    public boolean isDisposed()
    {
        return disposed;
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
                body.dispose().await();
            }
            return result;
        });
    }
}
