package qub;

public interface MutableHttpHeadersTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(MutableHttpHeaders.class, () ->
        {
            runner.test("constructor()", (Test test) ->
            {
                final MutableHttpHeaders headers = new MutableHttpHeaders();
                test.assertFalse(headers.any());
                test.assertEqual(0, headers.getCount());
            });

            runner.testGroup("constuctor(Iterable<HttpHeader>)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders(null);
                    test.assertEqual(0, headers.getCount());
                });

                runner.test("with empty", (Test test) ->
                {
                    final List<HttpHeader> headerList = List.create();
                    final MutableHttpHeaders headers = new MutableHttpHeaders(headerList);
                    test.assertEqual(0, headers.getCount());

                    headerList.add(new HttpHeader("a", "b"));
                    test.assertEqual(1, headerList.getCount());
                    test.assertEqual(0, headers.getCount(), "The MutableHttpHeaders should not be bound to the Iterable that was passed to its constructor.");
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final List<HttpHeader> headerList = List.create(
                        new HttpHeader("a", "A"),
                        new HttpHeader("b", "B"));
                    final MutableHttpHeaders headers = new MutableHttpHeaders(headerList);
                    test.assertEqual(2, headers.getCount());
                    test.assertEqual(new HttpHeader("a", "A"), headers.get("a").await());
                    test.assertEqual(new HttpHeader("b", "B"), headers.get("b").await());

                    headerList.add(new HttpHeader("c", "C"));
                    test.assertEqual(3, headerList.getCount());
                    test.assertEqual(2, headers.getCount(), "The MutableHttpHeaders should not be bound to the Iterable that was passed to its constructor.");
                });
            });

            runner.testGroup("clear()", () ->
            {
                runner.test("when empty", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    headers.clear();
                    test.assertEqual(0, headers.getCount());
                });

                runner.test("when not empty", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    headers.set("a", "b");
                    test.assertEqual(1, headers.getCount());
                    headers.clear();
                    test.assertEqual(0, headers.getCount());
                });
            });

            runner.testGroup("set(HttpHeader)", () ->
            {
                runner.test("with null header", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.set(null), new PreConditionFailure("header cannot be null."));
                });

                runner.test("with non-null header", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    headers.set(new HttpHeader("header-name", "header-value"));
                    test.assertEqual("header-value", headers.get("header-name").await().getValue());
                });
            });

            runner.testGroup("set(String,String)", () ->
            {
                runner.test("with null header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.set(null, "header-value"), new PreConditionFailure("headerName cannot be null."));
                });

                runner.test("with empty header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.set("", "header-value"), new PreConditionFailure("headerName cannot be empty."));
                });

                runner.test("with null header value", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.set("header-name", null), new PreConditionFailure("headerValue cannot be null."));
                    test.assertFalse(headers.contains("header-name"));
                });

                runner.test("with empty header value", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.set("header-name", ""), new PreConditionFailure("headerValue cannot be empty."));
                    test.assertFalse(headers.contains("header-name"));
                });

                runner.test("with non-existing header", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    headers.set("header-name", "header-value");
                    test.assertEqual("header-value", headers.get("header-name").await().getValue());
                });

                runner.test("with existing header", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    headers.set("header-name", "header-value-1");

                    headers.set("header-name", "header-value-2");

                    test.assertEqual("header-value-2", headers.get("header-name").await().getValue());
                });
            });

            runner.testGroup("set(String,int)", () ->
            {
                runner.test("with null header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.set(null, 1), new PreConditionFailure("headerName cannot be null."));
                });

                runner.test("with empty header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.set("", 2), new PreConditionFailure("headerName cannot be empty."));
                });

                runner.test("with non-existing header", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    headers.set("header-name", 3);
                    test.assertEqual("3", headers.get("header-name").await().getValue());
                });

                runner.test("with existing header", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
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
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.set(null, 1L), new PreConditionFailure("headerName cannot be null."));
                });

                runner.test("with empty header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.set("", 2L), new PreConditionFailure("headerName cannot be empty."));
                });

                runner.test("with non-existing header", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    headers.set("header-name", 3L);
                    test.assertEqual("3", headers.get("header-name").await().getValue());
                });

                runner.test("with existing header", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
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
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.contains((String)null), new PreConditionFailure("headerName cannot be null."));
                });

                runner.test("with empty", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.contains(""), new PreConditionFailure("headerName cannot be empty."));
                });

                runner.test("with non-existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertFalse(headers.contains("abc"));
                });

                runner.test("with existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    headers.set("abc", 20);
                    test.assertTrue(headers.contains("abc"));
                });

                runner.test("with different-cased existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    headers.set("abc", 20);
                    test.assertTrue(headers.contains("ABC"));
                });
            });

            runner.testGroup("get(String)", () ->
            {
                runner.test("with null header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.get(null), new PreConditionFailure("headerName cannot be null."));
                });

                runner.test("with empty header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.get(""), new PreConditionFailure("headerName cannot be empty."));
                });

                runner.test("with non-existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.get("header-name").await(), new NotFoundException("header-name"));
                });

                runner.test("with existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    headers.set("header-name", "header-value");
                    test.assertEqual(new HttpHeader("header-name", "header-value"), headers.get("header-name").await());
                });

                runner.test("with different case of existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    headers.set("header-name", "header-value");
                    test.assertEqual(new HttpHeader("header-name", "header-value"), headers.get("HEADER-NAME").await());
                });
            });

            runner.testGroup("getValue(String)", () ->
            {
                runner.test("with null header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.getValue(null), new PreConditionFailure("headerName cannot be null."));
                });

                runner.test("with empty header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.getValue(""), new PreConditionFailure("headerName cannot be empty."));
                });

                runner.test("with non-existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.getValue("header-name").await(), new NotFoundException("header-name"));
                });

                runner.test("with existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    headers.set("header-name", "header-value");
                    test.assertEqual("header-value", headers.getValue("header-name").await());
                });

                runner.test("with different case of existing header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    headers.set("header-name", "header-value");
                    test.assertEqual("header-value", headers.getValue("HEADER-NAME").await());
                });
            });

            runner.testGroup("remove(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.remove(null), new PreConditionFailure("headerName cannot be null."));
                });

                runner.test("with empty", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.remove(""), new PreConditionFailure("headerName cannot be empty."));
                });

                runner.test("with not found header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    test.assertThrows(() -> headers.remove("A").await(), new NotFoundException("A"));
                });

                runner.test("with found header name", (Test test) ->
                {
                    final MutableHttpHeaders headers = new MutableHttpHeaders();
                    headers.set("A", "B");
                    test.assertEqual(new HttpHeader("A", "B"), headers.remove("A").await());
                    test.assertThrows(() -> headers.remove("A").await(), new NotFoundException("A"));
                });
            });
        });
    }
}
