package proxy;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Поток на запуск прокси-сервера
 */
public class ProxyThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(ProxyThread.class);
    private ExecutorService executorService;
    private ServerSocket serverSocket;

    public ProxyThread(ServerSocket serverSocket, int threadsCount) {
        this.executorService = Executors.newFixedThreadPool(threadsCount);
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                while (true) {
                    executorService.execute(new ClientProxyThread(serverSocket.accept()));
                }
            } catch (IOException e) {
                LOGGER.warn("Прокси-сервер завершил работу");
            }
        }
    }
}
