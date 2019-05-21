package model;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.json.HTTP.CRLF;

abstract class Web {

    protected static final String CONTENT = "content";
    private static final String CHARSET = "charset";
    private static final Logger LOGGER = Logger.getLogger(Web.class);

    String getCharsetFromContentType(@NotNull String contentType) {
        String[] values = contentType.split("; ");
        for (String value : values) {
            if (value.startsWith(CHARSET)) {
                return value.split("=")[1];
            }
        }
        return null;
    }

    String getMimeTypeFromContentType(@NotNull String contentType) {
        String[] values = contentType.split("; ");
        for (String value : values) {
            if (!value.startsWith(CHARSET)) {
                return value;
            }
        }
        return null;
    }

    byte[] getHttpMessageInBytes(@NotNull Consumer<StringBuilder> buildStartLine,
                                 byte[] body,
                                 @NotNull Set<Map.Entry<String, String>> headers) {
        byte[] result = null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            AtomicReference<StringBuilder> stringBuilder = new AtomicReference<>(new StringBuilder());
            buildStartLine.accept(stringBuilder.get());
            for (Map.Entry<String, String> entry : headers) {
                stringBuilder.getAndUpdate(s -> s.append(entry.getKey()).append(": ").append(entry.getValue()).append(CRLF));
            }
            stringBuilder.getAndUpdate(s -> s.append(CRLF));
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

}
