// Ethan Brooks 2/21/2022
package comp128.gestureRecognizer;

import edu.macalester.graphics.Point;

import java.util.*;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Recognizer to recognize 2D gestures. Uses the $1 gesture recognition algorithm.
 */
public class Recognizer {

    private double size = 250;
    private double width;
    private double height;
    private ArrayList<Template> templates = new ArrayList<Template>();

    /**
     * Constructs a recognizer object
     */
    public Recognizer() {
    }

    public int getTemplatesSize() {
        return templates.size();
    }
    /**
     * Create a template to use for matching
     * @param name of the template
     * @param points in the template gesture's path
     */
    public void addTemplate(String name, Deque<Point> points){
        Template newTemplate = new Template(name, doSteps(points));
        templates.add(newTemplate);
    }
    /**
     * Takes a gesture and resamples, rotates, scales, and tranlates it so it can be compared to templates.
     * @param points
     * @return
     */
    public Deque<Point> doSteps(Deque<Point> points) {
        Deque<Point> resampled = resample(points, 64);
        Deque<Point> rotated = rotateBy(resampled, -indicativeAngle(resampled));
        Deque<Point> scaled = scaleTo(rotated, size);
        Deque<Point> translated = translateTo(scaled, new Point(0,0));
        return translated;
    }
    
    /**
     * Given a processed gesture, this method compares it to templates and returns the closest match.
     * @param path
     * @return
     */
    public Templatematch recognize(Deque<Point> path) {
        double minScore = Double.MAX_VALUE;
        path = doSteps(path);
        Template closestTemplate = templates.get(0);
        for (int i = 0; i < templates.size(); i++){
            double dist = distanceAtBestAngle(path, templates.get(i).getPath());
            if (dist < minScore) {
                minScore = dist;
                closestTemplate = templates.get(i);
            }
        }
        double score = 1 - (minScore / (0.5 * Math.sqrt(size*size + size*size)));
        return new Templatematch(closestTemplate.getPath(), score, closestTemplate.getName());
    }

    /**
     * Uses a golden section search to calculate rotation that minimizes the distance between the gesture and the template points.
     * @param points
     * @param templatePoints
     * @return best distance
     */
    private double distanceAtBestAngle(Deque<Point> points, Deque<Point> templatePoints){
        double thetaA = -Math.toRadians(45);
        double thetaB = Math.toRadians(45);
        final double deltaTheta = Math.toRadians(2);
        double phi = 0.5*(-1.0 + Math.sqrt(5.0)); //golden ratio
        double x1 = phi*thetaA + (1-phi)*thetaB;
        double f1 = distanceAtAngle(points, templatePoints, x1);
        double x2 = (1 - phi)*thetaA + phi*thetaB;
        double f2 = distanceAtAngle(points, templatePoints, x2);
        while(Math.abs(thetaB-thetaA) > deltaTheta){
            if (f1 < f2){
                thetaB = x2;
                x2 = x1;
                f2 = f1;
                x1 = phi*thetaA + (1-phi)*thetaB;
                f1 = distanceAtAngle(points, templatePoints, x1);
            }
            else{
                thetaA = x1;
                x1 = x2;
                f1 = f2;
                x2 = (1-phi)*thetaA + phi*thetaB;
                f2 = distanceAtAngle(points, templatePoints, x2);
            }
        }
        return Math.min(f1, f2);
    }

    private double distanceAtAngle(Deque<Point> points, Deque<Point> templatePoints, double theta) {
        Deque<Point> rotatedPoints = null;
        rotatedPoints = rotateBy(points, theta);
        return pathDistance(rotatedPoints, templatePoints);
    }

    /**
     * compares each indviudal gesture point to each individual template point, and returns the avg distance btwn points.
     * @param a
     * @param b
     * @return
     */
    private double pathDistance(Deque<Point> a, Deque<Point> b){
        double sum = 0;
        Iterator<Point> itra = a.iterator();
        Iterator<Point> itrb = b.iterator();
        while (itra.hasNext()) {
            sum += itra.next().distance(itrb.next());
        }
        return sum / a.size();
    }
    /**
     * Finds the total length of the path by summing the distances between consecutive points.
     * @param path
     * @return
     */
    private double pathLength(Deque<Point> path) {
        Point pointA = path.peek();
        double lineDist = 0;
        for (Point point : path) {
            lineDist += pointA.distance(point);
            pointA = point;
        }
        return lineDist;
    }

    /**
     * iterates through path and creates a new path of the same length but with n points.
     * @param path
     * @param n
     * @return
     */
    private Deque<Point> resample(Deque<Point> path, int n) {
        double pathLength = pathLength(path);
        double accumDistance = 0;
        double stepDistance = pathLength / (n-1);
        ArrayDeque<Point> resampledPoints = new ArrayDeque<>();
        resampledPoints.addLast(path.peek());
        Iterator<Point> itr = path.iterator();
        Point prevPoint = itr.next();
        Point currentPoint = itr.next();
        while (itr.hasNext()) {
            double currentDist = currentPoint.distance(prevPoint);
            if (currentDist + accumDistance >= stepDistance) {
                Point newPoint = Point.interpolate(prevPoint, currentPoint, (stepDistance - accumDistance)/currentDist);
                resampledPoints.addLast(newPoint);
                prevPoint = newPoint;
                accumDistance = 0;
            }
            else {
            accumDistance += currentDist;
            prevPoint = currentPoint;
            currentPoint = itr.next();
            }
        }
        if (resampledPoints.size() == n-1) {
            resampledPoints.addLast(path.getLast());
        }
        return resampledPoints;
    }

    /**
     * returns the centroid of a given path.
     * @param path
     * @return
     */
    private Point centroid(Deque<Point> path) {
        double x = 0;
        double y = 0;
        Iterator itr = path.iterator();
        while (itr.hasNext()) {
            Point p = (Point) itr.next();
            x += p.getX();
            y += p.getY();
        }
        x = x / ((double)path.size());
        y = y / ((double)path.size());
        Point centroid = new Point(x,y);
        return centroid;
    }

     /**
      * returns the indicativeAngle of the path.
      * @param path
      * @return
      */
    private double indicativeAngle(Deque<Point> path) {
        Point centroid  = centroid(path);
        double indicativeAngle = Math.atan2(centroid.getY() - path.peek().getY(), centroid.getX() - path.peek().getX());
        return indicativeAngle;
    }

    /**
     * rotates the given path using the indicative angle.
     * @param path
     * @param angle
     * @return
     */
    private Deque<Point> rotateBy(Deque<Point> path, double angle) {
        Point centroid = centroid(path);
        ArrayDeque<Point> newPath = new ArrayDeque<Point>();
        for (Point point : path) {
            double x = (point.getX() - centroid.getX()) * Math.cos(angle) - (point.getY() - centroid.getY()) * Math.sin(angle) + centroid.getX();
            double y = (point.getX() - centroid.getX()) * Math.sin(angle) + (point.getY() - centroid.getY()) * Math.cos(angle) + centroid.getY();
            newPath.add(new Point(x,y));
        }
        return newPath;
    }

    /**
     * finds the height and width of a path. (just the range of x and y values.)
     * @param path
     * @param size
     */
    private void boxSize(Deque<Point> path) {
        double maxX = 0;
        double maxY = 0;
        double minX = 10000;
        double minY = 10000;
        for (Point point : path) {
            if (point.getX() > maxX) {
                maxX = point.getX();
            }
            if (point.getY() > maxY) {
                maxY = point.getY();
            }
            if (point.getX() < minX) {
                minX = point.getX();
            }
            if (point.getY() < minY){
                minY = point.getY();
            }
        }
        width = maxX - minX;
        height = maxY - minY;
    }

    /**
     * scales the given path to a given size
     * @param path
     * @param size
     * @return
     */
    private Deque<Point> scaleTo(Deque<Point> path, double size) {
        ArrayDeque<Point> newPath = new ArrayDeque<Point>();
        boxSize(path);
        
        for (Point point: path) {
            double x = point.getX() * size / width;
            double y = point.getY() * size / height;
            newPath.add(new Point(x,y));
        }
        return newPath; 
    }

    /**
     * translates the path to the point k.
     * @param path
     * @param k
     * @return
     */
    private Deque<Point> translateTo(Deque<Point> path, Point k) {
        ArrayDeque<Point> newPath = new ArrayDeque<Point>();
        Point centroid = centroid(path);
        for (Point point : path) {
            double x = point.getX() + k.getX() - centroid.getX();
            double y = point.getY() + k.getY() - centroid.getY();
            newPath.add(new Point(x,y));
        }
        return newPath;
    }
}