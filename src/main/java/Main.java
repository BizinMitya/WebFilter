import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class);
    private static final int THREADS_COUNT = 5;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS_COUNT);
        try (ServerSocket serverSocket = new ServerSocket(3333)) {
            while (true) {
                executor.execute(new ProxyThread(serverSocket.accept()));
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
