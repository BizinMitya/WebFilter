package model;

import java.io.Serializable;

public class Probability implements Serializable {

    private String category;
    private Double probability;

    public Probability(String category, Double probability) {
        this.category = category;
        this.probability = probability;
    }

    public Probability() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }
}
