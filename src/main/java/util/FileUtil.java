package util;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public abstract class FileUtil {

    private static final String PATH_TO_PAGE = "/html/hostInBlacklist.html";
    private static final Logger LOGGER = Logger.getLogger(FileUtil.class);

    @Nullable
    public static byte[] getHostInBlacklistPage() {
        byte[] result = null;
        try {
            result = IOUtils.toByteArray(FileUtil.class.getResourceAsStream(PATH_TO_PAGE));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

}
