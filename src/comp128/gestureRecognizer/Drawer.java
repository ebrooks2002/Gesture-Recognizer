package comp128.gestureRecognizer;

import java.util.GregorianCalendar;

import edu.macalester.graphics.Ellipse;
import edu.macalester.graphics.GraphicsGroup;
import edu.macalester.graphics.Line;
import edu.macalester.graphics.Point;

public class Drawer {
    
    public void apply(Point currentPoint, Point prevPoint, GraphicsGroup drawingSurface) {
        Line line = new Line(prevPoint.getX(), prevPoint.getY(), currentPoint.getX(), currentPoint.getY());
        drawingSurface.add(line);
        GestureApp.prevPoint = currentPoint;
    }
}