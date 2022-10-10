package qub;

public interface MutableHttpRequestTests
{
    public static void test(TestRunner runner)
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
                test.assertEqual(HttpHeaders.create(), request.getHeaders());
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

            runner.testGroup("setUrl(URL)", () ->
            {
                final Action2<URL,Throwable> setUrlErrorTest = (URL url, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(url), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        test.assertThrows(() -> request.setUrl(url), expected);
                        test.assertNull(request.getURL());
                    });
                };

                setUrlErrorTest.run(
                    null,
                    new PreConditionFailure("url cannot be null."));
                setUrlErrorTest.run(
                    URL.parse("www.google.com").await(),
                    new PreConditionFailure("url.hasScheme() cannot be false."));

                final Action1<URL> setUrlTest = (URL url) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(url), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        final MutableHttpRequest setUrlResult = request.setUrl(url);
                        test.assertSame(request, setUrlResult);
                        test.assertEqual(url, request.getURL());
                    });
                };

                setUrlTest.run(URL.parse("http://www.google.com").await());
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
                        test.assertEqual(HttpHeaders.create(), request.getHeaders());
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
                        test.assertEqual(HttpHeaders.create().set(headerName, headerValue), request.getHeaders());
                        test.assertEqual(HttpHeader.create(headerName, headerValue), request.getHeader(headerName).await());
                        test.assertEqual(headerValue, request.getHeaderValue(headerName).await());
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
                        test.assertEqual(HttpHeaders.create().set(headerName, headerValue), request.getHeaders());
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
                        test.assertEqual(HttpHeaders.create().set(headerName, headerValue), request.getHeaders());
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
                        test.assertEqual(HttpHeaders.create(), request.getHeaders());
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
                    test.assertEqual(HttpHeaders.create(), request.getHeaders());
                });

                runner.test("with 3 contentLength and non-null body", (Test test) ->
                {
                    final MutableHttpRequest request = MutableHttpRequest.create();
                    request.setBody(3, new InMemoryByteStream(new byte[] { 0, 1, 2 }));
                    test.assertNotNull(request.getBody());
                    test.assertEqual(
                        Iterable.create(HttpHeader.create("Content-Length", 3)),
                        request.getHeaders());
                });
            });

            runner.testGroup("setAuthorizationHeader(String)", () ->
            {
                final Action2<String,Throwable> setAuthorizationErrorTest = (String authorization, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(authorization), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        test.assertThrows(() -> request.setAuthorizationHeader(authorization),
                            expected);
                        test.assertThrows(() -> request.getAuthorizationHeaderValue().await(),
                            new NotFoundException("No \"Authorization\" header found."));
                        test.assertThrows(() -> request.getAuthorizationBearerHeaderValue().await(),
                            new NotFoundException("No \"Authorization\" header found."));
                        test.assertThrows(() -> request.getAuthorizationTokenHeaderValue().await(),
                            new NotFoundException("No \"Authorization\" header found."));
                        test.assertEqual(HttpHeaders.create(), request.getHeaders());
                    });
                };

                setAuthorizationErrorTest.run(null, new PreConditionFailure("authorizationHeaderValue cannot be null."));
                setAuthorizationErrorTest.run("", new PreConditionFailure("authorizationHeaderValue cannot be empty."));

                final Action1<String> setAuthorizationTest = (String authorization) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(authorization), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        final MutableHttpRequest setAuthorizationResult = request.setAuthorizationHeader(authorization);
                        test.assertSame(request, setAuthorizationResult);
                        test.assertEqual(authorization, request.getAuthorizationHeaderValue().await());
                    });
                };

                setAuthorizationTest.run("abcdef");
                setAuthorizationTest.run("token hello");
                setAuthorizationTest.run("bearer there");
            });

            runner.testGroup("setAuthorizationBearer(String)", () ->
            {
                final Action2<String,Throwable> setAuthorizationBearerErrorTest = (String authorizationBearer, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(authorizationBearer), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        test.assertThrows(() -> request.setAuthorizationBearerHeader(authorizationBearer),
                            expected);
                        test.assertThrows(() -> request.getAuthorizationHeaderValue().await(),
                            new NotFoundException("No \"Authorization\" header found."));
                        test.assertThrows(() -> request.getAuthorizationBearerHeaderValue().await(),
                            new NotFoundException("No \"Authorization\" header found."));
                        test.assertThrows(() -> request.getAuthorizationTokenHeaderValue().await(),
                            new NotFoundException("No \"Authorization\" header found."));
                        test.assertEqual(HttpHeaders.create(), request.getHeaders());
                    });
                };

                setAuthorizationBearerErrorTest.run(null, new PreConditionFailure("authorizationBearer cannot be null."));
                setAuthorizationBearerErrorTest.run("", new PreConditionFailure("authorizationBearer cannot be empty."));

                final Action1<String> setAuthorizationBearerTest = (String authorizationBearer) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(authorizationBearer), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        final MutableHttpRequest setAuthorizationResult = request.setAuthorizationBearerHeader(authorizationBearer);
                        test.assertSame(request, setAuthorizationResult);
                        test.assertEqual("Bearer " + authorizationBearer, request.getAuthorizationHeaderValue().await());
                        test.assertEqual(authorizationBearer, request.getAuthorizationBearerHeaderValue().await());
                        test.assertThrows(() -> request.getAuthorizationTokenHeaderValue().await(),
                            new NotFoundException("No \"Authorization\" header found with a \"Token \" prefix."));
                    });
                };

                setAuthorizationBearerTest.run("abcdef");
                setAuthorizationBearerTest.run("hello");
                setAuthorizationBearerTest.run("there");
            });

            runner.testGroup("setAuthorizationToken(String)", () ->
            {
                final Action2<String,Throwable> setAuthorizationTokenErrorTest = (String authorizationToken, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(authorizationToken), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        test.assertThrows(() -> request.setAuthorizationTokenHeader(authorizationToken),
                            expected);
                        test.assertThrows(() -> request.getAuthorizationHeaderValue().await(),
                            new NotFoundException("No \"Authorization\" header found."));
                        test.assertThrows(() -> request.getAuthorizationBearerHeaderValue().await(),
                            new NotFoundException("No \"Authorization\" header found."));
                        test.assertThrows(() -> request.getAuthorizationTokenHeaderValue().await(),
                            new NotFoundException("No \"Authorization\" header found."));
                        test.assertEqual(HttpHeaders.create(), request.getHeaders());
                    });
                };

                setAuthorizationTokenErrorTest.run(null, new PreConditionFailure("authorizationToken cannot be null."));
                setAuthorizationTokenErrorTest.run("", new PreConditionFailure("authorizationToken cannot be empty."));

                final Action1<String> setAuthorizationTokenTest = (String authorizationToken) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(authorizationToken), (Test test) ->
                    {
                        final MutableHttpRequest request = MutableHttpRequest.create();
                        final MutableHttpRequest setAuthorizationResult = request.setAuthorizationTokenHeader(authorizationToken);
                        test.assertSame(request, setAuthorizationResult);
                        test.assertEqual("Token " + authorizationToken, request.getAuthorizationHeaderValue().await());
                        test.assertThrows(() -> request.getAuthorizationBearerHeaderValue().await(),
                            new NotFoundException("No \"Authorization\" header found with a \"Bearer \" prefix."));
                        test.assertEqual(authorizationToken, request.getAuthorizationTokenHeaderValue().await());
                    });
                };

                setAuthorizationTokenTest.run("abcdef");
                setAuthorizationTokenTest.run("hello");
                setAuthorizationTokenTest.run("there");
            });

            runner.testGroup("setBody(byte[])", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final MutableHttpRequest request = MutableHttpRequest.create();
                    final MutableHttpRequest setBodyResult = request.setBody((byte[])null);
                    test.assertSame(request, setBodyResult);
                    test.assertNull(request.getBody());
                    test.assertEqual(HttpHeaders.create(), request.getHeaders());
                });

                runner.test("with empty", (Test test) ->
                {
                    final MutableHttpRequest request = MutableHttpRequest.create();
                    final MutableHttpRequest setBodyResult = request.setBody(new byte[0]);
                    test.assertSame(request, setBodyResult);
                    test.assertNull(request.getBody());
                    test.assertEqual(HttpHeaders.create(), request.getHeaders());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final MutableHttpRequest request = MutableHttpRequest.create();
                    final MutableHttpRequest setBodyResult = request.setBody(new byte[] { 0, 1, 2, 3, 4 });
                    test.assertSame(request, setBodyResult);
                    test.assertEqual(new byte[] { 0, 1, 2, 3, 4 }, request.getBody().readAllBytes().await());
                    test.assertEqual(
                        HttpHeaders.create().set(HttpHeader.ContentLengthName, 5),
                        request.getHeaders());
                    test.assertEqual(5, request.getContentLength().await());
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
                        Iterable.create(HttpHeader.create("Content-Length", 5)),
                        request.getHeaders());
                    test.assertEqual(5, request.getContentLength().await());
                });
            });
        });
    }
}
