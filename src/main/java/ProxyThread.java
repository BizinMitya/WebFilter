import model.HttpRequest;
import model.HttpResponse;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Socket;

import static model.HttpRequest.readHttpRequest;

public class ProxyThread implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ProxyThread.class);
    //браузер не принимает ответ от прокси
    private static final String SOCKET_WRITE_ERROR = "Software caused connection abort: socket write error";

    private Socket socket;

    public ProxyThread(Socket socket) {
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
            Proxy proxy = new ProxyServer();
            HttpRequest httpRequestFromClient = readHttpRequest(socket.getInputStream());//парсинг http-запроса от браузера
            HttpResponse httpResponseFromServer = proxy.toServer(httpRequestFromClient);//отправка запроса на сервер (предварительная обработка) и получение ответа от него
            HttpResponse httpResponseToClient = proxy.fromServer(httpResponseFromServer);//обработка ответа от сервера
            socket.getOutputStream().write(httpResponseToClient.getAllResponseInBytes());//отправка запроса обратно браузеру
            socket.getOutputStream().flush();
        } catch (IOException e) {
            if (!SOCKET_WRITE_ERROR.equals(e.getMessage())) {
                LOGGER.error(e.getMessage(), e);
            }
        } catch (NullPointerException ignored) {

        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

}
