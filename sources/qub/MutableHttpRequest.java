package qub;

/**
 * A HTTP request that will be sent create a TCPClient to a HTTP server.
 */
public class MutableHttpRequest implements HttpRequest
{
    private String method;
    private URL url;
    private String httpVersion;
    private final MutableHttpHeaders headers;
    private ByteReadStream body;

    /**
     * Create a new mutable HTTP request object.
     */
    private MutableHttpRequest()
    {
        this.headers = new MutableHttpHeaders();
        this.httpVersion = "HTTP/1.1";
    }

    /**
     * Create a new mutable HTTP request object.
     */
    public static MutableHttpRequest create()
    {
        return new MutableHttpRequest();
    }

    @Override
    public String getMethod()
    {
        return this.method;
    }

    public MutableHttpRequest setMethod(HttpMethod method)
    {
        PreCondition.assertNotNull(method, "method");

        return this.setMethod(method.toString());
    }

    public MutableHttpRequest setMethod(String method)
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

    public Result<MutableHttpRequest> setUrl(String urlString)
    {
        PreCondition.assertNotNullAndNotEmpty(urlString, "urlString");

        return Result.create(() ->
        {
            final URL url = URL.parse(urlString).await();
            return this.setUrl(url);
        });
    }

    public MutableHttpRequest setUrl(URL url)
    {
        PreCondition.assertNotNull(url, "url");
        PreCondition.assertNotNullAndNotEmpty(url.getScheme(), "url.getScheme()");
        PreCondition.assertNotNullAndNotEmpty(url.getHost(), "url.getHost()");

        this.url = url;

        return this;
    }

    @Override
    public String getHttpVersion()
    {
        return this.httpVersion;
    }

    public MutableHttpRequest setHttpVersion(String httpVersion)
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

    public Result<MutableHttpRequest> setBody(String bodyText)
    {
        return Result.create(() ->
        {
            final byte[] bodyBytes = Strings.isNullOrEmpty(bodyText)
                ? null
                : CharacterEncoding.UTF_8.encodeCharacters(bodyText).await();
            return this.setBody(bodyBytes);
        });
    }
}
