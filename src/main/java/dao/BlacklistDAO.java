package dao;

import db.JDBC;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class BlacklistDAO {

    private static final Logger LOGGER = Logger.getLogger(BlacklistDAO.class);
    private static final String GET_ALL_HOSTS_QUERY = "SELECT * FROM blacklist";
    private static final String INSERT_HOST_QUERY = "INSERT INTO blacklist (host) VALUES (?)";
    private static final String DELETE_HOST_QUERY = "DELETE FROM blacklist WHERE host = ?";
    private static final String GET_HOST_QUERY = "SELECT host FROM blacklist WHERE host = ?";

    public static List<String> getAllHosts() {
        List<String> allHosts = new ArrayList<>();
        try (Connection connection = JDBC.getJdbcDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_ALL_HOSTS_QUERY)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String host = resultSet.getString("host");
                    allHosts.add(host);
                }
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return allHosts;
    }

    public static void addHost(String host) throws SQLException {
        try (Connection connection = JDBC.getJdbcDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_HOST_QUERY)) {
            preparedStatement.setString(1, host);
            preparedStatement.execute();
        }
    }

    public static void removeHost(String host) throws SQLException {
        try (Connection connection = JDBC.getJdbcDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_HOST_QUERY)) {
            preparedStatement.setString(1, host);
            preparedStatement.executeUpdate();
        }
    }

    public static boolean isHostInBlacklist(String host) {
        boolean hostInBlacklist = false;
        try (Connection connection = JDBC.getJdbcDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_HOST_QUERY)) {
            preparedStatement.setString(1, host);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                hostInBlacklist = resultSet.next();
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return hostInBlacklist;
    }

}
