package web.servlets;

import org.apache.log4j.Logger;
import util.LogUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LogsServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LogsServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setCharacterEncoding(UTF_8.toString());
            String date = request.getParameter("date");
            if (date == null) {
                response.getWriter().write(LogUtil.getCurrentLog());
                response.getWriter().flush();
            } else {

            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

}
