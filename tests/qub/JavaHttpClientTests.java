package qub;

public class JavaHttpClientTests
{
    private static HttpClient createHttpClient(Test test)
    {
        return JavaHttpClient.create();
    }

    public static void test(TestRunner runner)
    {
        runner.testGroup(JavaHttpClient.class, () ->
        {
            HttpClientTests.test(runner, JavaHttpClientTests::createHttpClient);

            runner.testGroup("send(MutableHttpRequest)", runner.skipIfNoNetworkConnection(), () ->
            {
                runner.test("with GET request to https://www.treasurydirect.gov/TA_WS/securities/auctioned?format=json&type=Bill", (Test test) ->
                {
                    final HttpClient httpClient = createHttpClient(test);
                    final URL requestURL = URL.parse("https://www.treasurydirect.gov/TA_WS/securities/auctioned?format=json&type=Bill").await();
                    final MutableHttpRequest httpRequest = new MutableHttpRequest(HttpMethod.GET, requestURL);

                    final HttpResponse httpResponse = httpClient.send(httpRequest).await();
                    test.assertEqual("HTTP/1.1", httpResponse.getHTTPVersion());
                    test.assertEqual(200, httpResponse.getStatusCode());
                    test.assertEqual("OK", httpResponse.getReasonPhrase());
                    test.assertNotNull(httpResponse.getHeaders());
                    test.assertEqual("application/json;charset=UTF-8", httpResponse.getHeaders().getValue("content-type").await());
                    test.assertNotNull(httpResponse.getBody());
                    final String responseBody = CharacterEncoding.UTF_8.decodeAsString(httpResponse.getBody().readAllBytes().await()).await();
                    test.assertStartsWith(responseBody, "[");
                    test.assertEndsWith(responseBody, "]");
                });
            });
        });
    }
}
