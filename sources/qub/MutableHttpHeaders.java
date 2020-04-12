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
    public MutableHttpHeaders clear()
    {
        this.headerMap.clear();
        return this;
    }

    /**
     * Set the provided header within this HTTP headers collection.
     * @param header The header to set.
     */
    public MutableHttpHeaders set(HttpHeader header)
    {
        PreCondition.assertNotNull(header, "header");

        return this.set(header.getName(), header.getValue());
    }

    /**
     * Set the provided header name and value within this HTTP headers collection.
     * @param headerName The name of the header to set.
     * @param headerValue The value of the header to set.
     */
    public MutableHttpHeaders set(String headerName, int headerValue)
    {
        PreCondition.assertNotNullAndNotEmpty(headerName, "headerName");

        return this.set(headerName, Integers.toString(headerValue));
    }

    /**
     * Set the provided header name and value within this HTTP headers collection.
     * @param headerName The name of the header to set.
     * @param headerValue The value of the header to set.
     */
    public MutableHttpHeaders set(String headerName, long headerValue)
    {
        PreCondition.assertNotNullAndNotEmpty(headerName, "headerName");

        return this.set(headerName, Longs.toString(headerValue));
    }

    /**
     * Set the provided header name and value within this HTTP headers collection.
     * @param headerName The name of the header to set.
     * @param headerValue The value of the header to set.
     */
    public MutableHttpHeaders set(String headerName, String headerValue)
    {
        PreCondition.assertNotNullAndNotEmpty(headerName, "headerName");
        PreCondition.assertNotNull(headerValue, "headerValue");

        this.headerMap.set(MutableHttpHeaders.getHeaderKey(headerName), new HttpHeader(headerName, headerValue));
        return this;
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
