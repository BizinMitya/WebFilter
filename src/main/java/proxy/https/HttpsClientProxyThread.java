package proxy.https;

import model.WebRequest;
import model.WebResponse;
import org.apache.log4j.Logger;
import org.bouncycastle.crypto.tls.TlsServerProtocol;
import proxy.ProxyHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        try (InputStream socketInputStream = socket.getInputStream();
             OutputStream socketOutputStream = socket.getOutputStream()) {
            WebRequest connectWebRequest = readWebRequest(socketInputStream);
            String host = connectWebRequest.getHost();
            if (connectWebRequest.isConnectMethod()) {
                sendOkToConnect(socket);
                tlsServerProtocol = new TlsServerProtocol(socketInputStream,
                        socketOutputStream, new SecureRandom());
                tlsServerProtocol.accept(new FakeTlsServer(host));
                try (InputStream tlsInputStream = tlsServerProtocol.getInputStream();
                     OutputStream tlsOutputStream = tlsServerProtocol.getOutputStream()) {
                    WebRequest webRequestFromClient = readWebRequest(tlsInputStream);// парсинг https-запроса от браузера
                    WebResponse webResponseFromServer = ProxyHandler.doHttpsRequestToServer(webRequestFromClient);// отправка запроса на сервер (предварительная обработка) и получение ответа от него
                    WebResponse webResponseToClient = ProxyHandler.fromServer(webResponseFromServer);// обработка ответа от сервера
                    tlsOutputStream.write(webResponseToClient.getAllResponseInBytes());// отправка запроса обратно браузеру
                }
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
        webResponse.setStatusCode(HttpServletResponse.SC_OK);
        webResponse.setReasonPhrase("OK");
        webResponse.setVersion("HTTP/1.1");
        sslSocket.getOutputStream().write(webResponse.getAllResponseInBytes());
        sslSocket.getOutputStream().flush();
    }

}
