package proxy;

import org.apache.log4j.Logger;
import proxy.model.HttpRequest;
import proxy.model.HttpResponse;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static proxy.model.HttpRequest.readHttpRequest;
import static util.SettingsUtil.*;

/**
 * Поток для клиента прокси-сервера
 */
public class ClientProxyThread implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientProxyThread.class);
    private int timeoutForClient;//таймаут на чтение данных от клиента (браузера)

    private Socket socket;

    public ClientProxyThread(Socket socket) throws SocketException {
        setSettings();
        socket.setSoTimeout(timeoutForClient);
        this.socket = socket;
    }

    private void setSettings() {
        String timeoutForClientString = getSettingByName(TIMEOUT_FOR_CLIENT, String.valueOf(DEFAULT_TIMEOUT_FOR_CLIENT));
        if (timeoutForClientString == null) {
            timeoutForClient = DEFAULT_TIMEOUT_FOR_CLIENT;
            LOGGER.warn(String.format("Значение таймаута для клиента не установлено! Установлено значение по умолчанию: %d",
                    DEFAULT_TIMEOUT_FOR_CLIENT));
        } else {
            try {
                timeoutForClient = Integer.parseInt(timeoutForClientString);
            } catch (NumberFormatException e) {
                LOGGER.warn(String.format("Значение таймаута для клиента (%s) некорректно! Установлено значение по умолчанию: %d",
                        timeoutForClientString, DEFAULT_TIMEOUT_FOR_CLIENT), e);
                timeoutForClient = DEFAULT_TIMEOUT_FOR_CLIENT;
            }
        }
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
            HttpRequest httpRequestFromClient = readHttpRequest(socket.getInputStream());//парсинг http-запроса от браузера
            HttpResponse httpResponseFromServer = proxyHandler.toServer(httpRequestFromClient);//отправка запроса на сервер (предварительная обработка) и получение ответа от него
            HttpResponse httpResponseToClient = proxyHandler.fromServer(httpResponseFromServer);//обработка ответа от сервера
            socket.getOutputStream().write(httpResponseToClient.getAllResponseInBytes());//отправка запроса обратно браузеру
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
