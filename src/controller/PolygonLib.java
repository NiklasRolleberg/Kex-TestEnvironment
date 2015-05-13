package controller;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**Polygon computation stuff library */
public class PolygonLib {
    public static double findMax(ArrayList<Double> inList){
        Double temp = Double.MIN_VALUE;
        for (Double d : inList){
            if(d > temp){
                temp = d;
            }
        }
        return temp;

    }
    public static double findMin(ArrayList<Double> inList){
        Double temp = Double.MAX_VALUE;
        for (Double d : inList){
            if(d < temp){
                temp = d;
            }
        }
        return temp;

    }







//private static final Integer ZERO = new Integer(0);


    /**
     * from http://www.gregbugaj.com/?p=499
     * Find Convex hull of given points
     *
     *
     * @return convex hull based on x and y coords
     */
    public static ArrayList<ArrayList<Double>> findConvexHull(ArrayList<Double> xIn, ArrayList<Double> yIn){//(final ArrayList<SearchElement> vertices){
        System.out.println("Convex hull! Sizes x, y: " + xIn.size() + ", " + yIn.size());
        ArrayList<ArrayList<Double>> cHull = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> xPts, yPts, xHull, yHull;
        xHull = new ArrayList<Double>();
        yHull = new ArrayList<Double>();
        xPts = new ArrayList<Double>();
        yPts = new ArrayList<Double>();
        ArrayList<Point2D> pts = new ArrayList<Point2D>();
        ArrayList<Point2D> hullPts = new ArrayList<Point2D>();
        //initialize
        for (int i = 0; i<xIn.size();i++){
            xPts.add(xIn.get(i));
            yPts.add(yIn.get(i));
            pts.add(new Point2D.Double(xIn.get(i),yIn.get(i)));
        }
        if (pts.size() == 0) {
            System.out.println("empty parameter sent to findConvexHull");
            return new ArrayList<ArrayList<Double>>();
        }

        if (pts.size() < 3){
            System.out.println(" Size less than 3");
            cHull.add(xPts);
            cHull.add(yPts);
            return  cHull;
        }



//        final ArrayList<Point> hull = new ArrayList<Point>();
        Point2D pointOnHull = getMinXPoint(pts);
        Point2D endpoint = null;
        do{
            xHull.add(pointOnHull.getX());
            yHull.add(pointOnHull.getY());

            hullPts.add(pointOnHull);
            endpoint = pts.get(0);

            for (final Point2D pt : pts){
                final int turn = findTurn(pointOnHull, endpoint, pt);
                if (endpoint.equals(pointOnHull) || turn == -1 || turn == 0 && pointOnHull.distance(pt) > endpoint.distance(pointOnHull)){
                    endpoint = pt;
                }
            }
            pointOnHull = endpoint;
        } while (!endpoint.equals(hullPts.get(0))); // we are back at the start


        cHull.add(xHull);
        cHull.add(yHull);
        System.out.println("Convex hull found! Sizes x, y: " + cHull.get(0).size() + ", " + cHull.get(1).size());
        return cHull;
    }

    /**Returns the leftmost point*/
    private static Point2D getMinXPoint(ArrayList<Point2D> points) {
        //Point2D tempP = new Point2D.Double(Double.MAX_VALUE, Double.MAX_VALUE);
        Double temp = Double.MAX_VALUE;
        int i = 0;
        int tempIndex = 0;
        for (Point2D p : points){
            if (p.getX() < temp){
                temp = p.getX();
                tempIndex = i;
            }
            i++;
        }
        return points.get(tempIndex);
    }


    private static double dist(final Point p, final Point q) {
        final double dx = (q.x - p.x);
        final double dy = (q.y - p.y);
        return dx * dx + dy * dy;
    }


    /**
     * Returns -1, 0, 1 if p, q, r forms a right, straight, or left turn.
     * 1 = left, -1 = right, 0 = none
     *
     * @ref http://www-ma2.upc.es/geoc/mat1q1112/OrientationTests.pdf
     * @param p
     * @param q
     * @param r
     * @return 1 = left, -1 = right, 0 = none
     */
    private static int findTurn(final Point2D p, final Point2D q, final Point2D r) {
        Double zero = 0.0;
        final double x1 = (q.getX() - p.getX()) * (r.getY() - p.getY());
        final double x2 = (r.getX() - p.getX()) * (q.getY() - p.getY());
        final double anotherDouble = x1 - x2;
        return zero.compareTo(anotherDouble);
    }
}