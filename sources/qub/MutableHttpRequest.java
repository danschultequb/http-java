package qub;

/**
 * A HTTP request that will be sent create a TCPClient to a HTTP server.
 */
public class MutableHttpRequest implements HttpRequest
{
    private HttpMethod method;
    private URL url;
    private String httpVersion;
    private final MutableHttpHeaders headers;
    private ByteReadStream body;

    /**
     * Create a new mutable HTTP request object.
     */
    public MutableHttpRequest()
    {
        this.headers = new MutableHttpHeaders();
        this.httpVersion = "HTTP/1.1";
    }

    /**
     * Create a new mutable HTTP request object.
     * @param method The HTTP method that will be used with this request.
     * @param url The URL where this HTTP request will be sent to.
     */
    public MutableHttpRequest(HttpMethod method, URL url)
    {
        PreCondition.assertNotNull(method, "method");
        PreCondition.assertNotNull(url, "url");

        this.method = method;
        this.url = url;
        this.headers = new MutableHttpHeaders();
    }

    @Override
    public HttpMethod getMethod()
    {
        return this.method;
    }

    public MutableHttpRequest setMethod(HttpMethod method)
    {
        PreCondition.assertNotNull(method, "method");

        this.method = method;

        return this;
    }

    @Override
    public URL getURL()
    {
        return this.url;
    }

    public Result<Void> setUrl(String urlString)
    {
        PreCondition.assertNotNullAndNotEmpty(urlString, "urlString");

        return URL.parse(urlString)
            .then((Action1<URL>)this::setUrl);
    }

    public MutableHttpRequest setUrl(URL url)
    {
        PreCondition.assertNotNull(url, "url");

        this.url = url;

        return this;
    }

    @Override
    public String getHttpVersion()
    {
        return this.httpVersion;
    }

    public void setHttpVersion(String httpVersion)
    {
        PreCondition.assertNotNullAndNotEmpty(httpVersion, "httpVersion");

        this.httpVersion = httpVersion;
    }

    @Override
    public HttpHeaders getHeaders()
    {
        return this.headers;
    }

    public MutableHttpRequest setHeader(String headerName, String headerValue)
    {
        this.headers.set(headerName, headerValue);

        return this;
    }

    public MutableHttpRequest setHeader(String headerName, int headerValue)
    {
        this.headers.set(headerName, headerValue);

        return this;
    }

    public MutableHttpRequest setHeader(String headerName, long headerValue)
    {
        this.headers.set(headerName, headerValue);

        return this;
    }

    @Override
    public ByteReadStream getBody()
    {
        return this.body;
    }

    public MutableHttpRequest setBody(long contentLength, ByteReadStream body)
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

    public MutableHttpRequest setBody(byte[] bodyBytes)
    {
        final int contentLength = bodyBytes == null ? 0 : bodyBytes.length;
        this.setBody(contentLength, contentLength == 0 ? null : new InMemoryByteStream(bodyBytes).endOfStream());

        return this;
    }

    public Result<Void> setBody(String bodyText)
    {
        PreCondition.assertNotNullAndNotEmpty(bodyText, "bodyText");

        return CharacterEncoding.UTF_8.encodeCharacters(bodyText)
            .then((Action1<byte[]>)this::setBody);
    }
}
