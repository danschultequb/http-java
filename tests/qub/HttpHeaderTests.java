package qub;

public interface HttpHeaderTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(HttpHeader.class, () ->
        {
            runner.testGroup("constructor(String,String)", () ->
            {
                final Action3<String,String,Throwable> constructorErrorTest = (String name, String value, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Iterable.create(name, value).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        test.assertThrows(() -> new HttpHeader(name, value), expected);
                    });
                };

                constructorErrorTest.run(null, "V", new PreConditionFailure("name cannot be null."));
                constructorErrorTest.run("", "V", new PreConditionFailure("name cannot be empty."));
                constructorErrorTest.run("N", null, new PreConditionFailure("value cannot be null."));

                final Action2<String,String> constructorTest = (String name, String value) ->
                {
                    runner.test("with " + English.andList(Iterable.create(name, value).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        final HttpHeader header = new HttpHeader(name, value);
                        test.assertNotNull(header);
                        test.assertEqual(name, header.getName());
                        test.assertEqual(value, header.getValue());
                    });
                };

                constructorTest.run("N", "");
                constructorTest.run("N", "V");
                constructorTest.run("user-agent", "qub-browser");
            });

            runner.testGroup("equals(Object)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final HttpHeader header = new HttpHeader("apples", "fruit");
                    test.assertFalse(header.equals((Object)null));
                });

                runner.test("with different type", (Test test) ->
                {
                    final HttpHeader header = new HttpHeader("apples", "fruit");
                    test.assertFalse(header.equals((Object)"test"));
                });

                runner.test("with different header name", (Test test) ->
                {
                    final HttpHeader header = new HttpHeader("apples", "fruit");
                    test.assertFalse(header.equals((Object)new HttpHeader("oranges", "fruit")));
                });

                runner.test("with different header value", (Test test) ->
                {
                    final HttpHeader header = new HttpHeader("apples", "fruit");
                    test.assertFalse(header.equals((Object)new HttpHeader("apples", "yummy")));
                });

                runner.test("with same header", (Test test) ->
                {
                    final HttpHeader header = new HttpHeader("apples", "fruit");
                    test.assertTrue(header.equals((Object)header));
                });

                runner.test("with equal header", (Test test) ->
                {
                    final HttpHeader header = new HttpHeader("apples", "fruit");
                    test.assertTrue(header.equals((Object)new HttpHeader("apples", "fruit")));
                });
            });

            runner.testGroup("equals(HttpHeader)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final HttpHeader header = new HttpHeader("apples", "fruit");
                    test.assertFalse(header.equals((HttpHeader)null));
                });

                runner.test("with different header name", (Test test) ->
                {
                    final HttpHeader header = new HttpHeader("apples", "fruit");
                    test.assertFalse(header.equals(new HttpHeader("oranges", "fruit")));
                });

                runner.test("with different header value", (Test test) ->
                {
                    final HttpHeader header = new HttpHeader("apples", "fruit");
                    test.assertFalse(header.equals(new HttpHeader("apples", "yummy")));
                });

                runner.test("with same header", (Test test) ->
                {
                    final HttpHeader header = new HttpHeader("apples", "fruit");
                    test.assertTrue(header.equals(header));
                });

                runner.test("with equal header", (Test test) ->
                {
                    final HttpHeader header = new HttpHeader("apples", "fruit");
                    test.assertTrue(header.equals(new HttpHeader("apples", "fruit")));
                });
            });

            runner.test("toString()", (Test test) ->
            {
                final HttpHeader header = new HttpHeader("A", "B");
                test.assertEqual("A: B", header.toString());
            });
        });
    }
}
