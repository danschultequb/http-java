package qub;

public interface MutableHttpRequestTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(MutableHttpRequest.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final MutableHttpRequest request = MutableHttpRequest.create();
                test.assertNotNull(request);
                test.assertNull(request.getMethod());
                test.assertEqual("HTTP/1.1", request.getHttpVersion());
                test.assertNull(request.getURL());
                test.assertEqual(new MutableHttpHeaders(), request.getHeaders());
                test.assertNull(request.getBody());
            });

            runner.testGroup("setMethod(HttpMethod)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final MutableHttpRequest request = MutableHttpRequest.create();
                    test.assertThrows(() -> request.setMethod((HttpMethod)null),
                        new PreConditionFailure("method cannot be null."));
                    test.assertNull(request.getMethod());
                });

                for (final HttpMethod method : HttpMethod.values())
                {
                    runner.test("with " + method, (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        final MutableHttpRequest setMethodResult = request.setMethod(method);
                        test.assertSame(request, setMethodResult);
                        test.assertEqual(method.toString(), request.getMethod());
                    });
                }
            });

            runner.testGroup("setMethod(String)", () ->
            {
                final Action2<String,Throwable> setMethodErrorTest = (String method, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(method), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        test.assertThrows(() -> request.setMethod(method), expected);
                        test.assertNull(request.getMethod());
                    });
                };

                setMethodErrorTest.run(null, new PreConditionFailure("method cannot be null."));
                setMethodErrorTest.run("", new PreConditionFailure("method cannot be empty."));

                final Action1<String> setMethodTest = (String method) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(method), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        final MutableHttpRequest setMethodResult = request.setMethod(method);
                        test.assertSame(request, setMethodResult);
                        test.assertEqual(method, request.getMethod());
                    });
                };

                for (final HttpMethod method : HttpMethod.values())
                {
                    setMethodTest.run(method.toString());
                }
                setMethodTest.run("apples");
            });

            runner.testGroup("setUrl(String)", () ->
            {
                final Action2<String,Throwable> setUrlErrorTest = (String url, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(url), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        test.assertThrows(() -> request.setUrl(url).await(), expected);
                        test.assertNull(request.getURL());
                    });
                };

                setUrlErrorTest.run(null, new PreConditionFailure("urlString cannot be null."));
                setUrlErrorTest.run("", new PreConditionFailure("urlString cannot be empty."));
                setUrlErrorTest.run("I'm not a valid url", new IllegalArgumentException("A URL must begin with either a scheme (such as \"http\") or a host (such as \"www.example.com\"), not \"'\"."));
                setUrlErrorTest.run("www.google.com", new PreConditionFailure("url.getScheme() cannot be null."));

                final Action1<String> setUrlTest = (String url) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(url), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        final MutableHttpRequest setUrlResult = request.setUrl(url).await();
                        test.assertSame(request, setUrlResult);
                        test.assertEqual(url, request.getURL().toString());
                    });
                };

                setUrlTest.run("http://www.google.com");
            });

            runner.testGroup("setHttpVersion(String)", () ->
            {
                final Action2<String,Throwable> setHttpVersionErrorTest = (String httpVersion, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(httpVersion), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        test.assertThrows(() -> request.setHttpVersion(httpVersion), expected);
                        test.assertEqual("HTTP/1.1", request.getHttpVersion());
                    });
                };

                setHttpVersionErrorTest.run(null, new PreConditionFailure("httpVersion cannot be null."));
                setHttpVersionErrorTest.run("", new PreConditionFailure("httpVersion cannot be empty."));

                final Action1<String> setHttpVersionTest = (String httpVersion) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(httpVersion), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        final MutableHttpRequest setHttpVersionResult = request.setHttpVersion(httpVersion);
                        test.assertSame(request, setHttpVersionResult);
                        test.assertEqual(httpVersion, request.getHttpVersion());
                    });
                };

                setHttpVersionTest.run("HTTP/1.1");
                setHttpVersionTest.run("HTTP/2.0");
                setHttpVersionTest.run("spam");
            });

            runner.testGroup("setHeader(String,String)", () ->
            {
                final Action3<String,String,Throwable> setHeaderErrorTest = (String headerName, String headerValue, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Iterable.create(headerName, headerValue).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        test.assertThrows(() -> request.setHeader(headerName, headerValue), expected);
                        test.assertEqual(new MutableHttpHeaders(), request.getHeaders());
                    });
                };

                setHeaderErrorTest.run(null, "there", new PreConditionFailure("headerName cannot be null."));
                setHeaderErrorTest.run("", "there", new PreConditionFailure("headerName cannot be empty."));
                setHeaderErrorTest.run("hello", null, new PreConditionFailure("headerValue cannot be null."));

                final Action2<String,String> setHeaderTest = (String headerName, String headerValue) ->
                {
                    runner.test("with " + English.andList(Iterable.create(headerName, headerValue).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        final MutableHttpRequest setHeaderResult = request.setHeader(headerName, headerValue);
                        test.assertSame(request, setHeaderResult);
                        test.assertEqual(new MutableHttpHeaders().set(headerName, headerValue), request.getHeaders());
                    });
                };

                setHeaderTest.run("hello", "");
                setHeaderTest.run("hello", "there");
            });

            runner.testGroup("setHeader(String,int)", () ->
            {
                final Action2<String,Integer> setHeaderTest = (String headerName, Integer headerValue) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(headerName), headerValue), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        final MutableHttpRequest setHeaderResult = request.setHeader(headerName, headerValue);
                        test.assertSame(request, setHeaderResult);
                        test.assertEqual(new MutableHttpHeaders().set(headerName, headerValue), request.getHeaders());
                    });
                };

                setHeaderTest.run("hello", -1);
                setHeaderTest.run("hello", 0);
                setHeaderTest.run("hello", 1);
            });

            runner.testGroup("setHeader(String,long)", () ->
            {
                final Action2<String,Long> setHeaderTest = (String headerName, Long headerValue) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(headerName), headerValue), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        final MutableHttpRequest setHeaderResult = request.setHeader(headerName, headerValue);
                        test.assertSame(request, setHeaderResult);
                        test.assertEqual(new MutableHttpHeaders().set(headerName, headerValue), request.getHeaders());
                    });
                };

                setHeaderTest.run("hello", -1L);
                setHeaderTest.run("hello", 0L);
                setHeaderTest.run("hello", 1L);
            });

            runner.testGroup("setBody(int,ByteReadStream)", () ->
            {
                final Action4<String,Integer,ByteReadStream,Throwable> setBodyErrorTest = (String testName, Integer contentLength, ByteReadStream body, Throwable expected) ->
                {
                    runner.test(testName, (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        test.assertThrows(() -> request.setBody(contentLength, body), expected);
                        test.assertNull(request.getBody());
                        test.assertEqual(new MutableHttpHeaders(), request.getHeaders());
                    });
                };

                setBodyErrorTest.run("with negative contentLength", -1, null, new PreConditionFailure("contentLength (-1) must be greater than or equal to 0."));
                setBodyErrorTest.run("with 0 contentLength and non-null body", 0, InMemoryByteStream.create(), new PreConditionFailure("If contentLength is 0, then the body must be null. cannot be false."));
                setBodyErrorTest.run("with 3 contentLength and null body", 3, null, new PreConditionFailure("If contentLength is greater than 0, then body must be not null. cannot be false."));

                runner.test("with 0 contentLength and null body", (Test test) ->
                {
                    final MutableHttpRequest request = MutableHttpRequest.create();
                    final MutableHttpRequest setBodyResult = request.setBody(0, null);
                    test.assertSame(request, setBodyResult);
                    test.assertNull(request.getBody());
                    test.assertEqual(new MutableHttpHeaders(), request.getHeaders());
                });

                runner.test("with 3 contentLength and non-null body", (Test test) ->
                {
                    final MutableHttpRequest request = MutableHttpRequest.create();
                    request.setBody(3, new InMemoryByteStream(new byte[] { 0, 1, 2 }));
                    test.assertNotNull(request.getBody());
                    test.assertEqual(
                        Iterable.create(new HttpHeader("Content-Length", 3)),
                        request.getHeaders());
                });
            });

            runner.testGroup("setBody(byte[])", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final MutableHttpRequest request = MutableHttpRequest.create();
                    final MutableHttpRequest setBodyResult = request.setBody((byte[])null);
                    test.assertSame(request, setBodyResult);
                    test.assertNull(request.getBody());
                    test.assertEqual(new MutableHttpHeaders(), request.getHeaders());
                });

                runner.test("with empty", (Test test) ->
                {
                    final MutableHttpRequest request = MutableHttpRequest.create();
                    final MutableHttpRequest setBodyResult = request.setBody(new byte[0]);
                    test.assertSame(request, setBodyResult);
                    test.assertNull(request.getBody());
                    test.assertEqual(new MutableHttpHeaders(), request.getHeaders());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final MutableHttpRequest request = MutableHttpRequest.create();
                    final MutableHttpRequest setBodyResult = request.setBody(new byte[] { 0, 1, 2, 3, 4 });
                    test.assertSame(request, setBodyResult);
                    test.assertEqual(new byte[] { 0, 1, 2, 3, 4 }, request.getBody().readAllBytes().await());
                    test.assertEqual(
                        new MutableHttpHeaders().set(HttpHeader.ContentLengthName, 5),
                        request.getHeaders());
                });
            });

            runner.testGroup("setBody(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final MutableHttpRequest request = MutableHttpRequest.create();
                    final MutableHttpRequest setBodyResult = request.setBody((String)null).await();
                    test.assertSame(request, setBodyResult);
                    test.assertNull(request.getBody());
                    test.assertEqual(Iterable.create(), request.getHeaders());
                });

                runner.test("with empty", (Test test) ->
                {
                    final MutableHttpRequest request = MutableHttpRequest.create();
                    final MutableHttpRequest setBodyResult = request.setBody("").await();
                    test.assertSame(request, setBodyResult);
                    test.assertNull(request.getBody());
                    test.assertEqual(Iterable.create(), request.getHeaders());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final MutableHttpRequest request = MutableHttpRequest.create();
                    final MutableHttpRequest setBodyResult = request.setBody("hello").await();
                    test.assertSame(request, setBodyResult);
                    test.assertEqual("hello", CharacterReadStream.create(request.getBody()).readLine().await());
                    test.assertEqual(
                        Iterable.create(new HttpHeader("Content-Length", 5)),
                        request.getHeaders());
                });
            });
        });
    }
}
