package qub;

public interface HttpHeaderTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(HttpHeader.class, () ->
        {
            runner.testGroup("create(String,String)", () ->
            {
                final Action3<String,String,Throwable> createErrorTest = (String name, String value, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Iterable.create(name, value).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        test.assertThrows(() -> HttpHeader.create(name, value), expected);
                    });
                };

                createErrorTest.run(null, "V", new PreConditionFailure("name cannot be null."));
                createErrorTest.run("", "V", new PreConditionFailure("name cannot be empty."));
                createErrorTest.run("N", null, new PreConditionFailure("value cannot be null."));

                final Action2<String,String> createTest = (String name, String value) ->
                {
                    runner.test("with " + English.andList(Iterable.create(name, value).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        final HttpHeader header = HttpHeader.create(name, value);
                        test.assertNotNull(header);
                        test.assertEqual(name, header.getName());
                        test.assertEqual(value, header.getValue());
                    });
                };

                createTest.run("N", "");
                createTest.run("N", "V");
                createTest.run("user-agent", "qub-browser");
            });

            runner.testGroup("equals(Object)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final HttpHeader header = HttpHeader.create("apples", "fruit");
                    test.assertFalse(header.equals((Object)null));
                });

                runner.test("with different type", (Test test) ->
                {
                    final HttpHeader header = HttpHeader.create("apples", "fruit");
                    test.assertFalse(header.equals((Object)"test"));
                });

                runner.test("with different header name", (Test test) ->
                {
                    final HttpHeader header = HttpHeader.create("apples", "fruit");
                    test.assertFalse(header.equals((Object)HttpHeader.create("oranges", "fruit")));
                });

                runner.test("with different header value", (Test test) ->
                {
                    final HttpHeader header = HttpHeader.create("apples", "fruit");
                    test.assertFalse(header.equals((Object)HttpHeader.create("apples", "yummy")));
                });

                runner.test("with same header", (Test test) ->
                {
                    final HttpHeader header = HttpHeader.create("apples", "fruit");
                    test.assertTrue(header.equals((Object)header));
                });

                runner.test("with equal header", (Test test) ->
                {
                    final HttpHeader header = HttpHeader.create("apples", "fruit");
                    test.assertTrue(header.equals((Object)HttpHeader.create("apples", "fruit")));
                });
            });

            runner.testGroup("equals(HttpHeader)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final HttpHeader header = HttpHeader.create("apples", "fruit");
                    test.assertFalse(header.equals((HttpHeader)null));
                });

                runner.test("with different header name", (Test test) ->
                {
                    final HttpHeader header = HttpHeader.create("apples", "fruit");
                    test.assertFalse(header.equals(HttpHeader.create("oranges", "fruit")));
                });

                runner.test("with different header value", (Test test) ->
                {
                    final HttpHeader header = HttpHeader.create("apples", "fruit");
                    test.assertFalse(header.equals(HttpHeader.create("apples", "yummy")));
                });

                runner.test("with same header", (Test test) ->
                {
                    final HttpHeader header = HttpHeader.create("apples", "fruit");
                    test.assertTrue(header.equals(header));
                });

                runner.test("with equal header", (Test test) ->
                {
                    final HttpHeader header = HttpHeader.create("apples", "fruit");
                    test.assertTrue(header.equals(HttpHeader.create("apples", "fruit")));
                });
            });

            runner.test("toString()", (Test test) ->
            {
                final HttpHeader header = HttpHeader.create("A", "B");
                test.assertEqual("A: B", header.toString());
            });
        });
    }
}
