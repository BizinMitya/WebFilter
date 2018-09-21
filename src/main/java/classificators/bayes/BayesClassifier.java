package classificators.bayes;

import classificators.Category;

import java.util.*;

import static classificators.Category.*;

public class BayesClassifier {

    //http://synergy-journal.ru/archive/article1737
    //http://bazhenov.me/blog/2012/06/11/naive-bayes

    /**
     * Категория на карту ключевого слова и его частоты.
     * Во второй карте присутствуют ВСЕ ключевые слова из всех категорий.
     */
    private Map<Category, Map<String, Boolean>> categoryToContainsKeywordsMap;
    private Map<Category, Double> categoryFrequencyMap;
    private static final Category[] CATEGORIES = {
            ADVERTISING,
            DRUGS,
            GAMES,
            VIOLENCE,
            WEAPON
    };

    public BayesClassifier() {
        categoryToContainsKeywordsMap = new HashMap<>();
        categoryFrequencyMap = new HashMap<>();
    }

    public void learn(Map<Category, List<String>> categoryToKeywordsMap) {
        Set<String> allKeywords = new HashSet<>();
        for (List<String> keywords : categoryToKeywordsMap.values()) {
            allKeywords.addAll(keywords);
        }
        for (Category category : CATEGORIES) {
            List<String> keywordsForCategory = categoryToKeywordsMap.get(category);
            categoryFrequencyMap.put(category, (double) keywordsForCategory.size() / allKeywords.size());
            Map<String, Boolean> keywordsContainsMap = new HashMap<>();
            for (String keyword : allKeywords) {
                keywordsContainsMap.put(keyword, keywordsForCategory.contains(keyword));
            }
            categoryToContainsKeywordsMap.put(category, keywordsContainsMap);
        }
    }

    public Category classify(List<String> keywords) {
        Map<Category, Double> categoryDoubleMap = new HashMap<>();
        for (Category category : CATEGORIES) {
            double d = Math.log(categoryFrequencyMap.get(category));// log(Dc/D)
            double sum = 0d;
            for (String keyword : keywords) {
                //todo:
            }
            double resultForCategory = d + sum;
            categoryDoubleMap.put(category, resultForCategory);
        }
        return categoryDoubleMap.entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .get();
    }

    private void print() {
        for (Map.Entry<Category, Map<String, Boolean>> entry : categoryToContainsKeywordsMap.entrySet()) {
            for (Map.Entry<String, Boolean> e : entry.getValue().entrySet()) {
                System.out.println(entry.getKey() + "(" + e.getKey() + ") = " + e.getValue());
            }
        }
    }

}
