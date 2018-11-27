package servlets;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DownloadRootCertificateServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DownloadRootCertificateServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            byte[] rootCertificateBytes = IOUtils.toByteArray(DownloadRootCertificateServlet.class.getResourceAsStream("/cert/WebFilterRoot.crt"));
            response.setContentType("application/x-x509-ca-cert");
            response.setContentLength(rootCertificateBytes.length);
            response.setHeader("Content-Disposition", "attachment; filename=\"WebFilterRoot.crt\"");
            response.getOutputStream().write(rootCertificateBytes);
            response.getOutputStream().close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
