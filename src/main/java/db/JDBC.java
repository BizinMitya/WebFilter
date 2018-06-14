package db;

import dao.SettingsDAO;
import org.apache.log4j.Logger;
import org.h2.jdbcx.JdbcDataSource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class JDBC {

    private static final Logger LOGGER = Logger.getLogger(JDBC.class);
    private static URI settingsSqlScriptFilename;
    private static URI blacklistSqlScriptFilename;
    private static JdbcDataSource jdbcDataSource;

    static {
        try {
            settingsSqlScriptFilename = JDBC.class.getResource("/sql/settings.sql").toURI();
            blacklistSqlScriptFilename = JDBC.class.getResource("/sql/blacklist.sql").toURI();
            jdbcDataSource = new JdbcDataSource();
            jdbcDataSource.setURL("jdbc:h2:./db/webfilter;TRACE_LEVEL_FILE=4");
            initializeTables();
            SettingsDAO.addDefaultData();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static JdbcDataSource getJdbcDataSource() {
        return jdbcDataSource;
    }

    private static void initializeTables() {
        try (Connection connection = jdbcDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(getSQLScriptAsString(settingsSqlScriptFilename));
            statement.execute(getSQLScriptAsString(blacklistSqlScriptFilename));
        } catch (SQLException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static String getSQLScriptAsString(URI scriptFileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(scriptFileName)), UTF_8);
    }

}
