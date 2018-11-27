package proxy.http;

import model.WebRequest;
import model.WebResponse;
import org.apache.log4j.Logger;
import proxy.ProxyHandler;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static dao.SettingsDAO.*;
import static model.WebRequest.readWebRequest;

/**
 * Поток для клиента HTTP прокси-сервера
 */
public class HttpClientProxyThread implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(HttpClientProxyThread.class);

    private Socket socket;

    HttpClientProxyThread(Socket socket) throws SocketException {
        String timeoutForClientString = getSettingByKey(TIMEOUT_FOR_CLIENT, String.valueOf(DEFAULT_TIMEOUT_FOR_CLIENT));
        int timeoutForClient = Integer.parseInt(timeoutForClientString);
        socket.setSoTimeout(timeoutForClient);
        this.socket = socket;
    }

    /**
     * 1. Прочитать данные от клиента
     * 2. Отправить данные на сервер
     * 3. Прочитать ответ от сервера
     * 4. Отправить ответ от сервера клиенту
     */
    @Override
    public void run() {
        try {
            WebRequest webRequestFromClient = readWebRequest(socket.getInputStream());// парсинг http-запроса от браузера
            WebResponse webResponseFromServer = ProxyHandler.doHttpRequestToServer(webRequestFromClient);// отправка запроса на сервер (предварительная обработка) и получение ответа от него
            WebResponse webResponseToClient = ProxyHandler.fromServer(webResponseFromServer);// обработка ответа от сервера
            socket.getOutputStream().write(webResponseToClient.getAllResponseInBytes());// отправка запроса обратно браузеру
            socket.getOutputStream().flush();
        } catch (IOException e) {
            if (!(e instanceof SocketException) && !(e instanceof SocketTimeoutException)) {
                LOGGER.error(e.getMessage(), e);
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

}
