package qub;

/**
 * A class that can send HTTP requests and receive HTTP responses.
 */
public interface HttpClient
{
    /**
     * Create a new HTTP client using the provided network.
     * @param network The network to send HTTP requests across.
     * @return The new HTTP client.
     */
    static HttpClient create(Network network)
    {
        PreCondition.assertNotNull(network, "network");

        return JavaHttpClient.create();
    }

    /**
     * Send the provided HttpRequest and then wait for the target endpoint to return a HttpResponse.
     * @param request The HttpRequest to send.
     * @return The HttpResponse that the server returned.
     */
    Result<HttpResponse> send(HttpRequest request);

    default Result<HttpResponse> get(String urlString)
    {
        PreCondition.assertNotNullAndNotEmpty(urlString, "urlString");

        return this.get(urlString, HttpHeaders.create());
    }

    default Result<HttpResponse> get(String urlString, HttpHeaders headers)
    {
        PreCondition.assertNotNullAndNotEmpty(urlString, "urlString");
        PreCondition.assertNotNull(headers, "headers");

        return Result.create(() ->
        {
            final URL url = URL.parse(urlString).await();
            return this.get(url, headers).await();
        });
    }

    default Result<HttpResponse> get(URL url)
    {
        return this.get(url, HttpHeaders.create());
    }

    default Result<HttpResponse> get(URL url, HttpHeaders headers)
    {
        PreCondition.assertNotNull(url, "url");
        PreCondition.assertNotNull(headers, "headers");

        return this.send(HttpRequest.get(url).setHeaders(headers));
    }
}
