package qub;

/**
 * An individual header within a HTTP request.
 */
public class HttpHeader
{
    /**
     * The standard name for the content length header.
     */
    public static final String ContentLengthName = "Content-Length";

    private final String name;
    private final String value;

    private HttpHeader(String name, String value)
    {
        PreCondition.assertNotNullAndNotEmpty(name, "name");
        PreCondition.assertNotNull(value, "value");

        this.name = name;
        this.value = value;
    }

    /**
     * Create a new HTTP header with the provided name and value.
     * @param name The name of the HTTP header.
     * @param value The value of the HTTP header.
     */
    public static HttpHeader create(String name, int value)
    {
        return HttpHeader.create(name, Integers.toString(value));
    }

    /**
     * Create a new HTTP header with the provided name and value.
     * @param name The name of the HTTP header.
     * @param value The value of the HTTP header.
     */
    public static HttpHeader create(String name, String value)
    {
        return new HttpHeader(name, value);
    }

    /**
     * Get the name of this HTTP header.
     * @return
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Get the value of this HTTP header.
     * @return
     */
    public String getValue()
    {
        return this.value;
    }

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof HttpHeader && this.equals((HttpHeader)rhs);
    }

    /**
     * Get whether or not this HttpHeader equals the provided HttpHeader.
     * @param rhs The HttpHeader to compare against this HttpHeader.
     * @return Whether or not this HttpHeader equals the provided HttpHeader.
     */
    public boolean equals(HttpHeader rhs)
    {
        return rhs != null &&
            this.name.equalsIgnoreCase(rhs.name) &&
            this.value.equals(rhs.value);
    }

    @Override
    public String toString()
    {
        return this.name + ": " + this.value;
    }
}
