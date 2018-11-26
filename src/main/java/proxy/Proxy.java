package proxy;

import org.apache.log4j.Logger;
import proxy.http.HttpProxyThread;
import proxy.https.HttpsProxyThread;

import java.io.IOException;
import java.net.ServerSocket;

import static dao.SettingsDAO.*;

// Singleton
public class Proxy {

    private static final Logger LOGGER = Logger.getLogger(Proxy.class);
    private static Proxy instance = new Proxy();
    private int threadsCount;
    private int httpProxyPort;
    private int httpsProxyPort;
    private ServerSocket httpServerSocket;
    private ServerSocket httpsServerSocket;

    private Proxy() {
    }

    public static Proxy getInstance() {
        return instance;
    }

    private void setSettings() {
        String threadsCountString = getSettingByKey(THREADS_COUNT, String.valueOf(DEFAULT_THREADS_COUNT));
        int threadsCount = Integer.parseInt(threadsCountString);
        if (threadsCount != this.threadsCount) {
            this.threadsCount = threadsCount;
            ThreadService.update(threadsCount);
        }
        String httpProxyPortString = getSettingByKey(HTTP_PROXY_PORT, String.valueOf(DEFAULT_HTTP_PROXY_PORT));
        httpProxyPort = Integer.parseInt(httpProxyPortString);
        String httpsProxyPortString = getSettingByKey(HTTPS_PROXY_PORT, String.valueOf(DEFAULT_HTTPS_PROXY_PORT));
        httpsProxyPort = Integer.parseInt(httpsProxyPortString);
    }

    public void startHttp() {
        try {
            setSettings();
            httpServerSocket = new ServerSocket(httpProxyPort);
            new HttpProxyThread(httpServerSocket).start();
            LOGGER.info("HTTP прокси-сервер запущен на порту " + httpProxyPort);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            try {
                httpServerSocket.close();
                LOGGER.info("HTTP прокси-сервер завершил работу!");
            } catch (IOException t) {
                LOGGER.error(t.getMessage(), t);
            }
        }
    }

    public void startHttps() {
        try {
            setSettings();
            httpsServerSocket = new ServerSocket(httpsProxyPort);
            new HttpsProxyThread(httpsServerSocket).start();
            LOGGER.info("HTTPS прокси-сервер запущен на порту " + httpsProxyPort);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            try {
                httpsServerSocket.close();
                LOGGER.info("HTTPS прокси-сервер завершил работу!");
            } catch (IOException t) {
                LOGGER.error(t.getMessage(), t);
            }
        }
    }

    public void stopHttp() {
        try {
            if (httpServerSocket != null && !httpServerSocket.isClosed()) {
                httpServerSocket.close();
                LOGGER.info("HTTP прокси-сервер остановлен");
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void stopHttps() {
        try {
            if (httpsServerSocket != null && !httpsServerSocket.isClosed()) {
                httpsServerSocket.close();
                LOGGER.info("HTTPS прокси-сервер остановлен");
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void restartHttp() {
        if (isRunningHttp()) {
            stopHttp();
            startHttp();
        } else {
            startHttp();
        }
        LOGGER.info("HTTP прокси-сервер перезагружен!");
    }

    public void restartHttps() {
        if (isRunningHttps()) {
            stopHttps();
            startHttps();
        } else {
            startHttps();
        }
        LOGGER.info("HTTPS прокси-сервер перезагружен!");
    }

    public boolean isRunningHttp() {
        return httpServerSocket != null && !httpServerSocket.isClosed();
    }

    public boolean isRunningHttps() {
        return httpsServerSocket != null && !httpsServerSocket.isClosed();
    }

}
