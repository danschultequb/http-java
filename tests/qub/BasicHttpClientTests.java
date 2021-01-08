package qub;

public class BasicHttpClientTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(BasicHttpClient.class, () ->
        {
            HttpClientTests.test(runner, (Network network) ->
            {
                return BasicHttpClient.create(network);
            });
        });
    }
}
