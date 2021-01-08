package qub;

public interface JavaHttpClientTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaHttpClient.class, () ->
        {
            HttpClientTests.test(runner, (Network network) -> JavaHttpClient.create());
        });
    }
}
