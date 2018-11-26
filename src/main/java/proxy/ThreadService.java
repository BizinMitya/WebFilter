package proxy;

import dao.SettingsDAO;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dao.SettingsDAO.DEFAULT_THREADS_COUNT;
import static dao.SettingsDAO.THREADS_COUNT;

/**
 * Сервис, обеспечивающий общий пул потоков для HTTP и HTTPS прокси
 */
public abstract class ThreadService {

    private static ExecutorService executorService;

    static {
        String threadsCountString = SettingsDAO.getSettingByKey(THREADS_COUNT, String.valueOf(DEFAULT_THREADS_COUNT));
        int threadsCount = Integer.parseInt(threadsCountString);
        executorService = Executors.newFixedThreadPool(threadsCount);
    }

    public static void execute(Runnable runnable) {
        executorService.execute(runnable);
    }

    public static void update(int threadsCount) {
        if (executorService != null) {
            executorService.shutdown();
        }
        executorService = Executors.newFixedThreadPool(threadsCount);
    }

}
