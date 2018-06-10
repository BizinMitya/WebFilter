package util;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class LogUtil {

    private static final Logger LOGGER = Logger.getLogger(LogUtil.class);
    private static final String PATH_TO_LOG_DIR = "logs/";
    private static final String NAME_LOG_FILE = "server";
    private static final String LOG_EXTENSION = ".log";


    public static String getCurrentLog() {
        try {
            return new String(Files.readAllBytes(Paths.get(PATH_TO_LOG_DIR + NAME_LOG_FILE + LOG_EXTENSION)));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return "";
    }

    public static String getLogByDate(Date date) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(".yyyy-MM-dd");
            String dateString = simpleDateFormat.format(date);
            return new String(Files.readAllBytes(Paths.get(PATH_TO_LOG_DIR + NAME_LOG_FILE + dateString + LOG_EXTENSION)));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return "";
    }

}
