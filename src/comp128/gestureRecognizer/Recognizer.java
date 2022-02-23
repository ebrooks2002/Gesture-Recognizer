package comp128.gestureRecognizer;

import edu.macalester.graphics.CanvasWindow;
import edu.macalester.graphics.Ellipse;
import edu.macalester.graphics.GraphicsGroup;
import edu.macalester.graphics.Point;

import java.lang.reflect.Array;
import java.util.*;

import java.util.ArrayDeque;
import java.util.Deque;

import comp128.Templatematch;

/**
 * Recognizer to recognize 2D gestures. Uses the $1 gesture recognition algorithm.
 */
public class Recognizer {

    //TODO: add any necessary instance variables here.
    double size = 200;

    /**
     * Constructs a recognizer object
     */
    public Recognizer(){
    }


    /**
     * Create a template to use for matching
     * @param name of the template
     * @param points in the template gesture's path
     */
    public void addTemplate(String name, Deque<Point> points){
        // TODO: process the points and add them as a template. Use Decomposition!
    }

    
    public Templatematch recognize(Deque<Point> path, ArrayList<Deque<Point>> templates) {
        double minScore = 1000000000;
        Deque<Point> closestTemplate = null;
        for (Deque<Point> template : templates){
            double dist = distanceAtBestAngle(path, template);
            if (dist < minScore) {
                minScore = dist;
                closestTemplate = template;
            }
        }
        double score = 1 - (minScore / 0.5 * Math.sqrt(200*200 + 200*200));
        return new Templatematch(closestTemplate, score);
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
        double phi = 0.5*(-1.0 + Math.sqrt(5.0));// golden ratio
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

    private double distanceAtAngle(Deque<Point> points, Deque<Point> templatePoints, double theta){
        
        Deque<Point> rotatedPoints = null;
        rotatedPoints = rotateBy(points, theta);
        return pathDistance(rotatedPoints, templatePoints);
    }

    public double pathDistance(Deque<Point> a, Deque<Point> b){
        double sum = 0;
        int aSize = a.size();
        for (int i = 0; i < aSize; i++) {
            sum += a.pop().distance(b.pop());
        }
        return sum / aSize;
    }

    public double pathLength(Deque<Point> path) {
        Point pointA = path.peek();
        double lineDist = 0;
        for (Point point : path) {
            lineDist += pointA.distance(point);
            pointA = point;
        }
        return lineDist;
    }

    public Deque<Point> resample(Deque<Point> path, int n) {
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

    public Point centroid(Deque<Point> path) {
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

    public double indicativeAngle(Deque<Point> path) {
        Point centroid  = centroid(path);
        double indicativeAngle = Math.atan2(centroid.getY() - path.peek().getY(), centroid.getX() - path.peek().getX());
        return indicativeAngle;
    }

    public Deque<Point> rotateBy(Deque<Point> path, double angle) {
        Point centroid = centroid(path);
        ArrayDeque<Point> newPath = new ArrayDeque<Point>();
        for (Point point : path) {
            double x = (point.getX() - centroid.getX()) * Math.cos(angle) - (point.getY() - centroid.getY()) * Math.sin(angle) + centroid.getX();
            double y = (point.getX() - centroid.getX()) * Math.sin(angle) + (point.getY() - centroid.getY()) * Math.cos(angle) + centroid.getY();
            newPath.add(new Point(x,y));
        }
        return newPath;
    }

    public Deque<Point> scaleTo(Deque<Point> path, double size) {
        double maxX = 0;
        double maxY = 0;
        double minX = 10000;
        double minY = 10000;
        ArrayDeque<Point> newPath = new ArrayDeque<Point>();
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
        double width = maxX - minX;
        double height = maxY - minY;

        for (Point point: path) {
            double x = point.getX() * size / width;
            double y = point.getY() * size / height;
            newPath.add(new Point(x,y));
        }
        return newPath; 
    }

    public Deque<Point> translateTo(Deque<Point> path, Point k) {
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