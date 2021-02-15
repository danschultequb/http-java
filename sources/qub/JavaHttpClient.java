package qub;

/**
 * An HttpClient implementation that uses the default Java libraries to send HTTP requests.
 */
public class JavaHttpClient implements HttpClient
{
    private JavaHttpClient()
    {
    }

    public static JavaHttpClient create()
    {
        return new JavaHttpClient();
    }

    @Override
    public Result<HttpResponse> send(HttpRequest request)
    {
        PreCondition.assertNotNull(request, "request");
        PreCondition.assertNotNullAndNotEmpty(request.getMethod(), "request.getMethod()");
        PreCondition.assertNotNull(request.getURL(), "request.getURL()");
        PreCondition.assertNotNullAndNotEmpty(request.getURL().getHost(), "request.getURL().getHost()");

        return Result.create(() ->
        {
            HttpResponse result;
            try
            {
                final java.net.URL url = new java.net.URL(request.getURL().toString(true));
                final java.net.HttpURLConnection urlConnection = (java.net.HttpURLConnection)url.openConnection();
                urlConnection.setInstanceFollowRedirects(false);

                urlConnection.setRequestMethod(request.getMethod());

                final HttpHeaders requestHeaders = request.getHeaders();
                for (final HttpHeader header : requestHeaders)
                {
                    urlConnection.setRequestProperty(header.getName(), header.getValue());
                }

                final ByteReadStream body = request.getBody();
                if (body != null)
                {
                    try
                    {
                        urlConnection.setDoOutput(true);

                        try (final ByteWriteStream writeStream = new OutputStreamToByteWriteStream(urlConnection.getOutputStream()))
                        {
                            writeStream.writeAll(body).await();
                        }
                    }
                    finally
                    {
                        body.dispose().await();
                    }
                }

                final MutableHttpResponse response = HttpResponse.create()
                    .setReasonPhrase(urlConnection.getResponseMessage())
                    .setStatusCode(urlConnection.getResponseCode());

                final java.util.Map<String,java.util.List<String>> responseHeaders = urlConnection.getHeaderFields();

                final String statusLine = responseHeaders.get(null).get(0);
                final int firstSpace = statusLine.indexOf(' ');
                response.setHttpVersion(statusLine.substring(0, firstSpace));

                for (final java.util.Map.Entry<String,java.util.List<String>> responseHeader : responseHeaders.entrySet())
                {
                    final String headerName = responseHeader.getKey();
                    if (!Strings.isNullOrEmpty(headerName))
                    {
                        response.setHeader(headerName, Strings.join(',', responseHeader.getValue()));
                    }
                }

                final int statusCode = response.getStatusCode();
                final java.io.InputStream javaResponseBody = 400 <= statusCode
                    ? urlConnection.getErrorStream()
                    : urlConnection.getInputStream();
                response.setBody(new InputStreamToByteReadStream(javaResponseBody));

                result = response;
            }
            catch (java.net.UnknownHostException e)
            {
                throw new HostNotFoundException(request.getURL().getHost());
            }
            catch (java.io.IOException e)
            {
                throw Exceptions.asRuntime(e);
            }

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }
}
