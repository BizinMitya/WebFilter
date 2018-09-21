import classificators.bayes.BayesClassifier;
import util.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        BayesClassifier bayesClassifier = new BayesClassifier();
        bayesClassifier.learn(FileUtil.getLearnKeywordsFromFiles());
        List<String> keywords = new ArrayList<>();
        keywords.add("");
        System.out.println(bayesClassifier.classify(keywords));
    }

}
