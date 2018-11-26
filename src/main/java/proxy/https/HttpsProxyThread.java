package proxy.https;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Поток на запуск HTTPS прокси-сервера
 */
public class HttpsProxyThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(HttpsProxyThread.class);
    private ExecutorService executorService;
    private ServerSocket sslServerSocket;

    public HttpsProxyThread(ServerSocket sslServerSocket, int threadsCount) {
        this.executorService = Executors.newFixedThreadPool(threadsCount);
        this.sslServerSocket = sslServerSocket;
    }

    @Override
    public void run() {
        while (true) {
            if (sslServerSocket == null || sslServerSocket.isClosed()) {
                break;
            } else {
                try {
                    executorService.execute(new HttpsClientProxyThread(sslServerSocket.accept()));
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

}
