package util;

import classificators.Category;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static classificators.Category.*;

public abstract class FileUtil {

    private static final String PATH_TO_HOST_IN_BLACKLIST_PAGE = "/html/hostInBlacklist.html";
    private static final Logger LOGGER = Logger.getLogger(FileUtil.class);
    private static final String PATH_TO_LEARN_DIR = "/learn";
    private static final Map<Category, String> pathsToLearnFiles;

    static {
        pathsToLearnFiles = new HashMap<>();
        pathsToLearnFiles.put(ADVERTISING, PATH_TO_LEARN_DIR + "/advertising.txt");
        pathsToLearnFiles.put(DRUGS, PATH_TO_LEARN_DIR + "/drugs.txt");
        pathsToLearnFiles.put(GAMES, PATH_TO_LEARN_DIR + "/games.txt");
        pathsToLearnFiles.put(VIOLENCE, PATH_TO_LEARN_DIR + "/violence.txt");
        pathsToLearnFiles.put(WEAPON, PATH_TO_LEARN_DIR + "/weapon.txt");
    }

    @Nullable
    public static byte[] getHostInBlacklistPage() {
        byte[] result = null;
        try {
            result = IOUtils.toByteArray(FileUtil.class.getResourceAsStream(PATH_TO_HOST_IN_BLACKLIST_PAGE));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    public static Map<Category, List<String>> getLearnKeywordsFromFiles() {
        Map<Category, List<String>> result = new HashMap<>();
        try {
            for (Map.Entry<Category, String> entry : pathsToLearnFiles.entrySet()) {
                result.put(entry.getKey(),
                        Files.lines(Paths.get(FileUtil.class.getResource(entry.getValue()).toURI()))
                                .collect(Collectors.toList()));
            }
        } catch (URISyntaxException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

}
