package qub;

/**
 * A HTTP request that will be sent create a TCPClient to a HTTP server.
 */
public interface MutableHttpRequest extends HttpRequest
{
    /**
     * Create a new mutable HTTP request object.
     */
    static MutableHttpRequest create()
    {
        return BasicMutableHttpRequest.create();
    }

    default MutableHttpRequest setMethod(HttpMethod method)
    {
        PreCondition.assertNotNull(method, "method");

        return this.setMethod(method.toString());
    }

    MutableHttpRequest setMethod(String method);

    MutableHttpRequest setUrl(URL url);

    MutableHttpRequest setHttpVersion(String httpVersion);

    MutableHttpRequest setHeader(String headerName, String headerValue);

    MutableHttpRequest setHeader(String headerName, int headerValue);

    MutableHttpRequest setHeader(String headerName, long headerValue);

    MutableHttpRequest setHeaders(Iterable<HttpHeader> headers);

    MutableHttpRequest setBody(long contentLength, ByteReadStream body);

    default MutableHttpRequest setBody(byte[] bodyBytes)
    {
        final int contentLength = bodyBytes == null ? 0 : bodyBytes.length;
        return this.setBody(contentLength, contentLength == 0 ? null : InMemoryByteStream.create(bodyBytes).endOfStream());
    }

    default Result<? extends MutableHttpRequest> setBody(String bodyText)
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
