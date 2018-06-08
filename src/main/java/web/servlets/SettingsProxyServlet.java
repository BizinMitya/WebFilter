package web.servlets;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import proxy.Proxy;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static util.SettingsUtil.*;

public class SettingsProxyServlet extends HttpServlet {

    private static final String UTF_8 = "UTF-8";
    private static final Logger LOGGER = Logger.getLogger(SettingsProxyServlet.class);
    private Proxy proxy = Proxy.getInstance();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, String> settings = getAllSettingsAsMap();
            response.setCharacterEncoding(UTF_8);
            response.getWriter().write(new JSONObject(settings).toString());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.setCharacterEncoding(UTF_8);
            JSONObject settingsJson = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
            saveSettingsWithValidation(settingsJson);
            if (proxy.isRunning()) {
                proxy.restart();
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void saveSettingsWithValidation(JSONObject settingsJson) {
        int proxyPort = settingsJson.has(PROXY_PORT) ?
                settingsJson.optInt(PROXY_PORT, DEFAULT_PROXY_PORT) :
                DEFAULT_PROXY_PORT;
        int threadsCount = settingsJson.has(THREADS_COUNT) ?
                settingsJson.optInt(THREADS_COUNT, DEFAULT_THREADS_COUNT) :
                DEFAULT_THREADS_COUNT;
        int timeoutForClient = settingsJson.has(TIMEOUT_FOR_CLIENT) ?
                settingsJson.optInt(TIMEOUT_FOR_CLIENT, DEFAULT_TIMEOUT_FOR_CLIENT) :
                DEFAULT_TIMEOUT_FOR_CLIENT;
        int timeoutForServer = settingsJson.has(TIMEOUT_FOR_SERVER) ?
                settingsJson.optInt(TIMEOUT_FOR_SERVER, DEFAULT_TIMEOUT_FOR_SERVER) :
                DEFAULT_TIMEOUT_FOR_SERVER;
        if (proxyPort < MIN_PROXY_PORT || proxyPort > MAX_PROXY_PORT) {
            proxyPort = DEFAULT_PROXY_PORT;
        }
        if (threadsCount < MIN_THREADS_COUNT || threadsCount > MAX_THREADS_COUNT) {
            threadsCount = DEFAULT_THREADS_COUNT;
        }
        if (timeoutForClient < MIN_TIMEOUT_FOR_CLIENT || timeoutForClient > MAX_TIMEOUT_FOR_CLIENT) {
            timeoutForClient = DEFAULT_TIMEOUT_FOR_CLIENT;
        }
        if (timeoutForServer < MIN_TIMEOUT_FOR_SERVER || timeoutForServer > MAX_TIMEOUT_FOR_SERVER) {
            timeoutForServer = DEFAULT_TIMEOUT_FOR_SERVER;
        }
        Properties properties = new Properties();
        properties.setProperty(PROXY_PORT, String.valueOf(proxyPort));
        properties.setProperty(THREADS_COUNT, String.valueOf(threadsCount));
        properties.setProperty(TIMEOUT_FOR_CLIENT, String.valueOf(timeoutForClient));
        properties.setProperty(TIMEOUT_FOR_SERVER, String.valueOf(timeoutForServer));
        saveAllSettings(properties);
    }

}
