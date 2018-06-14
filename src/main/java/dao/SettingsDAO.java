package dao;

import db.JDBC;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class SettingsDAO {

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

    private static final Logger LOGGER = Logger.getLogger(SettingsDAO.class);

    private static final String CHECK_SETTING_QUERY = "SELECT `key` FROM settings WHERE `key` = ?";
    private static final String INSERT_SETTING_QUERY = "INSERT INTO settings (`key`, value) VALUES (?, ?)";
    private static final String UPDATE_SETTING_QUERY = "UPDATE settings SET value = ? WHERE `key` = ?";
    private static final String GET_SETTING_QUERY = "SELECT value FROM settings WHERE `key` = ?";
    private static final String GET_ALL_SETTINGS_QUERY = "SELECT * FROM settings";

    private static boolean hasKey(String key) {
        boolean hasKey = false;
        try (Connection connection = JDBC.getJdbcDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(CHECK_SETTING_QUERY)) {
            preparedStatement.setString(1, key);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                hasKey = resultSet.next();
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return hasKey;
    }

    public static void addSetting(String key, String value) {
        addOrUpdateHelper(key, value, INSERT_SETTING_QUERY);
    }

    public static void updateSetting(String key, String value) {
        addOrUpdateHelper(value, key, UPDATE_SETTING_QUERY);
    }

    private static void addOrUpdateHelper(String key, String value, String sql) {
        try (Connection connection = JDBC.getJdbcDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, value);
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void addDefaultData() {
        if (!hasKey(PROXY_PORT)) {
            addSetting(PROXY_PORT, String.valueOf(DEFAULT_PROXY_PORT));
        }
        if (!hasKey(THREADS_COUNT)) {
            addSetting(THREADS_COUNT, String.valueOf(DEFAULT_THREADS_COUNT));
        }
        if (!hasKey(TIMEOUT_FOR_CLIENT)) {
            addSetting(TIMEOUT_FOR_CLIENT, String.valueOf(DEFAULT_TIMEOUT_FOR_CLIENT));
        }
        if (!hasKey(TIMEOUT_FOR_SERVER)) {
            addSetting(TIMEOUT_FOR_SERVER, String.valueOf(DEFAULT_TIMEOUT_FOR_SERVER));
        }
    }

    public static String getSettingByKey(String key, String defaultValue) {
        String value = defaultValue;
        try (Connection connection = JDBC.getJdbcDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_SETTING_QUERY)) {
            preparedStatement.setString(1, key);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    value = resultSet.getString("value");
                }
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return value;
    }

    public static Map<String, String> getAllSettings() {
        Map<String, String> allSettings = new HashMap<>();
        try (Connection connection = JDBC.getJdbcDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_ALL_SETTINGS_QUERY)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String key = resultSet.getString("key");
                    String value = resultSet.getString("value");
                    allSettings.put(key, value);
                }
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return allSettings;
    }

    public static void saveAllSettings(Map<String, String> allSettings) {
        for (Map.Entry<String, String> entry : allSettings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            addSetting(key, value);
        }
    }

    public static void updateAllSettings(Map<String, String> allSettings) {
        for (Map.Entry<String, String> entry : allSettings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            updateSetting(key, value);
        }
    }

}
