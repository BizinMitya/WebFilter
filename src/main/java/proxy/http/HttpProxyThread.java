package proxy.http;

import org.apache.log4j.Logger;
import proxy.ThreadService;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Поток HTTP прокси-сервера (обрабатывает входящие по HTTP соединения)
 */
public class HttpProxyThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(HttpProxyThread.class);
    private ServerSocket httpServerSocket;

    public HttpProxyThread(ServerSocket httpServerSocket) {
        this.httpServerSocket = httpServerSocket;
    }

    @Override
    public void run() {
        while (true) {
            if (httpServerSocket == null || httpServerSocket.isClosed()) {
                break;
            } else {
                try {
                    ThreadService.execute(new HttpClientProxyThread(httpServerSocket.accept()));
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

}
