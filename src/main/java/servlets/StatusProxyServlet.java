package servlets;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import proxy.Proxy;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class StatusProxyServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(StatusProxyServlet.class);
    private Proxy proxy = Proxy.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, @NotNull HttpServletResponse response) {
        try (Writer writer = response.getWriter()) {
            response.setCharacterEncoding(UTF_8.toString());
            writer.write("[" + proxy.isRunningHttp() + ", " + proxy.isRunningHttps() + "]");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

}