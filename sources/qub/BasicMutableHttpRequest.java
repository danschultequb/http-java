package qub;

/**
 * An {@link HttpRequest} that will be sent from a {@link TCPClient} to an {@link HttpServer}.
 */
public class BasicMutableHttpRequest implements MutableHttpRequest
{
    private String method;
    private URL url;
    private String httpVersion;
    private final MutableHttpHeaders headers;
    private ByteReadStream body;

    /**
     * Create a new mutable HTTP request object.
     */
    private BasicMutableHttpRequest()
    {
        this.headers = HttpHeaders.create();
        this.httpVersion = "HTTP/1.1";
    }

    /**
     * Create a new mutable HTTP request object.
     */
    public static BasicMutableHttpRequest create()
    {
        return new BasicMutableHttpRequest();
    }

    @Override
    public String getMethod()
    {
        return this.method;
    }

    @Override
    public BasicMutableHttpRequest setMethod(HttpMethod method)
    {
        return (BasicMutableHttpRequest)MutableHttpRequest.super.setMethod(method);
    }

    @Override
    public BasicMutableHttpRequest setMethod(String method)
    {
        PreCondition.assertNotNullAndNotEmpty(method, "method");

        this.method = method;

        return this;
    }

    @Override
    public URL getURL()
    {
        return this.url;
    }

    @Override
    public BasicMutableHttpRequest setUrl(URL url)
    {
        PreCondition.assertNotNull(url, "url");
        PreCondition.assertTrue(url.hasScheme(), "url.hasScheme()");

        this.url = url;

        return this;
    }

    @Override
    public String getHttpVersion()
    {
        return this.httpVersion;
    }

    @Override
    public BasicMutableHttpRequest setHttpVersion(String httpVersion)
    {
        PreCondition.assertNotNullAndNotEmpty(httpVersion, "httpVersion");

        this.httpVersion = httpVersion;

        return this;
    }

    @Override
    public HttpHeaders getHeaders()
    {
        return this.headers;
    }

    @Override
    public BasicMutableHttpRequest setHeader(String headerName, String headerValue)
    {
        this.headers.set(headerName, headerValue);

        return this;
    }

    @Override
    public BasicMutableHttpRequest setHeader(String headerName, int headerValue)
    {
        this.headers.set(headerName, headerValue);

        return this;
    }

    @Override
    public BasicMutableHttpRequest setHeader(String headerName, long headerValue)
    {
        this.headers.set(headerName, headerValue);

        return this;
    }

    @Override
    public BasicMutableHttpRequest setHeaders(Iterable<HttpHeader> headers)
    {
        PreCondition.assertNotNull(headers, "headers");

        this.headers.setAll(headers);

        return this;
    }

    @Override
    public ByteReadStream getBody()
    {
        return this.body;
    }

    @Override
    public BasicMutableHttpRequest setBody(long contentLength, ByteReadStream body)
    {
        PreCondition.assertGreaterThanOrEqualTo(contentLength, 0, "contentLength");
        PreCondition.assertTrue(contentLength > 0 || body == null, "If contentLength is 0, then the body must be null.");
        PreCondition.assertTrue(contentLength == 0 || body != null, "If contentLength is greater than 0, then body must be not null.");

        this.body = body;

        if (contentLength == 0)
        {
            this.headers.remove("Content-Length");
        }
        else
        {
            this.headers.set("Content-Length", contentLength);
        }

        return this;
    }

    @Override
    public BasicMutableHttpRequest setBody(byte[] bodyBytes)
    {
        return (BasicMutableHttpRequest)MutableHttpRequest.super.setBody(bodyBytes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<BasicMutableHttpRequest> setBody(String bodyText)
    {
        return (Result<BasicMutableHttpRequest>)MutableHttpRequest.super.setBody(bodyText);
    }
}
