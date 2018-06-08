package web.servlets;

import org.apache.log4j.Logger;
import proxy.Proxy;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StatusProxyServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(StatusProxyServlet.class);
    private static final String UTF_8 = "UTF-8";
    private Proxy proxy = Proxy.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setCharacterEncoding(UTF_8);
            response.getWriter().write(Boolean.toString(proxy.isRunning()));
            response.getWriter().flush();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}