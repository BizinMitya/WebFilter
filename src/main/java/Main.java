import classificators.bayes.BayesClassifier;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import servlets.*;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Security;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class);
    private static final int PORT = 8080;
    private static final String LOCALHOST_URL = "http://localhost:" + PORT + "/";

    public static void main(String[] args) {
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            BayesClassifier.learn();

            Server server = new Server(PORT);

            ServletHandler servletHandler = new ServletHandler();
            addServlets(servletHandler);

            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setDirectoriesListed(true);
            resourceHandler.setWelcomeFiles(new String[]{"/html/index.html"});
            resourceHandler.setResourceBase(Main.class.getResource("/web").toExternalForm());

            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[]{resourceHandler, servletHandler});

            server.setHandler(handlers);
            server.start();
            LOGGER.info("WebFilter запущен");
            openInBrowser(LOCALHOST_URL);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static void addServlets(ServletHandler servletHandler) {
        servletHandler.addServletWithMapping(StartHttpProxyServlet.class, "/proxy/startHttp");
        servletHandler.addServletWithMapping(StopHttpProxyServlet.class, "/proxy/stopHttp");
        servletHandler.addServletWithMapping(StartHttpsProxyServlet.class, "/proxy/startHttps");
        servletHandler.addServletWithMapping(StopHttpsProxyServlet.class, "/proxy/stopHttps");
        servletHandler.addServletWithMapping(StatusProxyServlet.class, "/proxy/status");
        servletHandler.addServletWithMapping(SettingsProxyServlet.class, "/proxy/settings");
        servletHandler.addServletWithMapping(BlacklistProxyServlet.class, "/proxy/blacklist");
        servletHandler.addServletWithMapping(MonitoringServlet.class, "/proxy/monitoring");
    }

    private static void openInBrowser(String url) {
        try {
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                LOGGER.warn("Операция открытия в браузере не поддерживается!");
                return;
            }
            desktop.browse(new URL(url).toURI());
        } catch (IOException | URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
