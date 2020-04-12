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
        PreCondition.assertNotNull(request.getURL(), "request.getURL()");
        PreCondition.assertNotNullAndNotEmpty(request.getURL().getHost(), "request.getURL().getHost()");

        return Result.create(() ->
        {
            HttpResponse result;
            try
            {
                final java.net.URL url = new java.net.URL(request.getURL().toString());
                final java.net.HttpURLConnection urlConnection = (java.net.HttpURLConnection)url.openConnection();
                urlConnection.setInstanceFollowRedirects(false);

                urlConnection.setRequestMethod(request.getMethod().toString());

                final HttpHeaders requestHeaders = request.getHeaders();
                if (requestHeaders != null)
                {
                    for (final HttpHeader header : requestHeaders)
                    {
                        urlConnection.setRequestProperty(header.getName(), header.getValue());
                    }
                }

                final ByteReadStream body = request.getBody();
                if (body != null)
                {
                    try
                    {
                        urlConnection.setDoOutput(true);

                        final ByteWriteStream writeStream = new OutputStreamToByteWriteStream(urlConnection.getOutputStream());
                        try
                        {
                            writeStream.writeAll(body).await();
                        }
                        finally
                        {
                            writeStream.dispose().await();
                        }
                    }
                    finally
                    {
                        body.dispose().await();
                    }
                }

                final MutableHttpResponse response = new MutableHttpResponse()
                    .setReasonPhrase(urlConnection.getResponseMessage())
                    .setStatusCode(urlConnection.getResponseCode());

                final java.util.Map<String,java.util.List<String>> responseHeaders = urlConnection.getHeaderFields();
                if (responseHeaders == null || !responseHeaders.containsKey(null))
                {
                    response.setHTTPVersion("HTTP/1.1");
                }
                else
                {
                    final String statusLine = responseHeaders.get(null).get(0);
                    final int firstSpace = statusLine.indexOf(' ');
                    response.setHTTPVersion(statusLine.substring(0, firstSpace));
                }

                if (responseHeaders != null)
                {
                    for (final java.util.Map.Entry<String,java.util.List<String>> responseHeader : responseHeaders.entrySet())
                    {
                        final String headerName = responseHeader.getKey();
                        if (!Strings.isNullOrEmpty(headerName))
                        {
                            response.setHeader(headerName, Strings.join(',', responseHeader.getValue()));
                        }
                    }
                }

                final int statusCode = response.getStatusCode();
                final java.io.InputStream javaResponseBody = (400 <= statusCode && statusCode <= 599)
                    ? urlConnection.getErrorStream()
                    : urlConnection.getInputStream();
                final ByteReadStream responseBody = javaResponseBody != null
                    ? new InputStreamToByteReadStream(javaResponseBody)
                    : new InMemoryByteStream().endOfStream();
                response.setBody(responseBody);

                result = response;
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
