import classificators.Category;
import classificators.bayes.BayesClassifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test {

    public static void main(String[] args) {
        BayesClassifier.learn();
        List<String> keywords = new ArrayList<>();
        keywords.add("оружие");
        keywords.add("как");
        keywords.add("игра");
        keywords.add("я");
        keywords.add("нормально");
        keywords.add("пока");
        Map<Category, Double> map = BayesClassifier.classify(keywords);
        for (Map.Entry<Category, Double> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }

    }

}
