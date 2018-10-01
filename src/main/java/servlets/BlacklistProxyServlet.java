package servlets;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import proxy.Proxy;
import proxy.model.Host;
import util.HostUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.sql.SQLException;

import static dao.BlacklistDAO.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class BlacklistProxyServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BlacklistProxyServlet.class);
    private Proxy proxy = Proxy.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setCharacterEncoding(UTF_8.toString());
            JSONArray jsonArray = new JSONArray(getAllHosts());
            response.getWriter().write(jsonArray.toString());
            response.getWriter().flush();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.setCharacterEncoding(UTF_8.toString());
            String body = request.getReader().readLine();
            String[] hostParam = body.split("=");
            if (hostParam.length == 2) {
                String hostOrIp = URLDecoder.decode(hostParam[1], UTF_8.toString());
                Host host = HostUtil.createHostFromHostOrIp(hostOrIp);
                addHostInBlacklist(host);
                LOGGER.info(String.format("Хост %s добавлен в чёрный список!", hostOrIp));
                if (proxy.isRunning()) {
                    proxy.restart();
                }
            }
        } catch (UnknownHostException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException | SQLException e) {
            LOGGER.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.setCharacterEncoding(UTF_8.toString());
            String body = request.getReader().readLine();
            String[] hostParam = body.split("=");
            if (hostParam.length == 2) {
                String ip = URLDecoder.decode(hostParam[1], UTF_8.toString());
                Host host = HostUtil.createHostFromHostOrIp(ip);
                removeHost(host);
                LOGGER.info(String.format("Хост %s удалён из чёрного списка!", ip));
                if (proxy.isRunning()) {
                    proxy.restart();
                }
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