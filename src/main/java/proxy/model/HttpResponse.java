package proxy.model;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.http.entity.ContentType.*;

public class HttpResponse {

    private static final String CR_LF = "\r\n";
    private static final Logger LOGGER = Logger.getLogger(HttpResponse.class);
    private String version;
    private int statusCode;
    private String reasonPhrase;
    private Map<String, String> headers;
    private byte[] body;
    private String bodyEncoding;
    private String mimeType;

    public HttpResponse() {
        headers = new HashMap<>();
    }

    public void replaceInBody(String target, String replacement) throws UnsupportedEncodingException {
        if (mimeType.equals(TEXT_HTML.getMimeType()) ||
                mimeType.equals(TEXT_XML.getMimeType()) ||
                mimeType.equals(TEXT_PLAIN.getMimeType()) ||
                mimeType.equals(APPLICATION_JSON.getMimeType()) ||
                mimeType.equals(APPLICATION_XML.getMimeType())) {
            String bodyString = new String(body, bodyEncoding);
            bodyString = bodyString.replace(target, replacement);
            this.body = bodyString.getBytes(bodyEncoding);
        }
    }

    public String getBodyEncoding() {
        return bodyEncoding;
    }

    public void setBodyEncoding(String bodyEncoding) {
        this.bodyEncoding = bodyEncoding;
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
            byteArrayOutputStream.write(stringBuilder.toString().getBytes(/*getEncoding()*/));
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

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
