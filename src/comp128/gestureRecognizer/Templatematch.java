package comp128.gestureRecognizer;

import java.util.Deque;

import edu.macalester.graphics.Point;

/**
 * this class is meant to hold information for the template that matches a gesture the closest.
 */
public class Templatematch {
    private String name;
    private Deque<Point> closestTemplate;
    private Double score;

    public Templatematch(Deque<Point> closestTemplate, Double score, String name) {
        this.closestTemplate = closestTemplate;
        this.score = score;
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public double getScore() {
        return score;
    }
}
