package model;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private static final String CR_LF = "\r\n";
    private static final Logger LOGGER = Logger.getLogger(HttpResponse.class);
    private static final String UTF_8 = "UTF-8";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String TEXT_HTML = "text/html";
    private String version;
    private int statusCode;
    private String reasonPhrase;
    private Map<String, String> headers;
    private byte[] body;

    public HttpResponse() {
        headers = new HashMap<>();
    }

    public void replaceInBody(String target, String replacement) throws UnsupportedEncodingException {
        String bodyString = new String(body, getEncoding());
        bodyString = bodyString.replace(target, replacement);
        this.body = bodyString.getBytes(getEncoding());
    }

    private String getCharsetFromContent(String content) {
        String[] values = content.split("; ");
        for (String value : values) {
            if (value.startsWith("charset")) {
                return value.split("=")[1];
            }
        }
        return null;
    }

    public String getEncoding() {
        String contentType = headers.get(CONTENT_TYPE);
        if (contentType != null) {
            String charset = getCharsetFromContent(contentType);
            if (charset != null) {
                return charset;
            }
        }
        if (TEXT_HTML.equals(contentType)) {
            String encoding = Jsoup.parse(new String(body)).head().getElementsByAttribute("charset").get(0).attr("charset");
            if (encoding.isEmpty()) {
                Element httpEquivElement = Jsoup.parse(new String(body)).head().getElementsByAttribute("http-equiv").get(0);
                String content = httpEquivElement.attr("content");
                String charset = getCharsetFromContent(content);
                if (charset != null) {
                    return charset;
                }
            } else {
                return encoding;
            }
        }
        return UTF_8;
    }

    public byte[] getAllResponseInBytes() {
        byte[] result = null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getVersion()).append(" ").append(getStatusCode()).append(" ").append(getReasonPhrase()).append(CR_LF);
            for (Map.Entry<String, String> entry : getHeaders().entrySet()) {
                stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append(CR_LF);
            }
            stringBuilder.append(CR_LF);
            byteArrayOutputStream.write(stringBuilder.toString().getBytes(getEncoding()));
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
