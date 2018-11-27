package proxy.https;

import model.WebRequest;
import model.WebResponse;
import org.apache.log4j.Logger;
import org.bouncycastle.crypto.tls.TlsServerProtocol;
import proxy.ProxyHandler;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.security.SecureRandom;

import static dao.SettingsDAO.*;
import static model.WebRequest.readWebRequest;

/**
 * Поток для клиента HTTPS прокси-сервера
 */
public class HttpsClientProxyThread implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(HttpsClientProxyThread.class);

    private Socket socket;

    public HttpsClientProxyThread(Socket socket) throws SocketException {
        String timeoutForClientString = getSettingByKey(TIMEOUT_FOR_CLIENT, String.valueOf(DEFAULT_TIMEOUT_FOR_CLIENT));
        int timeoutForClient = Integer.parseInt(timeoutForClientString);
        socket.setSoTimeout(timeoutForClient);
        this.socket = socket;
    }

    @Override
    public void run() {
        TlsServerProtocol tlsServerProtocol = null;
        try {
            WebRequest connectWebRequest = WebRequest.readWebRequest(socket.getInputStream());
            String host = connectWebRequest.getHost();
            if (connectWebRequest.isConnectMethod()) {
                sendOkToConnect(socket);

                tlsServerProtocol = new TlsServerProtocol(socket.getInputStream(),
                        socket.getOutputStream(), new SecureRandom());
                tlsServerProtocol.accept(new FakeTlsServer(host));

                WebRequest webRequestFromClient = readWebRequest(tlsServerProtocol.getInputStream());// парсинг https-запроса от браузера
                WebResponse webResponseFromServer = ProxyHandler.doHttpsRequestToServer(webRequestFromClient);// отправка запроса на сервер (предварительная обработка) и получение ответа от него
                WebResponse webResponseToClient = ProxyHandler.fromServer(webResponseFromServer);// обработка ответа от сервера
                tlsServerProtocol.getOutputStream().write(webResponseToClient.getAllResponseInBytes());// отправка запроса обратно браузеру
                tlsServerProtocol.getOutputStream().flush();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                if (tlsServerProtocol != null) {
                    tlsServerProtocol.close();
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void sendOkToConnect(Socket sslSocket) throws IOException {
        WebResponse webResponse = new WebResponse();
        webResponse.setStatusCode(200);
        webResponse.setReasonPhrase("OK");
        webResponse.setVersion("HTTP/1.1");
        sslSocket.getOutputStream().write(webResponse.getAllResponseInBytes());
        sslSocket.getOutputStream().flush();
    }

}
