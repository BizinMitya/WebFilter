package util;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.tartarus.snowball.ext.RussianStemmer;

import java.io.File;
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
import java.util.stream.Stream;

public abstract class FileUtil {

    private static final String PATH_TO_HOST_IN_BLACKLIST_PAGE = "/web/html/hostInBlacklist.html";
    private static final Logger LOGGER = Logger.getLogger(FileUtil.class);
    private static final String PATH_TO_LEARN_DIR = "learn/";
    private static final Map<String, String> pathsToLearnFiles;

    static {
        pathsToLearnFiles = new HashMap<>();
        File learnDir = new File(PATH_TO_LEARN_DIR);
        String[] fileNames = learnDir.list((folder, name) -> name.endsWith(".txt"));
        if (fileNames != null) {
            Stream.of(fileNames)
                    .distinct()
                    .map(s -> s.substring(0, s.lastIndexOf('.')))
                    .forEach(s -> pathsToLearnFiles.put(s, PATH_TO_LEARN_DIR + s + ".txt"));
        }
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
            for (Map.Entry<String, String> entry : pathsToLearnFiles.entrySet()) {
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
    public static Map<String, List<String>> getLearnKeywordsFromFiles() {
        prepareLearnKeywords();
        Map<String, List<String>> result = new HashMap<>();
        try {
            for (Map.Entry<String, String> entry : pathsToLearnFiles.entrySet()) {
                result.put(entry.getKey(),
                        Files.lines(Paths.get(entry.getValue())).collect(Collectors.toList()));
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

}
