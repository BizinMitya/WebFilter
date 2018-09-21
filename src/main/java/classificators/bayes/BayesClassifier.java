package classificators.bayes;

import classificators.Category;

import java.util.*;

import static classificators.Category.*;

public class BayesClassifier {

    // http://synergy-journal.ru/archive/article1737
    // http://bazhenov.me/blog/2012/06/11/naive-bayes

    /**
     * Карта категория <-> (карта ключевое слово <-> принадлежность ключевого слова этой категории).
     * Во второй карте присутствуют ВСЕ ключевые слова из всех категорий.
     */
    private Map<Category, Map<String, Boolean>> categoryToContainsKeywordsMap;

    /**
     * Карта категория <-> вероятность этой категории (Dc/D)
     */
    private Map<Category, Double> categoryProbabilityMap;

    /**
     * Карта категория <-> количество ключевых слов в этой категории
     */
    private Map<Category, Integer> categoryCountKeywordsMap;

    private static final Category[] CATEGORIES = {
            ADVERTISING,
            DRUGS,
            GAMES,
            VIOLENCE,
            WEAPON
    };

    public BayesClassifier() {
        categoryToContainsKeywordsMap = new HashMap<>();
        categoryProbabilityMap = new HashMap<>();
        categoryCountKeywordsMap = new HashMap<>();
    }

    /**
     * Метод для обучения (тренировки) классификатора на основе заготовленных наборов слов (документов)
     *
     * @param categoryToKeywordsMap карта категория <-> набор ключевых слов (документ) этой категории
     */
    public void learn(Map<Category, List<String>> categoryToKeywordsMap) {
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
    }

    /**
     * Метод для классификации набора слов (документа)
     *
     * @param words набор слов (документ)
     * @return карта категория <-> вероятность попадания набора слов (документа) в эту категорию
     */
    public Map<Category, Double> classify(List<String> words) {
        Map<Category, Double> categoryProbabilityMap = new HashMap<>();
        double s = 0d;// для нормирования на единицу
        for (Category category : CATEGORIES) {
            Map<String, Boolean> keywordsMap = categoryToContainsKeywordsMap.get(category);
            double d = Math.log(this.categoryProbabilityMap.get(category));// log(Dc/D)
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
            s += Math.exp(resultForCategory);
            categoryProbabilityMap.put(category, resultForCategory);
        }
        for (Map.Entry<Category, Double> entry : categoryProbabilityMap.entrySet()) {
            entry.setValue(Math.exp(entry.getValue()) / s);// делаем из логарифма вероятность (нормируем)
        }
        return categoryProbabilityMap;
    }

}
