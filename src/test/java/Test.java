import classificators.bayes.BayesClassifier;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test {

    private static final Logger LOGGER = Logger.getLogger(Test.class);

    public static void main(String[] args) {
        BayesClassifier.learn();
        List<String> keywords = new ArrayList<>();
        keywords.add("оружие");
        keywords.add("как");
        keywords.add("игра");
        keywords.add("я");
        keywords.add("нормально");
        keywords.add("пока");
        Map<String, Double> map = BayesClassifier.classify(keywords);
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            LOGGER.info(entry.getKey() + " -> " + entry.getValue());
        }
    }

}
