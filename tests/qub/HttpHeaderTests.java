package qub;

public interface HttpHeaderTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(HttpHeader.class, () ->
        {
            runner.testGroup("create(String,String)", () ->
            {
                runner.test("with null name", (Test test) ->
                {
                    test.assertThrows(() -> new HttpHeader(null, "V"), new PreConditionFailure("name cannot be null."));
                });

                runner.test("with empty name", (Test test) ->
                {
                    test.assertThrows(() -> new HttpHeader("", "V"), new PreConditionFailure("name cannot be empty."));
                });

                runner.test("with null value", (Test test) ->
                {
                    test.assertThrows(() -> new HttpHeader("N", null), new PreConditionFailure("value cannot be null."));
                });

                runner.test("with empty value", (Test test) ->
                {
                    test.assertThrows(() -> new HttpHeader("N", ""), new PreConditionFailure("value cannot be empty."));
                });

                runner.test("with name and value", (Test test) ->
                {
                    final HttpHeader header = new HttpHeader("user-agent", "qub-browser");
                    test.assertEqual("user-agent", header.getName());
                    test.assertEqual("qub-browser", header.getValue());
                });
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
