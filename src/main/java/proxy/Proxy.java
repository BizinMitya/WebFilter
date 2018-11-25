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
    private ServerSocket serverSocket;
    private ServerSocket sslServerSocket;

    private Proxy() {
    }

    public static Proxy getInstance() {
        return instance;
    }

    private void setSettings() {
        String threadsCountString = getSettingByKey(THREADS_COUNT, String.valueOf(DEFAULT_THREADS_COUNT));
        threadsCount = Integer.parseInt(threadsCountString);
        String httpProxyPortString = getSettingByKey(HTTP_PROXY_PORT, String.valueOf(DEFAULT_HTTP_PROXY_PORT));
        httpProxyPort = Integer.parseInt(httpProxyPortString);
        String httpsProxyPortString = getSettingByKey(HTTPS_PROXY_PORT, String.valueOf(DEFAULT_HTTPS_PROXY_PORT));
        httpsProxyPort = Integer.parseInt(httpsProxyPortString);
    }

    public synchronized void startHttp() {
        try {
            setSettings();
            serverSocket = new ServerSocket(httpProxyPort);
            new HttpProxyThread(serverSocket, threadsCount).start();
            LOGGER.info("HTTP прокси-сервер запущен на порту " + httpProxyPort);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            try {
                serverSocket.close();
                LOGGER.info("HTTP прокси-сервер завершил работу!");
            } catch (IOException t) {
                LOGGER.error(t.getMessage(), t);
            }
        }
    }

    public synchronized void startHttps() {
        try {
            setSettings();
            sslServerSocket = new ServerSocket(httpsProxyPort);
            new HttpsProxyThread(sslServerSocket, threadsCount).start();
            LOGGER.info("HTTPS прокси-сервер запущен на порту " + httpsProxyPort);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            try {
                sslServerSocket.close();
                LOGGER.info("HTTPS прокси-сервер завершил работу!");
            } catch (IOException t) {
                LOGGER.error(t.getMessage(), t);
            }
        }
    }

    public synchronized void stopHttp() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                LOGGER.info("HTTP прокси-сервер остановлен");
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public synchronized void stopHttps() {
        try {
            if (sslServerSocket != null && !sslServerSocket.isClosed()) {
                sslServerSocket.close();
                LOGGER.info("HTTPS прокси-сервер остановлен");
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public synchronized void restartHttp() {
        if (isRunningHttp()) {
            stopHttp();
            startHttp();
        } else {
            startHttp();
        }
        LOGGER.info("HTTP прокси-сервер перезагружен!");
    }

    public synchronized void restartHttps() {
        if (isRunningHttps()) {
            stopHttps();
            startHttps();
        } else {
            startHttps();
        }
        LOGGER.info("HTTPS прокси-сервер перезагружен!");
    }

    public boolean isRunningHttp() {
        return serverSocket != null && !serverSocket.isClosed();
    }

    public boolean isRunningHttps() {
        return sslServerSocket != null && !sslServerSocket.isClosed();
    }

}
