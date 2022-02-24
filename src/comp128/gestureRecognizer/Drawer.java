//Ethan Brooks
package comp128.gestureRecognizer;

import java.util.Deque;

import edu.macalester.graphics.GraphicsGroup;
import edu.macalester.graphics.Line;
import edu.macalester.graphics.Point;
/**
 * handles the user input for drawing gestures.
 */
public class Drawer {

    /**
     * adds lines to the drawing surface, and points to the path.
     * @param currentPoint
     * @param prevPoint
     * @param drawingSurface
     * @param path
     */
    public void apply(Point currentPoint, Point prevPoint, GraphicsGroup drawingSurface, Deque<Point> path) {
        if (path.size() != 0) {
            prevPoint = path.getLast();
        }
        path.addLast(currentPoint);
        Line line = new Line(prevPoint.getX(), prevPoint.getY(), currentPoint.getX(), currentPoint.getY());
        drawingSurface.add(line);
        GestureApp.setPrevPoint(currentPoint);
    }
}
