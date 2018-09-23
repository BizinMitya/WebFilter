package classificators.bayes;

import classificators.Category;
import org.apache.log4j.Logger;
import org.tartarus.snowball.ext.RussianStemmer;
import util.FileUtil;

import java.util.*;

import static classificators.Category.*;

public abstract class BayesClassifier {

    private static final Logger LOGGER = Logger.getLogger(BayesClassifier.class);

    private static final Category[] CATEGORIES = {
            ADVERTISING,
            DRUGS,
            GAMES,
            VIOLENCE,
            WEAPON
    };

    /**
     * Карта категория -> (карта ключевое слово -> принадлежность ключевого слова этой категории).
     * Во второй карте присутствуют ВСЕ ключевые слова из всех категорий.
     */
    private static final Map<Category, Map<String, Boolean>> categoryToContainsKeywordsMap = new HashMap<>();

    /**
     * Карта категория -> вероятность этой категории (Dc/D)
     */
    private static final Map<Category, Double> categoryProbabilityMap = new HashMap<>();

    /**
     * Карта категория -> количество ключевых слов в этой категории
     */
    private static final Map<Category, Integer> categoryCountKeywordsMap = new HashMap<>();

    /**
     * Метод для обучения (тренировки) классификатора на основе заготовленных наборов слов (документов)
     */
    public static void learn() {
        LOGGER.info("Начало обучения классификатора");
        Map<Category, List<String>> categoryToKeywordsMap = FileUtil.getLearnKeywordsFromFiles();
        for (Map.Entry<Category, List<String>> entry : categoryToKeywordsMap.entrySet()) {
            List<String> keywords = entry.getValue();
            for (int i = 0; i < keywords.size(); i++) {
                keywords.set(i, keywords.get(i).toLowerCase());
            }
        }
        Set<String> allKeywords = new HashSet<>();// уникальные ключевые слова из ВСЕХ категорий
        for (List<String> keywords : categoryToKeywordsMap.values()) {
            allKeywords.addAll(keywords);
        }
        for (Category category : CATEGORIES) {
            List<String> keywordsForCategory = categoryToKeywordsMap.get(category);
            categoryCountKeywordsMap.put(category, keywordsForCategory.size());
            categoryProbabilityMap.put(category, (double) keywordsForCategory.size() / allKeywords.size());
            Map<String, Boolean> keywordsContainsMap = new HashMap<>();
            for (String keyword : allKeywords) {
                keywordsContainsMap.put(keyword, keywordsForCategory.contains(keyword));
            }
            categoryToContainsKeywordsMap.put(category, keywordsContainsMap);
        }
        LOGGER.info("Конец обучения классификатора");
    }

    /**
     * Метод для классификации набора слов (документа)
     *
     * @param words набор слов (документ)
     * @return карта категория -> вероятность попадания набора слов (документа) в эту категорию
     */
    public static Map<Category, Double> classify(List<String> words) {
        words.removeIf(s -> s.length() < 4);
        RussianStemmer russianStemmer = new RussianStemmer();
        for (int i = 0; i < words.size(); i++) {
            russianStemmer.setCurrent(words.get(i).toLowerCase());
            russianStemmer.stem();
            words.set(i, russianStemmer.getCurrent());
        }
        Map<Category, Double> categoryProbabilityMap = new HashMap<>();
        for (Category category : CATEGORIES) {
            Map<String, Boolean> keywordsMap = categoryToContainsKeywordsMap.get(category);// V, все ключевые слова
            double d = Math.log(BayesClassifier.categoryProbabilityMap.get(category));// log(Dc/D)
            double sum = 0d;// сумма логарифмов
            for (String word : words) {
                long denominator = categoryToContainsKeywordsMap.size() + categoryCountKeywordsMap.size();
                if (keywordsMap.containsKey(word)) {
                    sum += Math.log((1d + (keywordsMap.get(word) ? 1 : 0)) / denominator);
                } else {
                    sum += Math.log(1d / denominator);
                }
            }
            double resultForCategory = d + sum;// значение логарифма для категории
            categoryProbabilityMap.put(category, resultForCategory);
        }
        Map<Category, Double> copy = new HashMap<>(categoryProbabilityMap);
        for (Map.Entry<Category, Double> entry : categoryProbabilityMap.entrySet()) {
            double s = 0d;
            for (Category category : CATEGORIES) {
                if (!entry.getKey().equals(category)) {
                    s += Math.exp(copy.get(category) - entry.getValue());
                }
            }
            entry.setValue(1d / (1d + s));// делаем из логарифма вероятность (нормируем)
        }
        return categoryProbabilityMap;
    }

}
