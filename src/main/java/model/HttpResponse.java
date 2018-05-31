package model;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private static final Logger LOGGER = Logger.getLogger(HttpResponse.class);
    private static final String UTF_8 = "UTF-8";

    private String version;
    private int statusCode;
    private String reasonPhrase;
    private Map<String, String> headers;
    private byte[] body;

    public HttpResponse() {
        headers = new HashMap<>();
    }

    private static void parseStartLine(HttpResponse httpResponse, String startLine) {
        String[] startLineParameters = startLine.split(" ");
        httpResponse.version = startLineParameters[0];
        httpResponse.statusCode = Integer.parseInt(startLineParameters[1]);
        httpResponse.reasonPhrase = startLineParameters[2];
    }

    private static void parseHeader(HttpResponse httpResponse, String header) {
        int idx = header.indexOf(":");
        if (idx == -1) {
            LOGGER.error("Invalid Header Parameter: " + header);
        }
        httpResponse.headers.put(header.substring(0, idx), header.substring(idx + 2, header.length()));
    }

    public void replaceInBody(String target, String replacement) throws UnsupportedEncodingException {
        String bodyString = new String(body, this.getEncoding());
        bodyString = bodyString.replace(target, replacement);
        this.body = bodyString.getBytes(this.getEncoding());
    }

    public String getEncoding() {
        String contentType = headers.get("Content-Type");
        if (contentType != null) {
            String[] values = contentType.split("; ");
            for (String value : values) {
                if (value.startsWith("charset")) {
                    return value.split("=")[1];
                }
            }
        }
        return UTF_8;
    }

    public byte[] getAllResponseInBytes() {
        byte[] result = null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getVersion()).append(" ").append(getStatusCode()).append(" ").append(getReasonPhrase()).append("\r\n");
            for (Map.Entry<String, String> entry : getHeaders().entrySet()) {
                stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
            }
            stringBuilder.append("\r\n");
            byteArrayOutputStream.write(stringBuilder.toString().getBytes());
            if (body != null) {
                byteArrayOutputStream.write(body);
            }
            byteArrayOutputStream.flush();
            result = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
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
