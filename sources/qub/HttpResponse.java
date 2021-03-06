package qub;

/**
 * An object sent in response to an HTTP request.
 */
public interface HttpResponse extends Disposable
{
    static MutableHttpResponse create()
    {
        return MutableHttpResponse.create();
    }

    /**
     * Get the HTTP version of this response.
     * @return The HTTP version of this response.
     */
    String getHttpVersion();

    /**
     * Get the status code of this response.
     * @return The status code of this response.
     */
    int getStatusCode();

    /**
     * Get the description of the status code.
     * @return The description of the status code.
     */
    String getReasonPhrase();

    /**
     * Get the headers that are included in this response.
     * @return The headers that are included in this response.
     */
    HttpHeaders getHeaders();

    /**
     * Get the header in this response with the provided headerName.
     * @param headerName The name of the header to get.
     * @return The matching header or an error if the header was not found.
     */
    default Result<HttpHeader> getHeader(String headerName)
    {
        return this.getHeaders().get(headerName);
    }

    /**
     * Get the value of the header in this response with the provided headerName.
     * @param headerName The name of the header to get the value of.
     * @return The matching header value or an error if the header was not found.
     */
    default Result<String> getHeaderValue(String headerName)
    {
        return this.getHeaders().getValue(headerName);
    }

    /**
     * Get the parsed value of the Content-Length header that has been set in this response.
     * @return The parsed value of the Content-Length header or an error if the header was not
     * found.
     */
    default Result<Long> getContentLength()
    {
        return Result.create(() ->
        {
            final String contentLengthString = this.getHeaderValue(HttpHeader.ContentLengthName).await();
            return Longs.parse(contentLengthString).await();
        });
    }

    /**
     * Get the body of this response.
     * @return The body of this response.
     */
    ByteReadStream getBody();
}
