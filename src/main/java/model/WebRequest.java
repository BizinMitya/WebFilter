package model;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static dao.SettingsDAO.*;
import static org.apache.http.HttpHeaders.*;
import static org.apache.http.HttpVersion.HTTP_1_1;
import static org.apache.lucene.util.IOUtils.UTF_8;
import static org.eclipse.jetty.http.HttpHeaderValue.IDENTITY;
import static org.json.HTTP.CRLF;

public class WebRequest extends Web {

    private static final Logger LOGGER = Logger.getLogger(WebRequest.class);
    private static final String OPTIONS = "OPTIONS";
    private static final String GET = "GET";
    private static final String HEAD = "HEAD";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String PATCH = "PATCH";
    private static final String DELETE = "DELETE";
    private static final String TRACE = "TRACE";
    private static final String CONNECT = "CONNECT";
    private int timeoutForServer;// таймаут на чтение данных от сервера
    private String method;
    private String URI;
    private byte[] body;
    private Map<String, String> headers;
    private String version;

    private WebRequest() {
        headers = new HashMap<>();
        setSettings();
    }

    @Nullable
    public static WebRequest readWebRequest(InputStream inputStream) {
        WebRequest webRequest = new WebRequest();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String startLine = bufferedReader.readLine();
            if (startLine == null) {
                throw new SocketException();
            }
            parseStartLine(webRequest, startLine);
            String header = bufferedReader.readLine();
            while (header != null && header.length() > 0) {
                parseHeader(webRequest, header);
                header = bufferedReader.readLine();
            }
            if (webRequest.getHeaders().containsKey(CONTENT_LENGTH)) {
                int contentLength = Integer.parseInt(webRequest.getHeaders().get(CONTENT_LENGTH));
                char[] body = new char[contentLength];//todo: длина в байтах же!
                //todo: проверить запросы с телом
                bufferedReader.read(body);
                webRequest.body = new String(body).getBytes();
            }
            webRequest.headers.put(ACCEPT_ENCODING, IDENTITY.toString());
            return webRequest;
        } catch (Exception e) {
            return null;
        }
    }

    private static void parseStartLine(@NotNull WebRequest webRequest, @NotNull String startLine) {
        String[] startLineParameters = startLine.split(" ");
        webRequest.method = startLineParameters[0];
        webRequest.URI = startLineParameters[1];
        webRequest.version = startLineParameters[2];
    }

    private static void parseHeader(WebRequest webRequest, @NotNull String header) {
        int idx = header.indexOf(":");
        if (idx == -1) {
            LOGGER.error("Некорректный параметр заголовка: " + header);
        }
        webRequest.headers.put(header.substring(0, idx), header.substring(idx + 2));
    }

    public boolean isConnectMethod() {
        return CONNECT.equals(method);
    }

    private void setSettings() {
        String timeoutForServerString = getSettingByKey(TIMEOUT_FOR_SERVER, String.valueOf(DEFAULT_TIMEOUT_FOR_SERVER));
        timeoutForServer = Integer.parseInt(timeoutForServerString);
    }

    private void addHeaders(org.apache.http.HttpRequest request) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (!CONTENT_LENGTH.equals(entry.getKey())) {// устанавливается автоматически при установки байтового массива body
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    private Map<String, String> getMapHeaders(@NotNull Header[] headers) {
        Map<String, String> mapHeaders = new HashMap<>(headers.length);
        for (Header header : headers) {
            mapHeaders.put(header.getName(), header.getValue());
        }
        return mapHeaders;
    }

    @Nullable
    public String getHost() {
        String host = headers.getOrDefault(HOST, "");
        if (host.indexOf(':') != -1) {
            host = host.substring(0, host.indexOf(':'));
        }
        return host;
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
            default: {
                return null;
            }
        }
    }

    public WebResponse doHttpRequest() throws IOException {
        WebResponse webResponse = new WebResponse();
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
                try (CloseableHttpResponse response = client.execute(request)) {
                    webResponse.setStatusCode(response.getStatusLine().getStatusCode());
                    webResponse.setReasonPhrase(response.getStatusLine().getReasonPhrase());
                    webResponse.setVersion(response.getStatusLine().getProtocolVersion().toString());
                    webResponse.setHeaders(getMapHeaders(response.getAllHeaders()));
                    if (response.getEntity() != null) {
                        byte[] body = IOUtils.toByteArray(response.getEntity().getContent());
                        webResponse.setBody(body);
                        Header contentTypeHeader = response.getEntity().getContentType();
                        if (contentTypeHeader != null) {
                            String contentType = contentTypeHeader.getValue();
                            webResponse.setMimeType(getMimeTypeFromContentType(contentType));
                            webResponse.setBodyEncoding(getCharsetFromContentType(contentType));
                        }
                        if (webResponse.getHeaders().containsKey(TRANSFER_ENCODING)) {
                            webResponse.getHeaders().replace(TRANSFER_ENCODING, IDENTITY.toString());
                            webResponse.getHeaders().put(CONTENT_LENGTH, String.valueOf(body.length));
                        }
                    }
                }
            } else {// connect method
                webResponse.setStatusCode(HttpServletResponse.SC_OK);
                webResponse.setReasonPhrase("OK");
                webResponse.setVersion(HTTP_1_1.toString());
            }
        }
        return webResponse;
    }

    @Nullable
    public WebResponse doHttpsRequest() throws IOException {
        if (!method.equals(CONNECT)) {
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            try (SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(getHost(), 443);
                 InputStream inputStream = sslSocket.getInputStream();
                 OutputStream outputStream = sslSocket.getOutputStream()) {
                sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
                sslSocket.setSoTimeout(timeoutForServer);
                sslSocket.startHandshake();
                outputStream.write(getAllRequestInBytes());
                outputStream.flush();
                return readHttpsResponse(inputStream);
            }
        } else {
            WebResponse webResponse = new WebResponse();
            webResponse.setStatusCode(HttpServletResponse.SC_OK);
            webResponse.setReasonPhrase("OK");
            webResponse.setVersion(HTTP_1_1.toString());
            return webResponse;
        }
    }

    private void parseStartLine(@NotNull WebResponse webResponse, @NotNull String startLine) {
        String[] startLineParameters = startLine.split(" ");
        webResponse.setVersion(startLineParameters[0]);
        webResponse.setStatusCode(Integer.parseInt(startLineParameters[1]));
        webResponse.setReasonPhrase(startLineParameters[2]);
    }

    private void parseHeader(WebResponse webResponse, @NotNull String header) {
        int idx = header.indexOf(":");
        if (idx == -1) {
            LOGGER.error("Некорректный параметр заголовка: " + header);
        }
        webResponse.getHeaders().put(header.substring(0, idx), header.substring(idx + 2));
    }

    @Nullable
    private WebResponse readHttpsResponse(InputStream inputStream) {
        try {
            WebResponse webResponse = new WebResponse();
            Scanner scanner = new Scanner(inputStream, UTF_8).useDelimiter(CRLF);
            String startLine = scanner.next();
            if (startLine == null) {
                throw new SocketException();
            }
            parseStartLine(webResponse, startLine);
            while (scanner.hasNext()) {
                String header = scanner.next();
                if (header.isEmpty()) {
                    break;
                } else {
                    parseHeader(webResponse, header);
                }
            }
            if (webResponse.getHeaders().containsKey(CONTENT_TYPE)) {
                String contentType = webResponse.getHeaders().get(CONTENT_TYPE);
                webResponse.setMimeType(getMimeTypeFromContentType(contentType));
                webResponse.setBodyEncoding(getCharsetFromContentType(contentType));
            }
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                String bodyEncoding = webResponse.getBodyEncoding() != null ? webResponse.getBodyEncoding() : UTF_8;
                while (scanner.hasNext()) {
                    //todo чтение в неправильной кодировке!
                    byteArrayOutputStream.write(Charset.forName(bodyEncoding).encode(scanner.next()).array());
                }
                webResponse.setBody(byteArrayOutputStream.toByteArray());
                if (webResponse.getHeaders().containsKey(TRANSFER_ENCODING)) {
                    webResponse.getHeaders().replace(TRANSFER_ENCODING, IDENTITY.toString());
                    webResponse.getHeaders().put(CONTENT_LENGTH, String.valueOf(webResponse.getBody().length));
                }
            }
            return webResponse;
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] getAllRequestInBytes() {
        return getHttpMessageInBytes(
                s -> s.append(getMethod()).append(" ")
                        .append(getURI()).append(" ")
                        .append(getVersion()).append(CRLF),
                body,
                headers.entrySet()
        );
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    private String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    private String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    private Map<String, String> getHeaders() {
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
