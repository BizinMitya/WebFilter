package servlets;

import model.Host;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import util.HostUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.sql.SQLException;

import static dao.BlacklistDAO.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class BlacklistProxyServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BlacklistProxyServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, @NotNull HttpServletResponse response) {
        try (Writer writer = response.getWriter()) {
            response.setCharacterEncoding(UTF_8.toString());
            JSONArray jsonArray = new JSONArray(getAllHosts());
            writer.write(jsonArray.toString());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPost(@NotNull HttpServletRequest request, HttpServletResponse response) {
        try (BufferedReader reader = request.getReader()) {
            request.setCharacterEncoding(UTF_8.toString());
            String body = reader.readLine();
            String[] hostParam = body.split("=");
            if (hostParam.length == 2) {
                String hostOrIp = URLDecoder.decode(hostParam[1], UTF_8.toString());
                Host host = HostUtil.createHostFromHostOrIp(hostOrIp);
                addHostInBlacklist(host);
                LOGGER.info(String.format("Хост %s добавлен в чёрный список!", hostOrIp));
            }
        } catch (UnknownHostException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException | SQLException e) {
            LOGGER.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(@NotNull HttpServletRequest request, HttpServletResponse response) {
        try (BufferedReader reader = request.getReader()) {
            request.setCharacterEncoding(UTF_8.toString());
            String body = reader.readLine();
            String[] hostParam = body.split("=");
            if (hostParam.length == 2) {
                String ip = URLDecoder.decode(hostParam[1], UTF_8.toString());
                Host host = HostUtil.createHostFromHostOrIp(ip);
                removeHost(host);
                LOGGER.info(String.format("Хост %s удалён из чёрного списка!", ip));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (UnknownHostException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException | SQLException e) {
            LOGGER.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}