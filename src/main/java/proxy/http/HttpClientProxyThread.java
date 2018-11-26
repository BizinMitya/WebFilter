package proxy.http;

import model.HttpRequest;
import model.HttpResponse;
import org.apache.log4j.Logger;
import proxy.ProxyHandler;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static dao.SettingsDAO.*;
import static model.HttpRequest.readHttpRequest;

/**
 * Поток для клиента HTTP прокси-сервера
 */
public class HttpClientProxyThread implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(HttpClientProxyThread.class);
    private int timeoutForClient;// таймаут на чтение данных от клиента (браузера)

    private Socket socket;

    public HttpClientProxyThread(Socket socket) throws SocketException {
        setSettings();
        socket.setSoTimeout(timeoutForClient);
        this.socket = socket;
    }

    private void setSettings() {
        String timeoutForClientString = getSettingByKey(TIMEOUT_FOR_CLIENT, String.valueOf(DEFAULT_TIMEOUT_FOR_CLIENT));
        timeoutForClient = Integer.parseInt(timeoutForClientString);
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
            ProxyHandler proxyHandler = new ProxyHandler();
            HttpRequest httpRequestFromClient = readHttpRequest(socket.getInputStream());// парсинг http-запроса от браузера
            HttpResponse httpResponseFromServer = proxyHandler.toServer(httpRequestFromClient);// отправка запроса на сервер (предварительная обработка) и получение ответа от него
            HttpResponse httpResponseToClient = proxyHandler.fromServer(httpResponseFromServer);// обработка ответа от сервера
            socket.getOutputStream().write(httpResponseToClient.getAllResponseInBytes());// отправка запроса обратно браузеру
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
