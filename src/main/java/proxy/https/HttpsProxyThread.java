package proxy.https;

import org.apache.log4j.Logger;
import proxy.ThreadService;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Поток HTTPS прокси-сервера (обрабатывает входящие по HTTPS соединения)
 */
public class HttpsProxyThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(HttpsProxyThread.class);
    private ServerSocket httpsServerSocket;

    public HttpsProxyThread(ServerSocket httpsServerSocket) {
        this.httpsServerSocket = httpsServerSocket;
    }

    @Override
    public void run() {
        while (true) {
            if (httpsServerSocket == null || httpsServerSocket.isClosed()) {
                break;
            } else {
                try {
                    ThreadService.execute(new HttpsClientProxyThread(httpsServerSocket.accept()));
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

}
