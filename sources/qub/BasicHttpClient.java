package qub;

public class BasicHttpClient implements HttpClient
{
    private final Network network;
    private final DNS dns;

    private BasicHttpClient(Network network, DNS dns)
    {
        PreCondition.assertNotNull(network, "network");
        PreCondition.assertNotNull(dns, "dns");

        this.network = network;
        this.dns = dns;
    }

    public static BasicHttpClient create(Network network)
    {
        PreCondition.assertNotNull(network, "network");

        final DNS dns = DNS.create();
        return BasicHttpClient.create(network, dns);
    }

    public static BasicHttpClient create(Network network, DNS dns)
    {
        PreCondition.assertNotNull(network, "network");
        PreCondition.assertNotNull(dns, "dns");

        return new BasicHttpClient(network, dns);
    }

    @Override
    public Result<HttpResponse> send(HttpRequest request)
    {
        PreCondition.assertNotNull(request, "request");
        PreCondition.assertNotNull(request.getURL(), "request.getURL()");
        PreCondition.assertNotNullAndNotEmpty(request.getURL().getHost(), "request.getURL().getHost()");

        return Result.create(() ->
        {
            final URL requestUrl = request.getURL();
            final String requestHost = requestUrl.getHost();
            final IPv4Address requestIPAddress = this.dns.resolveHost(requestHost).await();

            Integer requestPort = requestUrl.getPort();
            if (requestPort == null)
            {
                requestPort = 80;
            }

            final MutableHttpResponse result = new MutableHttpResponse();

            try (final TCPClient tcpClient = this.network.createTCPClient(requestIPAddress, requestPort).await())
            {
                final BufferedByteWriteStream tcpClientBufferedWriteStream = new BufferedByteWriteStream(tcpClient);
                final CharacterToByteWriteStream tcpClientWriteStream = CharacterToByteWriteStream.create(tcpClientBufferedWriteStream)
                    .setCharacterEncoding(CharacterEncoding.UTF_8)
                    .setNewLine("\r\n");
                String httpVersion = request.getHttpVersion();
                if (Strings.isNullOrEmpty(httpVersion))
                {
                    httpVersion = "HTTP/1.1";
                }
                tcpClientWriteStream.writeLine("%s %s %s", request.getMethod(), request.getURL(), httpVersion).await();
                for (final HttpHeader header : request.getHeaders())
                {
                    tcpClientWriteStream.writeLine("%s:%s", header.getName(), header.getValue()).await();
                }
                tcpClientWriteStream.writeLine().await();

                final ByteReadStream requestBodyStream = request.getBody();
                if (requestBodyStream != null)
                {
                    tcpClientWriteStream.writeAll(requestBodyStream).await();
                }
                tcpClientBufferedWriteStream.flush().await();

                final BufferedByteReadStream bufferedByteReadStream = new BufferedByteReadStream(tcpClient);
                final CharacterReadStream responseCharacterReadStream = bufferedByteReadStream.asCharacterReadStream();
                String statusLine = responseCharacterReadStream.readLine().await();
                final int httpVersionLength = statusLine.indexOf(' ');

                result.setHTTPVersion(statusLine.substring(0, httpVersionLength));
                statusLine = statusLine.substring(httpVersionLength + 1);

                final int statusCodeStringLength = statusLine.indexOf(' ');
                final String statusCodeString = statusLine.substring(0, statusCodeStringLength);
                result.setStatusCode(Integer.parseInt(statusCodeString));
                result.setReasonPhrase(statusLine.substring(statusCodeStringLength + 1));

                String headerLine = responseCharacterReadStream.readLine().await();
                while (!headerLine.isEmpty())
                {
                    final int colonIndex = headerLine.indexOf(':');
                    final String headerName = headerLine.substring(0, colonIndex);
                    final String headerValue = headerLine.substring(colonIndex + 1).trim();
                    result.setHeader(headerName, headerValue);

                    headerLine = responseCharacterReadStream.readLine().await();
                }

                final Long contentLength = result.getContentLength()
                    .catchError(NotFoundException.class, () -> 0L)
                    .await();
                if (0 < contentLength)
                {
                    final InMemoryByteStream responseBodyStream = new InMemoryByteStream();

                    if (request.getMethod() != HttpMethod.HEAD)
                    {
                        long bytesToRead = contentLength;
                        while (0 < bytesToRead)
                        {
                            final byte[] bytesRead = bufferedByteReadStream.readBytes((int)Math.minimum(bytesToRead, Integers.maximum)).await();
                            if (bytesRead == null)
                            {
                                bytesToRead = 0;
                            }
                            else
                            {
                                responseBodyStream.writeAll(bytesRead).await();
                                bytesToRead -= bytesRead.length;
                            }
                        }
                    }
                    responseBodyStream.endOfStream();
                    result.setBody(responseBodyStream);
                }
            }

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }
}
