package db;

import dao.SettingsDAO;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.h2.jdbcx.JdbcDataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class JDBC {

    private static final Logger LOGGER = Logger.getLogger(JDBC.class);
    private static final String SETTINGS_SQL_SCRIPT_FILENAME = "/sql/settings.sql";
    private static final String BLACKLIST_SQL_SCRIPT_FILENAME = "/sql/blacklist.sql";
    private static JdbcDataSource jdbcDataSource;

    static {
        jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setURL(/*"jdbc:h2:./db/webfilter;TRACE_LEVEL_FILE=4"*/"jdbc:h2:./db/webfilter");
        initializeTables();
        SettingsDAO.addDefaultData();
    }

    public static JdbcDataSource getJdbcDataSource() {
        return jdbcDataSource;
    }

    private static void initializeTables() {
        try (Connection connection = jdbcDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(getSQLScriptAsString(SETTINGS_SQL_SCRIPT_FILENAME));
            statement.execute(getSQLScriptAsString(BLACKLIST_SQL_SCRIPT_FILENAME));
        } catch (SQLException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static String getSQLScriptAsString(String scriptFileName) throws IOException {
        byte[] data = IOUtils.toByteArray(JDBC.class.getResourceAsStream(scriptFileName));
        return new String(data, UTF_8);
    }

}
