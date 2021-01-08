package qub;

public interface MutableHttpResponseTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(MutableHttpResponse.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final MutableHttpResponse httpResponse = MutableHttpResponse.create();
                test.assertNotNull(httpResponse);
                test.assertFalse(httpResponse.isDisposed());
                test.assertEqual(0, httpResponse.getStatusCode());
                test.assertNull(httpResponse.getReasonPhrase());
                test.assertNull(httpResponse.getHttpVersion());
                test.assertEqual(HttpHeaders.create(), httpResponse.getHeaders());
                try (final ByteReadStream body = httpResponse.getBody())
                {
                    test.assertNotNull(body);
                    test.assertFalse(body.isDisposed());
                    test.assertEqual(new byte[0], body.readAllBytes().await());
                }
            });

            runner.testGroup("setHttpVersion(String)", () ->
            {
                final Action2<String,Throwable> setHttpVersionErrorTest = (String httpVersion, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(httpVersion), (Test test) ->
                    {
                        final MutableHttpResponse httpResponse = MutableHttpResponse.create();
                        test.assertThrows(() -> httpResponse.setHttpVersion(httpVersion), expected);
                        test.assertNull(httpResponse.getHttpVersion());
                    });
                };

                setHttpVersionErrorTest.run(null, new PreConditionFailure("httpVersion cannot be null."));
                setHttpVersionErrorTest.run("", new PreConditionFailure("httpVersion cannot be empty."));

                final Action1<String> setHttpVersionTest = (String httpVersion) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(httpVersion), (Test test) ->
                    {
                        final MutableHttpResponse httpResponse = MutableHttpResponse.create();
                        final MutableHttpResponse setHttpVersionResult = httpResponse.setHttpVersion(httpVersion);
                        test.assertSame(httpResponse, setHttpVersionResult);
                        test.assertEqual(httpVersion, httpResponse.getHttpVersion());
                    });
                };

                setHttpVersionTest.run("HTTP/1.1");
                setHttpVersionTest.run("HTTP/2.0");
                setHttpVersionTest.run("apples");
            });

            runner.testGroup("setStatusCode(int)", () ->
            {
                final Action1<Integer> setStatusCodeTest = (Integer statusCode) ->
                {
                    runner.test("with " + statusCode, (Test test) ->
                    {
                        final MutableHttpResponse httpResponse = MutableHttpResponse.create();
                        final MutableHttpResponse setStatusCodeResult = httpResponse.setStatusCode(statusCode);
                        test.assertSame(httpResponse, setStatusCodeResult);
                        test.assertEqual(statusCode, httpResponse.getStatusCode());
                    });
                };

                setStatusCodeTest.run(-1);
                setStatusCodeTest.run(0);
                setStatusCodeTest.run(200);
                setStatusCodeTest.run(404);
                setStatusCodeTest.run(500);
                setStatusCodeTest.run(1000);
            });

            runner.testGroup("setReasonPhrase(String)", () ->
            {
                final Action1<String> setReasonPhraseTest = (String reasonPhrase) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(reasonPhrase), (Test test) ->
                    {
                        final MutableHttpResponse httpResponse = MutableHttpResponse.create();
                        final MutableHttpResponse setReasonPhraseResult = httpResponse.setReasonPhrase(reasonPhrase);
                        test.assertSame(httpResponse, setReasonPhraseResult);
                        test.assertEqual(reasonPhrase, httpResponse.getReasonPhrase());
                    });
                };

                setReasonPhraseTest.run(null);
                setReasonPhraseTest.run("");
                setReasonPhraseTest.run("OK");
                setReasonPhraseTest.run("hello there");
            });

            runner.testGroup("setHeaders(Iterable<HttpHeader>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final MutableHttpResponse httpResponse = MutableHttpResponse.create();
                    test.assertThrows(() -> httpResponse.setHeaders(null),
                        new PreConditionFailure("headers cannot be null."));
                    test.assertEqual(Iterable.create(), httpResponse.getHeaders());
                });

                final Action3<MutableHttpResponse,HttpHeaders,HttpHeaders> setHeadersTest = (MutableHttpResponse httpResponse, HttpHeaders headers, HttpHeaders expected) ->
                {
                    runner.test("with " + httpResponse.getHeaders() + " and " + headers, (Test test) ->
                    {
                        final MutableHttpResponse setHeadersResult = httpResponse.setHeaders(headers);
                        test.assertSame(httpResponse, setHeadersResult);
                        test.assertEqual(expected, httpResponse.getHeaders());
                    });
                };

                setHeadersTest.run(
                    MutableHttpResponse.create(),
                    HttpHeaders.create(),
                    HttpHeaders.create());
                setHeadersTest.run(
                    MutableHttpResponse.create()
                        .setHeader("a", "b"),
                    HttpHeaders.create(),
                    HttpHeaders.create()
                        .set("a", "b"));
                setHeadersTest.run(
                    MutableHttpResponse.create(),
                    HttpHeaders.create()
                        .set("a", "b"),
                    HttpHeaders.create()
                        .set("a", "b"));
                setHeadersTest.run(
                    MutableHttpResponse.create()
                        .setHeader("a", "b"),
                    HttpHeaders.create()
                        .set("c", "d"),
                    HttpHeaders.create()
                        .set("a", "b")
                        .set("c", "d"));
                setHeadersTest.run(
                    MutableHttpResponse.create()
                        .setHeader("c", "d"),
                    HttpHeaders.create()
                        .set("a", "b"),
                    HttpHeaders.create()
                        .set("c", "d")
                        .set("a", "b"));
                setHeadersTest.run(
                    MutableHttpResponse.create()
                        .setHeader("a", "b"),
                    HttpHeaders.create()
                        .set("a", "c"),
                    HttpHeaders.create()
                        .set("a", "c"));
            });

            runner.testGroup("setHeader(HttpHeader)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final MutableHttpResponse httpResponse = MutableHttpResponse.create();
                    test.assertThrows(() -> httpResponse.setHeader(null),
                        new PreConditionFailure("header cannot be null."));
                    test.assertEqual(Iterable.create(), httpResponse.getHeaders());
                });

                final Action3<MutableHttpResponse,HttpHeader,HttpHeaders> setHeadersTest = (MutableHttpResponse httpResponse, HttpHeader header, HttpHeaders expected) ->
                {
                    runner.test("with " + English.andList(httpResponse.getHeaders(), header), (Test test) ->
                    {
                        final MutableHttpResponse setHeadersResult = httpResponse.setHeader(header);
                        test.assertSame(httpResponse, setHeadersResult);
                        test.assertEqual(header, httpResponse.getHeader(header.getName()).await());
                        test.assertEqual(expected, httpResponse.getHeaders());
                    });
                };

                setHeadersTest.run(
                    MutableHttpResponse.create(),
                    HttpHeader.create("a", "b"),
                    HttpHeaders.create()
                        .set("a", "b"));
                setHeadersTest.run(
                    MutableHttpResponse.create()
                        .setHeader("a", "b"),
                    HttpHeader.create("b", "c"),
                    HttpHeaders.create()
                        .set("a", "b")
                        .set("b", "c"));
                setHeadersTest.run(
                    MutableHttpResponse.create()
                        .setHeader("c", "d"),
                    HttpHeader.create("a", "b"),
                    HttpHeaders.create()
                        .set("c", "d")
                        .set("a", "b"));
                setHeadersTest.run(
                    MutableHttpResponse.create()
                        .setHeader("a", "b"),
                    HttpHeader.create("a", "c"),
                    HttpHeaders.create()
                        .set("a", "c"));
            });

            runner.testGroup("setHeader(String,String)", () ->
            {
                final Action3<String,String,Throwable> setHeaderErrorTest = (String headerName, String headerValue, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Iterable.create(headerName, headerValue).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        final MutableHttpResponse httpResponse = MutableHttpResponse.create();
                        test.assertThrows(() -> httpResponse.setHeader(headerName, headerValue), expected);
                        test.assertEqual(HttpHeaders.create(), httpResponse.getHeaders());
                    });
                };

                setHeaderErrorTest.run(null, null, new PreConditionFailure("headerName cannot be null."));
                setHeaderErrorTest.run("", null, new PreConditionFailure("headerName cannot be empty."));
                setHeaderErrorTest.run("a", null, new PreConditionFailure("headerValue cannot be null."));

                final Action4<MutableHttpResponse,String,String,HttpHeaders> setHeadersTest = (MutableHttpResponse httpResponse, String headerName, String headerValue, HttpHeaders expected) ->
                {
                    runner.test("with " + English.andList(httpResponse.getHeaders(), Strings.escapeAndQuote(headerName), Strings.escapeAndQuote(headerValue)), (Test test) ->
                    {
                        final MutableHttpResponse setHeadersResult = httpResponse.setHeader(headerName, headerValue);
                        test.assertSame(httpResponse, setHeadersResult);
                        test.assertEqual(expected, httpResponse.getHeaders());
                    });
                };

                setHeadersTest.run(
                    MutableHttpResponse.create(),
                    "a",
                    "b",
                    HttpHeaders.create()
                        .set("a", "b"));
                setHeadersTest.run(
                    MutableHttpResponse.create()
                        .setHeader("a", "b"),
                    "b",
                    "c",
                    HttpHeaders.create()
                        .set("a", "b")
                        .set("b", "c"));
                setHeadersTest.run(
                    MutableHttpResponse.create()
                        .setHeader("c", "d"),
                    "a",
                    "b",
                    HttpHeaders.create()
                        .set("c", "d")
                        .set("a", "b"));
                setHeadersTest.run(
                    MutableHttpResponse.create()
                        .setHeader("a", "b"),
                    "a",
                    "c",
                    HttpHeaders.create()
                        .set("a", "c"));
            });

            runner.testGroup("setHeader(String,int)", () ->
            {
                final Action3<String,Integer,Throwable> setHeaderErrorTest = (String headerName, Integer headerValue, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(headerName), headerValue), (Test test) ->
                    {
                        final MutableHttpResponse httpResponse = MutableHttpResponse.create();
                        test.assertThrows(() -> httpResponse.setHeader(headerName, headerValue), expected);
                        test.assertEqual(HttpHeaders.create(), httpResponse.getHeaders());
                    });
                };

                setHeaderErrorTest.run(null, 1, new PreConditionFailure("headerName cannot be null."));
                setHeaderErrorTest.run("", 1, new PreConditionFailure("headerName cannot be empty."));

                final Action4<MutableHttpResponse,String,Integer,HttpHeaders> setHeadersTest = (MutableHttpResponse httpResponse, String headerName, Integer headerValue, HttpHeaders expected) ->
                {
                    runner.test("with " + English.andList(httpResponse.getHeaders(), Strings.escapeAndQuote(headerName), headerValue), (Test test) ->
                    {
                        final MutableHttpResponse setHeadersResult = httpResponse.setHeader(headerName, headerValue);
                        test.assertSame(httpResponse, setHeadersResult);
                        test.assertEqual(expected, httpResponse.getHeaders());
                    });
                };

                setHeadersTest.run(
                    MutableHttpResponse.create(),
                    "a",
                    1,
                    HttpHeaders.create()
                        .set("a", "1"));
                setHeadersTest.run(
                    MutableHttpResponse.create()
                        .setHeader("a", "b"),
                    "b",
                    2,
                    HttpHeaders.create()
                        .set("a", "b")
                        .set("b", "2"));
                setHeadersTest.run(
                    MutableHttpResponse.create()
                        .setHeader("c", "d"),
                    "a",
                    3,
                    HttpHeaders.create()
                        .set("c", "d")
                        .set("a", 3));
                setHeadersTest.run(
                    MutableHttpResponse.create()
                        .setHeader("a", "b"),
                    "a",
                    4,
                    HttpHeaders.create()
                        .set("a", "4"));
            });

            runner.testGroup("setHeader(String,long)", () ->
            {
                final Action3<String,Long,Throwable> setHeaderErrorTest = (String headerName, Long headerValue, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Strings.escapeAndQuote(headerName), headerValue), (Test test) ->
                    {
                        final MutableHttpResponse httpResponse = MutableHttpResponse.create();
                        test.assertThrows(() -> httpResponse.setHeader(headerName, headerValue), expected);
                        test.assertEqual(HttpHeaders.create(), httpResponse.getHeaders());
                    });
                };

                setHeaderErrorTest.run(null, 1L, new PreConditionFailure("headerName cannot be null."));
                setHeaderErrorTest.run("", 1L, new PreConditionFailure("headerName cannot be empty."));

                final Action4<MutableHttpResponse,String,Long,HttpHeaders> setHeadersTest = (MutableHttpResponse httpResponse, String headerName, Long headerValue, HttpHeaders expected) ->
                {
                    runner.test("with " + English.andList(httpResponse.getHeaders(), Strings.escapeAndQuote(headerName), headerValue), (Test test) ->
                    {
                        final MutableHttpResponse setHeadersResult = httpResponse.setHeader(headerName, headerValue);
                        test.assertSame(httpResponse, setHeadersResult);
                        test.assertEqual(expected, httpResponse.getHeaders());
                    });
                };

                setHeadersTest.run(
                    MutableHttpResponse.create(),
                    "a",
                    1L,
                    HttpHeaders.create()
                        .set("a", "1"));
                setHeadersTest.run(
                    MutableHttpResponse.create()
                        .setHeader("a", "b"),
                    "b",
                    2L,
                    HttpHeaders.create()
                        .set("a", "b")
                        .set("b", "2"));
                setHeadersTest.run(
                    MutableHttpResponse.create()
                        .setHeader("c", "d"),
                    "a",
                    3L,
                    HttpHeaders.create()
                        .set("c", "d")
                        .set("a", "3"));
                setHeadersTest.run(
                    MutableHttpResponse.create()
                        .setHeader("a", "b"),
                    "a",
                    4L,
                    HttpHeaders.create()
                        .set("a", "4"));
            });

            runner.testGroup("setBody(ByteReadStream)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final MutableHttpResponse httpResponse = MutableHttpResponse.create();
                    final ByteReadStream body = httpResponse.getBody();
                    test.assertThrows(() -> httpResponse.setBody((ByteReadStream)null),
                        new PreConditionFailure("body cannot be null."));
                    test.assertSame(body, httpResponse.getBody());
                });

                runner.test("with non-null", (Test test) ->
                {
                    final MutableHttpResponse httpResponse = MutableHttpResponse.create();
                    final InMemoryByteStream body = InMemoryByteStream.create();
                    final MutableHttpResponse setBodyResult = httpResponse.setBody(body);
                    test.assertSame(httpResponse, setBodyResult);
                    test.assertSame(body, httpResponse.getBody());
                });
            });

            runner.testGroup("setBody(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final MutableHttpResponse httpResponse = MutableHttpResponse.create();
                    final ByteReadStream body = httpResponse.getBody();
                    test.assertThrows(() -> httpResponse.setBody((String)null),
                        new PreConditionFailure("body cannot be null."));
                    test.assertSame(body, httpResponse.getBody());
                });

                final Action1<String> setBodyTest = (String body) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(body), (Test test) ->
                    {
                        final MutableHttpResponse httpResponse = MutableHttpResponse.create();
                        final MutableHttpResponse setBodyResult = httpResponse.setBody(body);
                        test.assertSame(httpResponse, setBodyResult);
                        test.assertEqual(body, CharacterReadStream.create(httpResponse.getBody()).readEntireString().catchError(EndOfStreamException.class, () -> "").await());
                    });
                };

                setBodyTest.run("");
                setBodyTest.run("hello");
            });

            runner.test("dispose()", (Test test) ->
            {
                final MutableHttpResponse httpResponse = MutableHttpResponse.create();

                test.assertTrue(httpResponse.dispose().await());
                test.assertTrue(httpResponse.isDisposed());
                test.assertTrue(httpResponse.getBody().isDisposed());

                test.assertFalse(httpResponse.dispose().await());
                test.assertTrue(httpResponse.isDisposed());
                test.assertTrue(httpResponse.getBody().isDisposed());
            });
        });
    }
}
