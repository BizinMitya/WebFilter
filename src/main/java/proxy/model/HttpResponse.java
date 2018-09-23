package proxy.model;

import classificators.Category;
import classificators.bayes.BayesClassifier;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.http.HttpHeaders.TRANSFER_ENCODING;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.*;
import static org.eclipse.jetty.http.HttpHeaderValue.IDENTITY;
import static util.FileUtil.getHostInBlacklistPage;

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

    public static HttpResponse hostInBlacklistResponse() {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(SC_OK);
        httpResponse.setReasonPhrase("");
        httpResponse.setVersion("HTTP/1.1");
        httpResponse.setHeaders(new HashMap<>());
        httpResponse.getHeaders().put(TRANSFER_ENCODING, IDENTITY.toString());
        byte[] body = getHostInBlacklistPage();
        httpResponse.setBody(body);
        return httpResponse;
    }

    public void replaceInBody(String target, String replacement) throws UnsupportedEncodingException {
        if (isHtml() ||
                TEXT_XML.getMimeType().equals(mimeType) ||
                TEXT_PLAIN.getMimeType().equals(mimeType) ||
                APPLICATION_JSON.getMimeType().equals(mimeType) ||
                APPLICATION_XML.getMimeType().equals(mimeType)) {
            String bodyString = new String(body, bodyEncoding);
            bodyString = bodyString.replace(target, replacement);
            this.body = bodyString.getBytes(bodyEncoding);
        }
    }

    public boolean isHtml() {
        return TEXT_HTML.getMimeType().equals(mimeType);
    }

    public Map<Category, Double> classifyContent() throws UnsupportedEncodingException {
        String bodyString = new String(body, bodyEncoding);
        Pattern pattern = Pattern.compile("[а-яА-Я]+");
        Matcher matcher = pattern.matcher(Jsoup.parse(bodyString).text());
        List<String> words = new ArrayList<>();
        while (matcher.find()) {
            words.add(matcher.group());
        }
        return BayesClassifier.classify(words);
    }

    public void createCategoriesInfoScript(Map<Category, Double> categoryProbabilityMap) throws UnsupportedEncodingException {
        String bodyString = new String(body, bodyEncoding);
        StringBuffer infoScript = new StringBuffer();
        infoScript.append("<script>").append("alert('");
        for (Map.Entry<Category, Double> entry : categoryProbabilityMap.entrySet()) {
            infoScript.append(entry.getKey()).append(": ").append(String.format("%.4f", entry.getValue())).append("; ");
        }
        infoScript.append("')").append("</script>");
        Document html = Jsoup.parse(bodyString);
        html.head().append(infoScript.toString());
        this.body = html.toString().getBytes(bodyEncoding);
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
