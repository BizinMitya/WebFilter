package proxy.http;

import model.WebRequest;
import model.WebResponse;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import proxy.ProxyHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static dao.SettingsDAO.*;
import static model.WebRequest.readWebRequest;

/**
 * Поток для клиента HTTP прокси-сервера
 */
class HttpClientProxyThread implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(HttpClientProxyThread.class);

    private Socket socket;

    HttpClientProxyThread(@NotNull Socket socket) throws SocketException {
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
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            WebRequest webRequestFromClient = readWebRequest(inputStream);// парсинг http-запроса от браузера
            if (webRequestFromClient != null) {
                WebResponse webResponseFromServer = ProxyHandler.doHttpRequestToServer(webRequestFromClient);// отправка запроса на сервер (предварительная обработка) и получение ответа от него
                WebResponse webResponseToClient = ProxyHandler.fromServer(webResponseFromServer);// обработка ответа от сервера
                outputStream.write(webResponseToClient.getAllResponseInBytes());// отправка запроса обратно браузеру
            }
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
