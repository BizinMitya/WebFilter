package proxy.https;

import org.apache.log4j.Logger;

import java.net.Socket;
import java.net.SocketException;

import static dao.SettingsDAO.*;

/**
 * Поток для клиента HTTPS прокси-сервера
 */
public class HttpsClientProxyThread implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(HttpsClientProxyThread.class);
    private int timeoutForClient;//таймаут на чтение данных от клиента (браузера)

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

    }

}
