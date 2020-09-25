package qub;

public interface HttpHeaders extends Iterable<HttpHeader>
{
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
}
