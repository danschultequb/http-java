package qub;

public interface HttpHeaders extends Iterable<HttpHeader>
{
    String authorizationHeaderName = "Authorization";
    String bearerPrefix = "Bearer ";
    String tokenPrefix = "Token ";

    static MutableHttpHeaders create()
    {
        return MutableHttpHeaders.create();
    }

    /**
     * Get whether or not this HTTP header collection contains a header with the provided header
     * name.
     * @param headerName The name of the header to look for.
     * @return Whether or not this HTTP header collection contains a header with the provided header
     * name.
     */
    default boolean contains(String headerName)
    {
        PreCondition.assertNotNullAndNotEmpty(headerName, "headerName");

        return this.get(headerName)
            .then(() -> true)
            .catchError(NotFoundException.class, () -> false)
            .await();
    }

    /**
     * Get the header in this collection that has the provided header name.
     * @param headerName The name of the header to get.
     * @return The header in this collection with the provided headerName, if the header exists in
     * this collection.
     */
    Result<HttpHeader> get(String headerName);

    /**
     * Get the value of the header in this collection that has the provided header name.
     * @param headerName The name of the header to get.
     * @return The value of header in this collection with the provided headerName, if the header
     * exists in this collection.
     */
    default Result<String> getValue(String headerName)
    {
        PreCondition.assertNotNullAndNotEmpty(headerName, "headerName");

        return Result.create(() ->
        {
            final HttpHeader header = this.get(headerName).await();
            return header.getValue();
        });
    }

    /**
     * Get the value of the Authorization header.
     * @return The value of the Authorization header.
     */
    default Result<String> getAuthorization()
    {
        return this.getValue("Authorization");
    }

    default Result<String> getAuthorization(String headerValuePrefix)
    {
        PreCondition.assertNotNullAndNotEmpty(headerValuePrefix, "headerValuePrefix");

        return Result.create(() ->
        {
            final String authorization = this.getAuthorization().await();
            if (!authorization.startsWith(headerValuePrefix))
            {
                throw new NotFoundException("No \"Authorization\" header found with a " + Strings.escapeAndQuote(headerValuePrefix) + " prefix.");
            }
            return authorization.substring(headerValuePrefix.length());
        });
    }

    default Result<String> getAuthorizationBearer()
    {
        return this.getAuthorization(HttpHeaders.bearerPrefix);
    }

    default Result<String> getAuthorizationToken()
    {
        return this.getAuthorization(HttpHeaders.tokenPrefix);
    }
}
