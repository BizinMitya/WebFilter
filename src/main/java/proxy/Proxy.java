package proxy;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;

import static util.SettingsUtil.*;

//singleton
public class Proxy {

    private static final Logger LOGGER = Logger.getLogger(Proxy.class);
    private static Proxy instance = new Proxy();
    private int threadsCount;
    private int proxyPort;
    private ServerSocket serverSocket;

    private Proxy() {
    }

    public static Proxy getInstance() {
        return instance;
    }

    private void setSettings() {
        String threadsCountString = getSettingByName(THREADS_COUNT, String.valueOf(DEFAULT_THREADS_COUNT));
        if (threadsCountString == null) {
            threadsCount = DEFAULT_THREADS_COUNT;
            LOGGER.warn(String.format("Значение количества потоков для прокси-сервера не установлено! Установлено значение по умолчанию: %d",
                    DEFAULT_THREADS_COUNT));
        } else {
            try {
                threadsCount = Integer.parseInt(threadsCountString);
            } catch (NumberFormatException e) {
                LOGGER.warn(String.format("Значение количества потоков для прокси-сервера (%s) некорректно! Установлено значение по умолчанию: %d",
                        threadsCountString, DEFAULT_THREADS_COUNT), e);
                threadsCount = DEFAULT_THREADS_COUNT;
            }
        }
        String proxyPortString = getSettingByName(PROXY_PORT, String.valueOf(DEFAULT_PROXY_PORT));
        if (proxyPortString == null) {
            proxyPort = DEFAULT_PROXY_PORT;
            LOGGER.warn(String.format("Значение порта прокси-сервера не установлено! Установлено значение по умолчанию: %d",
                    DEFAULT_PROXY_PORT));
        } else {
            try {
                proxyPort = Integer.parseInt(proxyPortString);
            } catch (NumberFormatException e) {
                LOGGER.warn(String.format("Значение порта прокси-сервера (%s) некорректно! Установлено значение по умолчанию: %d",
                        proxyPortString, DEFAULT_PROXY_PORT), e);
                proxyPort = DEFAULT_PROXY_PORT;
            }
        }
    }

    public synchronized void start() {
        try {
            setSettings();
            serverSocket = new ServerSocket(proxyPort);
            new ProxyThread(serverSocket, threadsCount).start();
            LOGGER.info("Прокси-сервер запущен на порту " + proxyPort);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public synchronized void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                LOGGER.info("Прокси-сервер остановлен");
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public synchronized void restart() {
        if (isRunning()) {
            stop();
            start();
        } else {
            start();
        }
    }

    public boolean isRunning() {
        return serverSocket != null && !serverSocket.isClosed();
    }

    public int getThreadsCount() {
        return threadsCount;
    }

    public void setThreadsCount(int threadsCount) {
        this.threadsCount = threadsCount;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }
}
