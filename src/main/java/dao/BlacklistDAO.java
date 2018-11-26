package dao;

import db.JDBC;
import model.Host;
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
    private static final String INSERT_HOST_QUERY = "INSERT INTO blacklist (ip, host) VALUES (?, ?)";
    private static final String DELETE_HOST_QUERY = "DELETE FROM blacklist WHERE ip = ?";
    private static final String GET_HOST_QUERY = "SELECT ip, host FROM blacklist WHERE ip = ?";

    public static List<Host> getAllHosts() {
        List<Host> results = new ArrayList<>();
        try (Connection connection = JDBC.getJdbcDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_ALL_HOSTS_QUERY)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String ip = resultSet.getString("ip");
                    String host = resultSet.getString("host");
                    results.add(new Host(ip, host));
                }
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return results;
    }

    public static void addHostInBlacklist(Host host) throws SQLException {
        try (Connection connection = JDBC.getJdbcDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_HOST_QUERY)) {
            preparedStatement.setString(1, host.getIp());
            preparedStatement.setString(2, host.getHostname());
            preparedStatement.execute();
        }
    }

    public static void removeHost(Host host) throws SQLException {
        try (Connection connection = JDBC.getJdbcDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_HOST_QUERY)) {
            preparedStatement.setString(1, host.getIp());
            preparedStatement.executeUpdate();
        }
    }

    public static boolean isHostInBlacklist(Host host) {
        boolean hostInBlacklist = false;
        try (Connection connection = JDBC.getJdbcDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_HOST_QUERY)) {
            preparedStatement.setString(1, host.getIp());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                hostInBlacklist = resultSet.next();
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return hostInBlacklist;
    }

}
