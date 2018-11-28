package util;

import classificators.Category;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.tartarus.snowball.ext.RussianStemmer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static classificators.Category.*;

public abstract class FileUtil {

    private static final String PATH_TO_HOST_IN_BLACKLIST_PAGE = "/web/html/hostInBlacklist.html";
    private static final Logger LOGGER = Logger.getLogger(FileUtil.class);
    private static final String PATH_TO_LEARN_DIR = "learn/";
    private static final Map<Category, String> pathsToLearnFiles;

    static {
        pathsToLearnFiles = new HashMap<>();
        pathsToLearnFiles.put(ADVERTISING, PATH_TO_LEARN_DIR + "advertising.txt");
        pathsToLearnFiles.put(DRUGS, PATH_TO_LEARN_DIR + "drugs.txt");
        pathsToLearnFiles.put(GAMES, PATH_TO_LEARN_DIR + "games.txt");
        pathsToLearnFiles.put(VIOLENCE, PATH_TO_LEARN_DIR + "violence.txt");
        pathsToLearnFiles.put(WEAPON, PATH_TO_LEARN_DIR + "weapon.txt");
    }

    @Nullable
    public static byte[] getHostInBlacklistPage() {
        byte[] result = null;
        try (InputStream inputStream = FileUtil.class.getResourceAsStream(PATH_TO_HOST_IN_BLACKLIST_PAGE)) {
            result = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Подготовка файлов с ключевыми словами - стемминг ключевых слов в каждом файле
     */
    private static void prepareLearnKeywords() {
        try {
            RussianStemmer russianStemmer = new RussianStemmer();
            for (Map.Entry<Category, String> entry : pathsToLearnFiles.entrySet()) {
                Path path = Paths.get(entry.getValue());
                List<String> lines = Files.readAllLines(path);
                for (int i = 0; i < lines.size(); i++) {
                    russianStemmer.setCurrent(lines.get(i));
                    russianStemmer.stem();
                    lines.set(i, russianStemmer.getCurrent());
                }
                Files.write(path, lines, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Метод чтения ключевых слов из файлов для каждой категории
     *
     * @return карта категория -> набор ключевых слов (документ) этой категории
     */
    public static Map<Category, List<String>> getLearnKeywordsFromFiles() {
        prepareLearnKeywords();
        Map<Category, List<String>> result = new HashMap<>();
        try {
            for (Map.Entry<Category, String> entry : pathsToLearnFiles.entrySet()) {
                result.put(entry.getKey(),
                        Files.lines(Paths.get(entry.getValue())).collect(Collectors.toList()));
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

}
