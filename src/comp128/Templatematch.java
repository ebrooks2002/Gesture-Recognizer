package comp128;

import java.util.Deque;

import edu.macalester.graphics.Point;

public class Templatematch {
    public Deque<Point> closestTemplate;
    public Double score;

    public Templatematch(Deque<Point> closestTemplate, Double score) {
        this.closestTemplate = closestTemplate;
        this.score = score;
    }

}
