package servlets;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import proxy.Proxy;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static dao.SettingsDAO.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SettingsProxyServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SettingsProxyServlet.class);
    private Proxy proxy = Proxy.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try (Writer writer = response.getWriter()) {
            Map<String, String> settings = getAllSettings();
            response.setCharacterEncoding(UTF_8.toString());
            writer.write(new JSONObject(settings).toString());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) {
        try (BufferedReader reader = request.getReader()) {
            request.setCharacterEncoding(UTF_8.toString());
            JSONObject settingsJson = new JSONObject(reader.lines().collect(Collectors.joining(System.lineSeparator())));
            saveSettingsWithValidation(settingsJson);
            if (proxy.isRunningHttp()) {
                proxy.restartHttp();
            }
            if (proxy.isRunningHttps()) {
                proxy.restartHttps();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @SuppressWarnings("Duplicates")
    private void saveSettingsWithValidation(JSONObject settingsJson) {
        int httpProxyPort = settingsJson.has(HTTP_PROXY_PORT) ?
                settingsJson.optInt(HTTP_PROXY_PORT, DEFAULT_HTTP_PROXY_PORT) :
                DEFAULT_HTTP_PROXY_PORT;
        int httpsProxyPort = settingsJson.has(HTTPS_PROXY_PORT) ?
                settingsJson.optInt(HTTPS_PROXY_PORT, DEFAULT_HTTPS_PROXY_PORT) :
                DEFAULT_HTTPS_PROXY_PORT;
        if (httpProxyPort == httpsProxyPort) {
            httpProxyPort = DEFAULT_HTTP_PROXY_PORT;
            httpsProxyPort = DEFAULT_HTTPS_PROXY_PORT;
        }
        int threadsCount = settingsJson.has(THREADS_COUNT) ?
                settingsJson.optInt(THREADS_COUNT, DEFAULT_THREADS_COUNT) :
                DEFAULT_THREADS_COUNT;
        int timeoutForClient = settingsJson.has(TIMEOUT_FOR_CLIENT) ?
                settingsJson.optInt(TIMEOUT_FOR_CLIENT, DEFAULT_TIMEOUT_FOR_CLIENT) :
                DEFAULT_TIMEOUT_FOR_CLIENT;
        int timeoutForServer = settingsJson.has(TIMEOUT_FOR_SERVER) ?
                settingsJson.optInt(TIMEOUT_FOR_SERVER, DEFAULT_TIMEOUT_FOR_SERVER) :
                DEFAULT_TIMEOUT_FOR_SERVER;
        if (httpProxyPort < MIN_PROXY_PORT || httpProxyPort > MAX_PROXY_PORT) {
            httpProxyPort = DEFAULT_HTTP_PROXY_PORT;
        }
        if (httpsProxyPort < MIN_PROXY_PORT || httpsProxyPort > MAX_PROXY_PORT) {
            httpsProxyPort = DEFAULT_HTTPS_PROXY_PORT;
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
        Map<String, String> allSettings = new HashMap<>();
        allSettings.put(HTTP_PROXY_PORT, String.valueOf(httpProxyPort));
        allSettings.put(HTTPS_PROXY_PORT, String.valueOf(httpsProxyPort));
        allSettings.put(THREADS_COUNT, String.valueOf(threadsCount));
        allSettings.put(TIMEOUT_FOR_CLIENT, String.valueOf(timeoutForClient));
        allSettings.put(TIMEOUT_FOR_SERVER, String.valueOf(timeoutForServer));
        updateAllSettings(allSettings);
        LOGGER.info("Настройки успешно сохранены");
    }

}
