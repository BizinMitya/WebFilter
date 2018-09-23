package proxy;

import classificators.bayes.BayesClassifier;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;

import static dao.SettingsDAO.*;

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
        String threadsCountString = getSettingByKey(THREADS_COUNT, String.valueOf(DEFAULT_THREADS_COUNT));
        threadsCount = Integer.parseInt(threadsCountString);
        String proxyPortString = getSettingByKey(PROXY_PORT, String.valueOf(DEFAULT_PROXY_PORT));
        proxyPort = Integer.parseInt(proxyPortString);
    }

    public synchronized void start() {
        try {
            setSettings();
            BayesClassifier.learn();
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
        LOGGER.info("Прокси-сервер перезагружен!");
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
