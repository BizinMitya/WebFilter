package servlets;

import org.apache.log4j.Logger;
import proxy.Proxy;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class StatusProxyServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(StatusProxyServlet.class);
    private Proxy proxy = Proxy.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setCharacterEncoding(UTF_8.toString());
            response.getWriter().write("[" + proxy.isRunningHttp() + ", " + proxy.isRunningHttps() + "]");
            response.getWriter().flush();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

}