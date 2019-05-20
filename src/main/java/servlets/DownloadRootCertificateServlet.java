package servlets;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import util.CertUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DownloadRootCertificateServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DownloadRootCertificateServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try (InputStream inputStream = DownloadRootCertificateServlet.class.getResourceAsStream("/cert/" + CertUtil.WEB_FILTER_ROOT_CRT);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] rootCertificateBytes = IOUtils.toByteArray(inputStream);
            response.setContentType("application/x-x509-ca-cert");
            response.setContentLength(rootCertificateBytes.length);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + CertUtil.WEB_FILTER_ROOT_CRT + "\"");
            outputStream.write(rootCertificateBytes);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
