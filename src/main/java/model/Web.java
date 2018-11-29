package model;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.Map;

import static org.apache.http.entity.ContentType.TEXT_HTML;
import static org.apache.lucene.util.IOUtils.UTF_8;

public abstract class Web {

    protected static final String CONTENT = "content";
    static final String CR_LF = "\r\n";
    private static final String CHARSET = "charset";
    private static final String HTTP_EQUIV = "http-equiv";
    protected byte[] body;
    protected Map<String, String> headers;
    protected String version;

    private String getCharsetFromContentType(String contentType) {
        String[] values = contentType.split("; ");
        for (String value : values) {
            if (value.startsWith(CHARSET)) {
                return value.split("=")[1];
            }
        }
        return null;
    }

    String getMimeTypeFromContentType(String contentType) {
        String[] values = contentType.split("; ");
        for (String value : values) {
            if (!value.startsWith(CHARSET)) {
                return value;
            }
        }
        return null;
    }

    String getEncoding(byte[] body, String contentType) {
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
        return UTF_8;
    }

}
