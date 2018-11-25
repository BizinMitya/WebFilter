package proxy.https;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.tls.TlsServerProtocol;
import proxy.model.HttpRequest;
import proxy.model.HttpResponse;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
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

    private void sendOkToConnect(Socket sslSocket) throws IOException {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(200);
        httpResponse.setReasonPhrase("OK");
        httpResponse.setVersion("HTTP/1.1");
        sslSocket.getOutputStream().write(httpResponse.getAllResponseInBytes());
        sslSocket.getOutputStream().flush();
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void run() {
        while (true) {
            if (sslServerSocket == null || sslServerSocket.isClosed()) {
                break;
            } else {
                try (Socket sslSocket = sslServerSocket.accept()) {
                    sslSocket.setSoTimeout(10_000);
                    //executorService.execute(new HttpsClientProxyThread(sslServerSocket.accept()));
                    TlsServerProtocol tlsServerProtocol;
                    HttpRequest httpRequest = HttpRequest.readHttpRequest(sslSocket.getInputStream());
                    String host = httpRequest.getHost();
                    if (httpRequest.isConnectMethod()) {
                        sendOkToConnect(sslSocket);

                        tlsServerProtocol = new TlsServerProtocol(
                                sslSocket.getInputStream(), sslSocket.getOutputStream(), new SecureRandom());

                        tlsServerProtocol.accept(new FakeTlsServer(host, tlsServerProtocol));

                    }
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

}
