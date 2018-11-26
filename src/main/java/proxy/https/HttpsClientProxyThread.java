package proxy.https;

import model.HttpRequest;
import model.HttpResponse;
import org.apache.log4j.Logger;
import org.bouncycastle.crypto.tls.TlsServerProtocol;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.security.SecureRandom;

import static dao.SettingsDAO.*;

/**
 * Поток для клиента HTTPS прокси-сервера
 */
public class HttpsClientProxyThread implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(HttpsClientProxyThread.class);
    private int timeoutForClient;// таймаут на чтение данных от клиента (браузера)

    private Socket sslSocket;

    public HttpsClientProxyThread(Socket sslSocket) throws SocketException {
        setSettings();
        sslSocket.setSoTimeout(timeoutForClient);
        this.sslSocket = sslSocket;
    }

    private void setSettings() {
        String timeoutForClientString = getSettingByKey(TIMEOUT_FOR_CLIENT, String.valueOf(DEFAULT_TIMEOUT_FOR_CLIENT));
        timeoutForClient = Integer.parseInt(timeoutForClientString);
    }

    @Override
    public void run() {
        try {
            HttpRequest httpRequest = HttpRequest.readHttpRequest(sslSocket.getInputStream());
            String host = httpRequest.getHost();
            if (httpRequest.isConnectMethod()) {
                sendOkToConnect(sslSocket);
                TlsServerProtocol tlsServerProtocol = new TlsServerProtocol(
                        sslSocket.getInputStream(), sslSocket.getOutputStream(), new SecureRandom());
                tlsServerProtocol.accept(new FakeTlsServer(host, tlsServerProtocol));
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                sslSocket.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void sendOkToConnect(Socket sslSocket) throws IOException {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(200);
        httpResponse.setReasonPhrase("OK");
        httpResponse.setVersion("HTTP/1.1");
        sslSocket.getOutputStream().write(httpResponse.getAllResponseInBytes());
        sslSocket.getOutputStream().flush();
    }

}
