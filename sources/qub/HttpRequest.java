package qub;

/**
 * An immutable HTTP request.
 */
public interface HttpRequest
{
    static MutableHttpRequest create()
    {
        return MutableHttpRequest.create();
    }

    /**
     * Get the HTTP method of this request.
     * @return The HTTP method of this request.
     */
    String getMethod();

    /**
     * Get the version of HTTP that this request was sent with.
     * @return The version of HTTP that this request was sent with.
     */
    String getHttpVersion();

    /**
     * Get the target URL of this request.
     * @return The target URL of this request.
     */
    URL getURL();

    /**
     * Get the headers that are included with this request.
     * @return The headers that are included with this request.
     */
    HttpHeaders getHeaders();

    /**
     * Get the header in this request with the provided headerName.
     * @param headerName The name of the header to get.
     * @return The matching header or an error if the header was not found.
     */
    default Result<HttpHeader> getHeader(String headerName)
    {
        return this.getHeaders().get(headerName);
    }

    /**
     * Get the value of the header in this request with the provided headerName.
     * @param headerName The name of the header to get the value of.
     * @return The matching header value or an error if the header was not found.
     */
    default Result<String> getHeaderValue(String headerName)
    {
        return this.getHeaders().getValue(headerName);
    }

    /**
     * Get the parsed value of the Content-Length header that has been set in this request.
     * @return The parsed value of the Content-Length header or an error if the header was not
     * found.
     */
    default Result<Long> getContentLength()
    {
        return Result.create(() ->
        {
            final String headerValue = this.getHeaderValue(HttpHeader.ContentLengthName).await();
            return Longs.parse(headerValue).await();
        });
    }

    /**
     * Get the body of this request.
     * @return The body of this request.
     */
    ByteReadStream getBody();

    static Result<MutableHttpRequest> get(String urlString)
    {
        PreCondition.assertNotNullAndNotEmpty(urlString, "urlString");

        return Result.create(() ->
        {
            final URL url = URL.parse(urlString).await();
            return HttpRequest.get(url);
        });
    }

    static MutableHttpRequest get(URL url)
    {
        PreCondition.assertNotNull(url, "url");

        return HttpRequest.create()
            .setMethod(HttpMethod.GET)
            .setUrl(url);
    }
}
