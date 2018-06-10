import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import web.servlets.*;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class);
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try {
            Server server = new Server(PORT);

            ServletHandler servletHandler = new ServletHandler();
            addServlets(servletHandler);

            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setDirectoriesListed(true);
            resourceHandler.setWelcomeFiles(new String[]{"html/index.html"});
            resourceHandler.setResourceBase("src/main/resources/");

            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[]{resourceHandler, servletHandler});

            server.setHandler(handlers);
            server.start();
            LOGGER.info("WebFilter запущен");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static void addServlets(ServletHandler servletHandler) {
        servletHandler.addServletWithMapping(StartProxyServlet.class, "/proxy/start");
        servletHandler.addServletWithMapping(StopProxyServlet.class, "/proxy/stop");
        servletHandler.addServletWithMapping(StatusProxyServlet.class, "/proxy/status");
        servletHandler.addServletWithMapping(SettingsProxyServlet.class, "/proxy/settings");
        servletHandler.addServletWithMapping(LogsServlet.class, "/logs");
    }

}
