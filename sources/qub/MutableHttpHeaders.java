package qub;

/**
 * A collection of HTTP headers to be used in an HTTP request or response.
 */
public class MutableHttpHeaders implements HttpHeaders
{
    private final MutableMap<String,HttpHeader> headerMap;

    /**
     * Create a new empty MutableHttpHeaders collection.
     */
    public MutableHttpHeaders()
    {
        this.headerMap = Map.create();
    }

    public MutableHttpHeaders(Iterable<HttpHeader> headers)
    {
        this();

        if (headers != null && headers.any())
        {
            for (final HttpHeader header : headers)
            {
                this.set(header);
            }
        }
    }

    private static String getHeaderKey(String headerName)
    {
        return headerName.toLowerCase();
    }

    /**
     * Remove all headers create this HTTP header collection.
     */
    public void clear()
    {
        this.headerMap.clear();
    }

    /**
     * Set the provided header within this HTTP headers collection.
     * @param header The header to set.
     */
    public void set(HttpHeader header)
    {
        PreCondition.assertNotNull(header, "header");

        this.set(header.getName(), header.getValue());
    }

    /**
     * Set the provided header name and value within this HTTP headers collection.
     * @param headerName The name of the header to set.
     * @param headerValue The value of the header to set.
     */
    public void set(String headerName, int headerValue)
    {
        PreCondition.assertNotNullAndNotEmpty(headerName, "headerName");

        this.set(headerName, Integers.toString(headerValue));
    }

    /**
     * Set the provided header name and value within this HTTP headers collection.
     * @param headerName The name of the header to set.
     * @param headerValue The value of the header to set.
     */
    public void set(String headerName, long headerValue)
    {
        PreCondition.assertNotNullAndNotEmpty(headerName, "headerName");

        this.set(headerName, Longs.toString(headerValue));
    }

    /**
     * Set the provided header name and value within this HTTP headers collection.
     * @param headerName The name of the header to set.
     * @param headerValue The value of the header to set.
     */
    public void set(String headerName, String headerValue)
    {
        PreCondition.assertNotNullAndNotEmpty(headerName, "headerName");
        PreCondition.assertNotNullAndNotEmpty(headerValue, "headerValue");

        this.headerMap.set(MutableHttpHeaders.getHeaderKey(headerName), new HttpHeader(headerName, headerValue));
    }

    @Override
    public boolean contains(String headerName)
    {
        PreCondition.assertNotNullAndNotEmpty(headerName, "headerName");

        return this.get(headerName)
            .then(() -> true)
            .catchError(() -> false)
            .await();
    }

    /**
     * Get the header in this collection that has the provided header name.
     * @param headerName The name of the header to get.
     * @return The value of the header in this collection, if the header exists in this collection.
     */
    public Result<HttpHeader> get(String headerName)
    {
        PreCondition.assertNotNullAndNotEmpty(headerName, "headerName");

        return this.headerMap.get(MutableHttpHeaders.getHeaderKey(headerName))
            .convertError(NotFoundException.class, () -> new NotFoundException(headerName));
    }

    public Result<String> getValue(String headerName)
    {
        PreCondition.assertNotNullAndNotEmpty(headerName, "headerName");

        return this.get(headerName).then(HttpHeader::getValue);
    }

    public Result<HttpHeader> remove(String headerName)
    {
        PreCondition.assertNotNullAndNotEmpty(headerName, "headerName");

        return this.headerMap.remove(MutableHttpHeaders.getHeaderKey(headerName))
            .convertError(NotFoundException.class, () -> new NotFoundException(headerName));
    }

    @Override
    public Iterator<HttpHeader> iterate()
    {
        return this.headerMap.getValues().iterate();
    }

    @Override
    public boolean equals(Object rhs)
    {
        return Iterable.equals(this, rhs);
    }

    @Override
    public String toString()
    {
        return Iterable.toString(this);
    }
}
