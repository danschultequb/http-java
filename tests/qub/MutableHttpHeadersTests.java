package qub;

public interface MutableHttpHeadersTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(MutableHttpHeaders.class, () ->
        {
            runner.test("constructor()", (Test test) ->
            {
                final MutableHttpHeaders headers = MutableHttpHeaders.create();
                test.assertFalse(headers.any());
                test.assertEqual(0, headers.getCount());
                test.assertEqual("[]", headers.toString());
            });

            runner.testGroup("clear()", () ->
            {
                runner.test("when empty", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    final MutableHttpHeaders clearResult = headers.clear();
                    test.assertSame(headers, clearResult);
                    test.assertEqual(0, headers.getCount());
                });

                runner.test("when not empty", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create().set("a", "b");
                    test.assertEqual(1, headers.getCount());
                    final MutableHttpHeaders clearResult = headers.clear();
                    test.assertSame(headers, clearResult);
                    test.assertEqual(0, headers.getCount());
                });
            });

            runner.testGroup("set(HttpHeader)", () ->
            {
                runner.test("with null header", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.set(null),
                        new PreConditionFailure("header cannot be null."));
                });

                runner.test("with non-null header", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    final MutableHttpHeaders setResult = headers.set(HttpHeader.create("header-name", "header-value"));
                    test.assertSame(headers, setResult);
                    test.assertEqual("header-value", headers.get("header-name").await().getValue());
                });
            });

            runner.testGroup("set(String,String)", () ->
            {
                runner.test("with null header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.set(null, "header-value"),
                        new PreConditionFailure("headerName cannot be null."));
                });

                runner.test("with empty header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.set("", "header-value"),
                        new PreConditionFailure("headerName cannot be empty."));
                });

                runner.test("with null header value", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.set("header-name", null),
                        new PreConditionFailure("headerValue cannot be null."));
                    test.assertFalse(headers.contains("header-name"));
                });

                runner.test("with empty header value", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    final MutableHttpHeaders setResult = headers.set("header-name", "");
                    test.assertSame(headers, setResult);
                    test.assertEqual(HttpHeader.create("header-name", ""), headers.get("header-name").await());
                });

                runner.test("with non-existing header", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    headers.set("header-name", "header-value");
                    test.assertEqual("header-value", headers.get("header-name").await().getValue());
                });

                runner.test("with existing header", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    headers.set("header-name", "header-value-1");

                    headers.set("header-name", "header-value-2");

                    test.assertEqual("header-value-2", headers.get("header-name").await().getValue());
                });
            });

            runner.testGroup("set(String,int)", () ->
            {
                runner.test("with null header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.set(null, 1), new PreConditionFailure("headerName cannot be null."));
                });

                runner.test("with empty header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.set("", 2), new PreConditionFailure("headerName cannot be empty."));
                });

                runner.test("with non-existing header", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    headers.set("header-name", 3);
                    test.assertEqual("3", headers.get("header-name").await().getValue());
                });

                runner.test("with existing header", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    headers.set("header-name", 4);
                    test.assertEqual("4", headers.get("header-name").await().getValue());

                    headers.set("header-name", 5);
                    test.assertEqual("5", headers.get("header-name").await().getValue());
                });
            });

            runner.testGroup("set(String,long)", () ->
            {
                runner.test("with null header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.set(null, 1L), new PreConditionFailure("headerName cannot be null."));
                });

                runner.test("with empty header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.set("", 2L), new PreConditionFailure("headerName cannot be empty."));
                });

                runner.test("with non-existing header", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    headers.set("header-name", 3L);
                    test.assertEqual("3", headers.get("header-name").await().getValue());
                });

                runner.test("with existing header", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    headers.set("header-name", 4L);
                    test.assertEqual("4", headers.get("header-name").await().getValue());

                    headers.set("header-name", 5L);
                    test.assertEqual("5", headers.get("header-name").await().getValue());
                });
            });

            runner.testGroup("contains(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.contains((String)null), new PreConditionFailure("headerName cannot be null."));
                });

                runner.test("with empty", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.contains(""), new PreConditionFailure("headerName cannot be empty."));
                });

                runner.test("with non-existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertFalse(headers.contains("abc"));
                });

                runner.test("with existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    headers.set("abc", 20);
                    test.assertTrue(headers.contains("abc"));
                });

                runner.test("with different-cased existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    headers.set("abc", 20);
                    test.assertTrue(headers.contains("ABC"));
                });
            });

            runner.testGroup("get(String)", () ->
            {
                runner.test("with null header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.get(null), new PreConditionFailure("headerName cannot be null."));
                });

                runner.test("with empty header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.get(""), new PreConditionFailure("headerName cannot be empty."));
                });

                runner.test("with non-existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.get("header-name").await(), new NotFoundException("header-name"));
                });

                runner.test("with existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    headers.set("header-name", "header-value");
                    test.assertEqual(HttpHeader.create("header-name", "header-value"), headers.get("header-name").await());
                });

                runner.test("with different case of existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    headers.set("header-name", "header-value");
                    test.assertEqual(HttpHeader.create("header-name", "header-value"), headers.get("HEADER-NAME").await());
                });
            });

            runner.testGroup("getValue(String)", () ->
            {
                runner.test("with null header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.getValue(null), new PreConditionFailure("headerName cannot be null."));
                });

                runner.test("with empty header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.getValue(""), new PreConditionFailure("headerName cannot be empty."));
                });

                runner.test("with non-existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.getValue("header-name").await(), new NotFoundException("header-name"));
                });

                runner.test("with existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    headers.set("header-name", "header-value");
                    test.assertEqual("header-value", headers.getValue("header-name").await());
                });

                runner.test("with different case of existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    headers.set("header-name", "header-value");
                    test.assertEqual("header-value", headers.getValue("HEADER-NAME").await());
                });
            });

            runner.testGroup("remove(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.remove(null), new PreConditionFailure("headerName cannot be null."));
                });

                runner.test("with empty", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.remove(""), new PreConditionFailure("headerName cannot be empty."));
                });

                runner.test("with not found header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    test.assertThrows(() -> headers.remove("A").await(), new NotFoundException("A"));
                });

                runner.test("with found header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = MutableHttpHeaders.create();
                    headers.set("A", "B");
                    test.assertEqual(HttpHeader.create("A", "B"), headers.remove("A").await());
                    test.assertThrows(() -> headers.remove("A").await(), new NotFoundException("A"));
                });
            });
        });
    }
}
