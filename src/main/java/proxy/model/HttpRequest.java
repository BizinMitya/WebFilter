package proxy.model;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import static dao.SettingsDAO.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpHeaders.CONTENT_LENGTH;
import static org.apache.http.HttpHeaders.TRANSFER_ENCODING;
import static org.apache.http.entity.ContentType.TEXT_HTML;
import static org.eclipse.jetty.http.HttpHeaderValue.IDENTITY;

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
    private static final String CHARSET = "charset";
    private static final String CONTENT = "content";
    private static final String HTTP_EQUIV = "http-equiv";
    private int timeoutForServer;//таймаут на чтение данных от сервера
    private String method;
    private String URI;
    private String version;
    private Map<String, String> headers;
    private byte[] body;

    public HttpRequest() {
        headers = new HashMap<>();
        setSettings();
    }

    public static HttpRequest readHttpRequest(InputStream inputStream) throws IOException {
        HttpRequest httpRequest = new HttpRequest();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, UTF_8));
        String startLine = bufferedReader.readLine();
        if (startLine == null) {
            throw new SocketException();
        }
        parseStartLine(httpRequest, startLine);
        String header = bufferedReader.readLine();
        while (header.length() > 0) {
            parseHeader(httpRequest, header);
            header = bufferedReader.readLine();
        }
        if (httpRequest.getHeaders().containsKey(CONTENT_LENGTH)) {
            int contentLength = Integer.parseInt(httpRequest.getHeaders().get(CONTENT_LENGTH));
            char[] body = new char[contentLength];
            bufferedReader.read(body);
            httpRequest.body = new String(body).getBytes(UTF_8);
        }
        return httpRequest;
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
            LOGGER.error("Некорректный параметр заголовка: " + header);
        }
        httpRequest.headers.put(header.substring(0, idx), header.substring(idx + 2, header.length()));
    }

    private void setSettings() {
        String timeoutForServerString = getSettingByKey(TIMEOUT_FOR_SERVER, String.valueOf(DEFAULT_TIMEOUT_FOR_SERVER));
        timeoutForServer = Integer.parseInt(timeoutForServerString);
    }

    private void addHeaders(org.apache.http.HttpRequest request) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (!CONTENT_LENGTH.equals(entry.getKey())) {//устанавливается автоматически при установки байтового массива body
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    private Map<String, String> getMapHeaders(Header[] headers) {
        Map<String, String> mapHeaders = new HashMap<>(headers.length);
        for (Header header : headers) {
            mapHeaders.put(header.getName(), header.getValue());
        }
        return mapHeaders;
    }

    @Nullable
    public String getHost() {
        return headers.getOrDefault("Host", null);
    }

    @Nullable
    private HttpRequestBase getRequestByMethod() {
        switch (method) {
            case GET: {
                return new HttpGet(URI);
            }
            case POST: {
                HttpPost httpPost = new HttpPost(URI);
                ByteArrayEntity byteArrayEntity = new ByteArrayEntity(body);
                httpPost.setEntity(byteArrayEntity);
                return httpPost;
            }
            case PUT: {
                HttpPut httpPut = new HttpPut(URI);
                ByteArrayEntity byteArrayEntity = new ByteArrayEntity(body);
                httpPut.setEntity(byteArrayEntity);
                return httpPut;
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
                HttpPatch httpPatch = new HttpPatch(URI);
                ByteArrayEntity byteArrayEntity = new ByteArrayEntity(body);
                httpPatch.setEntity(byteArrayEntity);
                return httpPatch;
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
                    .setConnectTimeout(timeoutForServer)
                    .setSocketTimeout(timeoutForServer)
                    .setConnectionRequestTimeout(timeoutForServer)
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
                    byte[] body = IOUtils.toByteArray(response.getEntity().getContent());
                    httpResponse.setBody(body);
                    Header contentTypeHeader = response.getEntity().getContentType();
                    if (contentTypeHeader != null) {
                        String contentType = contentTypeHeader.getValue();
                        httpResponse.setMimeType(getMimeTypeFromContentType(contentType));
                        httpResponse.setBodyEncoding(getEncoding(body, contentType));
                    }
                    if (httpResponse.getHeaders().containsKey(TRANSFER_ENCODING)) {
                        httpResponse.getHeaders().replace(TRANSFER_ENCODING, IDENTITY.toString());
                    }
                }
            }
        }
        return httpResponse;
    }

    private String getCharsetFromContentType(String contentType) {
        String[] values = contentType.split("; ");
        for (String value : values) {
            if (value.startsWith(CHARSET)) {
                return value.split("=")[1];
            }
        }
        return null;
    }

    private String getMimeTypeFromContentType(String contentType) {
        String[] values = contentType.split("; ");
        for (String value : values) {
            if (!value.startsWith(CHARSET)) {
                return value;
            }
        }
        return null;
    }

    public String getEncoding(byte[] body, String contentType) {
        if (contentType != null) {
            String charset = getCharsetFromContentType(contentType);
            if (charset != null) {
                return charset;
            }
        }
        if (TEXT_HTML.getMimeType().equals(contentType)) {
            Elements charsetElements = Jsoup.parse(new String(body)).head().getElementsByAttribute(CHARSET);
            if (!charsetElements.isEmpty()) {
                return charsetElements.get(0).attr(CHARSET);
            } else {
                Elements httpEquivElements = Jsoup.parse(new String(body)).head().getElementsByAttribute(HTTP_EQUIV);
                if (!httpEquivElements.isEmpty()) {
                    String content = httpEquivElements.get(0).attr(CONTENT);
                    String charset = getCharsetFromContentType(content);
                    if (charset != null) {
                        return charset;
                    }
                }
            }
        }
        return UTF_8.toString();
    }

    private byte[] doChunk(byte[] body, String encoding) {
        byte[] chunk = null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            String hexLength = Integer.toHexString(body.length);
            byteArrayOutputStream.write(hexLength.getBytes(encoding));
            byteArrayOutputStream.write(CR_LF.getBytes(encoding));
            byteArrayOutputStream.write(body);
            byteArrayOutputStream.write(CR_LF.getBytes(encoding));
            byteArrayOutputStream.write(Integer.toHexString(0).getBytes(encoding));
            byteArrayOutputStream.write(CR_LF.getBytes(encoding));
            byteArrayOutputStream.flush();
            chunk = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return chunk;
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
