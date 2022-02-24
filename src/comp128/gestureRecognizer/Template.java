package comp128.gestureRecognizer;

import java.util.Deque;

import edu.macalester.graphics.Point;

/**
 * this class is meant to hold the information for a template added.
 */
public class Template {
    private String name;
    private Deque<Point> path;

    public Template(String name, Deque<Point> path) {
        this.name = name;
        this.path = path;
    }
    public Deque<Point> getPath() {
        return path;
    }
    public String getName() {
        return name;
    }
}
