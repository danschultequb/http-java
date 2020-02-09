package qub;

public interface HttpClientTests
{
    static void test(TestRunner runner, Function1<Test,HttpClient> creator)
    {
        runner.testGroup(HttpClient.class, () ->
        {
            runner.testGroup("send(MutableHttpRequest)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final HttpClient httpClient = creator.run(test);
                    test.assertThrows(() -> httpClient.send(null), new PreConditionFailure("request cannot be null."));
                });

                runner.test("with unknown host", (Test test) ->
                {
                    final HttpClient httpClient = creator.run(test);
                    final MutableHttpRequest httpRequest = new MutableHttpRequest(HttpMethod.GET, URL.parse("http://www.idontexistbecauseimnotagoodurl.com").await());
                    test.assertThrows(() -> httpClient.send(httpRequest).await(),
                        new RuntimeException(new java.net.UnknownHostException("www.idontexistbecauseimnotagoodurl.com")));
                });

                runner.test("with HEAD request to www.example.com", (Test test) ->
                {
                    final HttpClient httpClient = creator.run(test);
                    final MutableHttpRequest httpRequest = new MutableHttpRequest(HttpMethod.HEAD, URL.parse("http://www.example.com").await());

                    try (final HttpResponse httpResponse = httpClient.send(httpRequest).await())
                    {
                        test.assertEqual("HTTP/1.1", httpResponse.getHTTPVersion());
                        test.assertEqual(200, httpResponse.getStatusCode());
                        test.assertEqual("OK", httpResponse.getReasonPhrase());
                        test.assertNotNull(httpResponse.getHeaders());
                        final String contentLength = httpResponse.getHeaders().getValue("content-length").await();
                        test.assertOneOf(Iterable.create("648", "1256"), contentLength);
                        try (final ByteReadStream responseBody = httpResponse.getBody())
                        {
                            test.assertNotNull(responseBody);
                            final String bodyString = responseBody.asCharacterReadStream().readEntireString().await();
                            test.assertEqual("", bodyString);
                        }
                    }
                });

                runner.test("with GET request to www.example.com", (Test test) ->
                {
                    final HttpClient httpClient = creator.run(test);
                    final MutableHttpRequest httpRequest = new MutableHttpRequest(HttpMethod.GET, URL.parse("http://www.example.com").await());

                    try (final HttpResponse httpResponse = httpClient.send(httpRequest).await())
                    {
                        test.assertEqual("HTTP/1.1", httpResponse.getHTTPVersion());
                        test.assertEqual(200, httpResponse.getStatusCode());
                        test.assertEqual("OK", httpResponse.getReasonPhrase());
                        test.assertNotNull(httpResponse.getHeaders());
                        final String contentLength = httpResponse.getHeaders().getValue("content-length").await();
                        test.assertOneOf(Iterable.create("1164", "1256", "1270"), contentLength);
                        test.assertNotNull(httpResponse.getBody());
                        final String bodyString = httpResponse.getBody().asCharacterReadStream().readEntireString().await();
                        test.assertNotNull(bodyString);
                        test.assertStartsWith(bodyString, "<!doctype html>", CharacterComparer.CaseInsensitive);
                        test.assertContains(bodyString, "<div>");
                        test.assertContains(bodyString, "<h1>Example Domain</h1>");
                        test.assertContains(bodyString, "</div>");
                    }
                });

                runner.test("with GET request to http://www.treasurydirect.gov/TA_WS/securities/auctioned?format=json&type=Bill", (Test test) ->
                {
                    final HttpClient httpClient = creator.run(test);
                    final MutableHttpRequest httpRequest = new MutableHttpRequest(HttpMethod.GET, URL.parse("http://www.treasurydirect.gov/TA_WS/securities/auctioned?format=json&type=Bill").await());

                    final HttpResponse httpResponse = httpClient.send(httpRequest).await();
                    test.assertTrue(httpResponse.getHTTPVersion().equals("HTTP/1.0") || httpResponse.getHTTPVersion().equals("HTTP/1.1"));
                    test.assertEqual(302, httpResponse.getStatusCode());
                    test.assertEqual("Moved Temporarily", httpResponse.getReasonPhrase());
                    test.assertNotNull(httpResponse.getHeaders());
                    final String locationHeader = httpResponse.getHeaders().getValue("location").await();
                    test.assertEndsWith(locationHeader, "www.treasurydirect.gov/TA_WS/securities/auctioned?format=json&type=Bill", "Incorrect Location header");
                    test.assertEqual("0", httpResponse.getHeaders().getValue("content-length").await());
                    final ByteReadStream responseBody = httpResponse.getBody();
                    test.assertNotNull(responseBody);
                    test.assertThrows(() -> responseBody.readAllBytes().await(),
                        new EndOfStreamException());
                });
            });
        });
    }
}
