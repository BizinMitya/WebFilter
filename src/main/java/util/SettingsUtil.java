package util;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public abstract class SettingsUtil {

    public static final String TIMEOUT_FOR_SERVER = "timeoutForServer";
    public static final String TIMEOUT_FOR_CLIENT = "timeoutForClient";
    public static final String PROXY_PORT = "proxyPort";
    public static final String THREADS_COUNT = "threadsCount";
    public static final int DEFAULT_TIMEOUT_FOR_SERVER = 10_000;
    public static final int DEFAULT_TIMEOUT_FOR_CLIENT = 10_000;
    public static final int DEFAULT_PROXY_PORT = 3333;
    public static final int DEFAULT_THREADS_COUNT = 5;

    public static final int MIN_PROXY_PORT = 1024;
    public static final int MIN_THREADS_COUNT = 1;
    public static final int MIN_TIMEOUT_FOR_CLIENT = 1_000;
    public static final int MIN_TIMEOUT_FOR_SERVER = 1_000;
    public static final int MAX_PROXY_PORT = 65535;
    public static final int MAX_THREADS_COUNT = 10;
    public static final int MAX_TIMEOUT_FOR_CLIENT = 60_000;
    public static final int MAX_TIMEOUT_FOR_SERVER = 60_000;

    private static final Logger LOGGER = Logger.getLogger(SettingsUtil.class);
    private static String fileSettingsName = "settings/settings.properties";

    public static String getSettingByName(String settingName, String defaultValue) {
        try (FileInputStream fileInputStream = new FileInputStream(fileSettingsName)) {
            Properties properties = new Properties();
            properties.load(fileInputStream);
            return properties.getProperty(settingName, defaultValue);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public static Properties getAllSettings() {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(fileSettingsName)) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return properties;
    }

    public static Map<String, String> getAllSettingsAsMap() {
        Properties properties = getAllSettings();
        return properties.entrySet().stream()
                .collect(Collectors.toMap(entry -> (String) entry.getKey(), e -> (String) e.getValue()));
    }

    public static void saveSetting(String setting, String value) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileSettingsName)) {
            Properties properties = getAllSettings();
            if (properties == null) {
                properties = new Properties();
            }
            properties.setProperty(setting, value);
            properties.store(fileOutputStream, null);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void saveAllSettings(Properties properties) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileSettingsName)) {
            properties.store(fileOutputStream, null);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
