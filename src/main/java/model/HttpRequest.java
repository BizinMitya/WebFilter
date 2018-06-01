package model;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private static final String CR_LF = "\r\n";
    private static final Logger LOGGER = Logger.getLogger(HttpRequest.class);
    private static final String OPTIONS = "OPTIONS";
    private static final String GET = "GET";
    private static final String HEAD = "HEAD";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String PATCH = "PATCH";
    private static final String DELETE = "DELETE";
    private static final String TRACE = "TRACE";
    private static final String CONNECT = "CONNECT";
    private static final String UTF_8 = "UTF-8";
    private String method;
    private String URI;
    private String version;
    private Map<String, String> headers;
    private byte[] body;

    public HttpRequest() {
        headers = new HashMap<>();
    }

    public static HttpRequest readHttpRequest(InputStream inputStream) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, UTF_8));
            HttpRequest httpRequest = new HttpRequest();
            String startLine = bufferedReader.readLine();
            parseStartLine(httpRequest, startLine);
            String header = bufferedReader.readLine();
            while (header.length() > 0) {
                parseHeader(httpRequest, header);
                header = bufferedReader.readLine();
            }
            StringBuilder body = new StringBuilder();
            String bodyLine;
            while (bufferedReader.ready() && (bodyLine = bufferedReader.readLine()) != null) {
                body.append(bodyLine).append(CR_LF);
            }
            httpRequest.body = body.toString().getBytes(UTF_8);
            return httpRequest;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private static void parseStartLine(HttpRequest httpRequest, String startLine) {
        String[] startLineParameters = startLine.split(" ");
        httpRequest.method = startLineParameters[0];
        httpRequest.URI = startLineParameters[1];
        httpRequest.version = startLineParameters[2];
    }

    private static void parseHeader(HttpRequest httpRequest, String header) {
        int idx = header.indexOf(":");
        if (idx == -1) {
            LOGGER.error("Invalid Header Parameter: " + header);
        }
        httpRequest.headers.put(header.substring(0, idx), header.substring(idx + 2, header.length()));
    }

    private void addHeaders(org.apache.http.HttpRequest request) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.addHeader(entry.getKey(), entry.getValue());
        }
    }

    private Map<String, String> getMapHeaders(Header[] headers) {
        Map<String, String> mapHeaders = new HashMap<>(headers.length);
        for (Header header : headers) {
            mapHeaders.put(header.getName(), header.getValue());
        }
        return mapHeaders;
    }

    public String getHost() {
        return headers.getOrDefault("Host", null);
    }

    private HttpRequestBase getRequestByMethod() {
        switch (method) {
            case GET: {
                return new HttpGet(URI);
            }
            case POST: {
                return new HttpPost(URI);
            }
            case PUT: {
                return new HttpPut(URI);
            }
            case DELETE: {
                return new HttpDelete(URI);
            }
            case OPTIONS: {
                return new HttpOptions(URI);
            }
            case HEAD: {
                return new HttpHead(URI);
            }
            case TRACE: {
                return new HttpTrace(URI);
            }
            case PATCH: {
                return new HttpPatch(URI);
            }
            case CONNECT: {
                return null;
            }
            default: {
                return null;
            }
        }
    }

    public HttpResponse doRequest() throws IOException {
        HttpResponse httpResponse = new HttpResponse();
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setCircularRedirectsAllowed(true)
                    .setRedirectsEnabled(false)
                    .setRelativeRedirectsAllowed(true)
                    .build();
            HttpRequestBase request = getRequestByMethod();
            if (request != null) {
                request.setConfig(requestConfig);
                addHeaders(request);
                org.apache.http.HttpResponse response = client.execute(request);
                httpResponse.setStatusCode(response.getStatusLine().getStatusCode());
                httpResponse.setReasonPhrase(response.getStatusLine().getReasonPhrase());
                httpResponse.setVersion(response.getStatusLine().getProtocolVersion().toString());
                httpResponse.setHeaders(getMapHeaders(response.getAllHeaders()));
                if (response.getEntity() != null) {
                    httpResponse.setBody(IOUtils.toByteArray(response.getEntity().getContent()));
                    httpResponse.getHeaders().put("Transfer-Encoding", "identity");
                }
            }
        }
        return httpResponse;
    }

    private byte[] doChunk(byte[] body, String encoding) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            String hexLength = Integer.toHexString(body.length);
            byteArrayOutputStream.write(hexLength.getBytes(encoding));
            byteArrayOutputStream.write(CR_LF.getBytes(encoding));
            byteArrayOutputStream.write(body);
            byteArrayOutputStream.write(CR_LF.getBytes(encoding));
            byteArrayOutputStream.write(Integer.toHexString(0).getBytes(encoding));
            byteArrayOutputStream.write(CR_LF.getBytes(encoding));
            byteArrayOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

}
