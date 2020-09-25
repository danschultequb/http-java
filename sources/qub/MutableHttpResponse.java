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
    public MutableHttpResponse()
    {
        this.headers = HttpHeaders.create();
        this.body = InMemoryByteStream.create().endOfStream();
    }

    /**
     * Set the HTTP version that this response was sent with.
     * @param httpVersion The HTTP version that this response was sent with.
     */
    public MutableHttpResponse setHTTPVersion(String httpVersion)
    {
        PreCondition.assertNotNullAndNotEmpty(httpVersion, "httpVersion");

        this.httpVersion = httpVersion;

        return this;
    }

    @Override
    public String getHTTPVersion()
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
    public MutableHttpResponse setHeaders(HttpHeaders headers)
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

        final InMemoryByteStream bodyStream = InMemoryByteStream.create();
        if (!Strings.isNullOrEmpty(body))
        {
            CharacterWriteStream.create(bodyStream).write(body).await();

            final int bodyStreamByteCount = bodyStream.getCount();
            this.setHeader(HttpHeader.ContentLengthName, bodyStreamByteCount);
        }
        bodyStream.endOfStream();
        return this.setBody(bodyStream);
    }

    @Override
    public boolean isDisposed()
    {
        return disposed;
    }

    @Override
    public Result<Boolean> dispose()
    {
        Result<Boolean> result;
        if (disposed)
        {
            result = Result.successFalse();
        }
        else
        {
            disposed = true;
            if (body == null)
            {
                result = Result.successTrue();
            }
            else
            {
                result = body.dispose();
            }
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }
}
