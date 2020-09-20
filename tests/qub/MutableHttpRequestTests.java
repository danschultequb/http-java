package qub;

public class MutableHttpRequestTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(MutableHttpRequest.class, () ->
        {
            runner.testGroup("constructor(HttpMethod,URL)", () ->
            {
                runner.test("with null HttpMethod", (Test test) ->
                {
                    final URL url = URL.parse("https://www.example.com").await();
                    test.assertThrows(() -> new MutableHttpRequest(null, url), new PreConditionFailure("method cannot be null."));
                });

                runner.test("with null url", (Test test) ->
                {
                    final HttpMethod method = HttpMethod.GET;
                    test.assertThrows(() -> new MutableHttpRequest(method, null), new PreConditionFailure("url cannot be null."));
                });

                runner.test("with valid HttpMethod and valid url", (Test test) ->
                {
                    final HttpMethod method = HttpMethod.GET;
                    final URL url = URL.parse("https://www.example.com").await();
                    final MutableHttpRequest request = new MutableHttpRequest(method, url);
                    test.assertEqual(HttpMethod.GET, request.getMethod());
                    test.assertEqual(url, request.getURL());
                    test.assertEqual(Iterable.create(), request.getHeaders());
                    test.assertNull(request.getBody());
                });
            });

            runner.testGroup("setMethod(HttpMethod)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final MutableHttpRequest request = create(HttpMethod.GET, "https://www.example.com");
                    test.assertThrows(() -> request.setMethod(null), new PreConditionFailure("method cannot be null."));
                    test.assertEqual(HttpMethod.GET, request.getMethod());
                });

                runner.test("with POST", (Test test) ->
                {
                    final MutableHttpRequest request = create(HttpMethod.GET, "https://www.example.com");
                    request.setMethod(HttpMethod.POST);
                    test.assertEqual(HttpMethod.POST, request.getMethod());
                });
            });

            runner.testGroup("setUrl(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final MutableHttpRequest request = create(HttpMethod.GET, "https://www.example.com");
                    test.assertThrows(() -> request.setUrl((String)null), new PreConditionFailure("urlString cannot be null."));
                    test.assertEqual(URL.parse("https://www.example.com").await(), request.getURL());
                });

                runner.test("with empty", (Test test) ->
                {
                    final MutableHttpRequest request = create(HttpMethod.GET, "https://www.example.com");
                    test.assertThrows(() -> request.setUrl(""), new PreConditionFailure("urlString cannot be empty."));
                    test.assertEqual("https://www.example.com", request.getURL().toString());
                });

                runner.test("with invalid URL", (Test test) ->
                {
                    final MutableHttpRequest request = create(HttpMethod.GET, "https://www.example.com");
                    test.assertThrows(() -> request.setUrl("I'm not a valid url").await(),
                        new IllegalArgumentException("A URL must begin with either a scheme (such as \"http\") or a host (such as \"www.example.com\"), not \"'\"."));
                    test.assertEqual("https://www.example.com", request.getURL().toString());
                });

                runner.test("with valid URL", (Test test) ->
                {
                    final MutableHttpRequest request = create(HttpMethod.GET, "https://www.example.com");
                    test.assertNull(request.setUrl("http://www.google.com").await());
                    test.assertEqual("http://www.google.com", request.getURL().toString());
                });
            });

            runner.testGroup("setBody(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final MutableHttpRequest request = create(HttpMethod.GET, "https://www.example.com");
                    test.assertThrows(() -> request.setBody((String)null), new PreConditionFailure("bodyText cannot be null."));
                    test.assertNull(request.getBody());
                    test.assertEqual(Iterable.create(), request.getHeaders());
                });

                runner.test("with empty", (Test test) ->
                {
                    final MutableHttpRequest request = create(HttpMethod.GET, "https://www.example.com");
                    test.assertThrows(() -> request.setBody(""), new PreConditionFailure("bodyText cannot be empty."));
                    test.assertNull(request.getBody());
                    test.assertEqual(Iterable.create(), request.getHeaders());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final MutableHttpRequest request = create(HttpMethod.GET, "https://www.example.com");
                    request.setBody("hello");
                    test.assertEqual("hello", CharacterReadStream.create(request.getBody()).readLine().await());
                    test.assertEqual(
                        Iterable.create(new HttpHeader("Content-Length", 5)),
                        request.getHeaders());
                });
            });

            runner.testGroup("setBody(byte[])", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final MutableHttpRequest request = create(HttpMethod.GET, "https://www.example.com");
                    request.setBody((byte[])null);
                    test.assertNull(request.getBody());
                    test.assertEqual(Iterable.create(), request.getHeaders());
                });

                runner.test("with empty", (Test test) ->
                {
                    final MutableHttpRequest request = create(HttpMethod.GET, "https://www.example.com");
                    request.setBody(new byte[0]);
                    test.assertNull(request.getBody());
                    test.assertEqual(Iterable.create(), request.getHeaders());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final MutableHttpRequest request = create(HttpMethod.GET, "https://www.example.com");
                    request.setBody(new byte[] { 0, 1, 2, 3, 4 });
                    test.assertEqual(new byte[] { 0, 1, 2, 3, 4 }, request.getBody().readAllBytes().await());
                    test.assertEqual(
                        Iterable.create(new HttpHeader("Content-Length", 5)),
                        request.getHeaders());
                });
            });

            runner.testGroup("setBody(int,ByteReadStream)", () ->
            {
                runner.test("with negative contentLength", (Test test) ->
                {
                    final MutableHttpRequest request = create(HttpMethod.POST, "https://www.example.com");
                    test.assertThrows(() -> request.setBody(-1, null),
                        new PreConditionFailure("contentLength (-1) must be greater than or equal to 0."));
                    test.assertNull(request.getBody());
                    test.assertEqual(Iterable.create(), request.getHeaders());
                });

                runner.test("with 0 contentLength and null body", (Test test) ->
                {
                    final MutableHttpRequest request = create(HttpMethod.POST, "https://www.example.com");
                    request.setBody(0, null);
                    test.assertNull(request.getBody());
                    test.assertEqual(Iterable.create(), request.getHeaders());
                });

                runner.test("with 0 contentLength and non-null body", (Test test) ->
                {
                    final MutableHttpRequest request = create(HttpMethod.POST, "https://www.example.com");
                    test.assertThrows(() -> request.setBody(0, InMemoryByteStream.create()),
                        new PreConditionFailure("If contentLength is 0, then the body must be null. cannot be false."));
                    test.assertNull(request.getBody());
                    test.assertEqual(Iterable.create(), request.getHeaders());
                });

                runner.test("with 3 contentLength and null body", (Test test) ->
                {
                    final MutableHttpRequest request = create(HttpMethod.POST, "https://www.example.com");
                    test.assertThrows(() -> request.setBody(3, null),
                        new PreConditionFailure("If contentLength is greater than 0, then body must be not null. cannot be false."));
                    test.assertNull(request.getBody());
                    test.assertEqual(Iterable.create(), request.getHeaders());
                });

                runner.test("with 3 contentLength and non-null body", (Test test) ->
                {
                    final MutableHttpRequest request = create(HttpMethod.POST, "https://www.example.com");
                    request.setBody(3, new InMemoryByteStream(new byte[] { 0, 1, 2 }));
                    test.assertNotNull(request.getBody());
                    test.assertEqual(
                        Iterable.create(new HttpHeader("Content-Length", 3)),
                        request.getHeaders());
                });
            });
        });
    }

    private static MutableHttpRequest create(HttpMethod method, String url)
    {
        return new MutableHttpRequest(method, URL.parse(url).await());
    }
}
