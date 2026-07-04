/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package turtle;

import java.util.List;
import java.util.ArrayList;

public class TurtleSoup {

    /**
     * Draw a square.
     * 
     * @param turtle the turtle context
     * @param sideLength length of each side
     */
    public static void drawSquare(Turtle turtle, int sideLength) {
        for (int i = 0; i < 4; i++) {
            turtle.forward(sideLength);
            turtle.turn(90.0);
        }
    }

    /**
     * Determine inside angles of a regular polygon.
     * 
     * There is a simple formula for calculating the inside angles of a polygon;
     * you should derive it and use it here.
     * 
     * @param sides number of sides, where sides must be > 2
     * @return angle in degrees, where 0 <= angle < 360
     */
    public static double calculateRegularPolygonAngle(int sides) {
        // Sum of interior angles of an n-gon is (n-2)*180; each of the n equal
        // interior angles is therefore (n-2)*180 / n.
        return (sides - 2) * 180.0 / sides;
    }

    /**
     * Determine number of sides given the size of interior angles of a regular polygon.
     * 
     * There is a simple formula for this; you should derive it and use it here.
     * Make sure you *properly round* the answer before you return it (see java.lang.Math).
     * HINT: it is easier if you think about the exterior angles.
     * 
     * @param angle size of interior angles in degrees, where 0 < angle < 180
     * @return the integer number of sides
     */
    public static int calculatePolygonSidesFromAngle(double angle) {
        // The exterior angle of a regular polygon is (180 - interior). The n
        // exterior angles sum to 360, so n = 360 / (180 - interior). Round to
        // the nearest integer to absorb floating-point input imprecision.
        return (int) Math.round(360.0 / (180.0 - angle));
    }

    /**
     * Given the number of sides, draw a regular polygon.
     * 
     * (0,0) is the lower-left corner of the polygon; use only right-hand turns to draw.
     * 
     * @param turtle the turtle context
     * @param sides number of sides of the polygon to draw
     * @param sideLength length of each side
     */
    public static void drawRegularPolygon(Turtle turtle, int sides, int sideLength) {
        // The turtle turns by the exterior angle (180 - interior) at each
        // vertex; after n sides it has turned a full 360 degrees.
        double exteriorAngle = 180.0 - calculateRegularPolygonAngle(sides);
        for (int i = 0; i < sides; i++) {
            turtle.forward(sideLength);
            turtle.turn(exteriorAngle);
        }
    }

    /**
     * Given the current direction, current location, and a target location, calculate the heading
     * towards the target point.
     * 
     * The return value is the angle input to turn() that would point the turtle in the direction of
     * the target point (targetX,targetY), given that the turtle is already at the point
     * (currentX,currentY) and is facing at angle currentHeading. The angle must be expressed in
     * degrees, where 0 <= angle < 360. 
     *
     * HINT: look at http://en.wikipedia.org/wiki/Atan2 and Java's math libraries
     * 
     * @param currentHeading current direction as clockwise from north
     * @param currentX current location x-coordinate
     * @param currentY current location y-coordinate
     * @param targetX target point x-coordinate
     * @param targetY target point y-coordinate
     * @return adjustment to heading (right turn amount) to get to target point,
     *         must be 0 <= angle < 360
     */
    public static double calculateHeadingToPoint(double currentHeading, int currentX, int currentY,
                                                 int targetX, int targetY) {
        int dx = targetX - currentX;
        int dy = targetY - currentY;
        // Headings are measured clockwise from north (+y). atan2(dx, dy) gives
        // exactly that convention: 0 = north, 90 = east, ... in radians.
        double absoluteHeading = Math.toDegrees(Math.atan2(dx, dy));
        double turn = absoluteHeading - currentHeading;
        // Normalize the right-turn amount to [0, 360).
        return ((turn % 360.0) + 360.0) % 360.0;
    }

    /**
     * Given a sequence of points, calculate the heading adjustments needed to get from each point
     * to the next.
     * 
     * Assumes that the turtle starts at the first point given, facing up (i.e. 0 degrees).
     * For each subsequent point, assumes that the turtle is still facing in the direction it was
     * facing when it moved to the previous point.
     * You should use calculateHeadingToPoint() to implement this function.
     * 
     * @param xCoords list of x-coordinates (must be same length as yCoords)
     * @param yCoords list of y-coordinates (must be same length as xCoords)
     * @return list of heading adjustments between points, of size 0 if (# of points) == 0,
     *         otherwise of size (# of points) - 1
     */
    public static List<Double> calculateHeadings(List<Integer> xCoords, List<Integer> yCoords) {
        List<Double> headings = new ArrayList<>();
        // The turtle starts facing north (0 degrees). For each hop it turns by
        // the heading adjustment to the next point, then keeps that new heading.
        double currentHeading = 0.0;
        for (int i = 0; i + 1 < xCoords.size(); i++) {
            double adjustment = calculateHeadingToPoint(currentHeading,
                    xCoords.get(i), yCoords.get(i),
                    xCoords.get(i + 1), yCoords.get(i + 1));
            headings.add(adjustment);
            currentHeading = (currentHeading + adjustment) % 360.0;
        }
        return headings;
    }

    /**
     * Draw your personal, custom art.
     * 
     * Many interesting images can be drawn using the simple implementation of a turtle.  For this
     * function, draw something interesting; the complexity can be as little or as much as you want.
     * 
     * @param turtle the turtle context
     */
    public static void drawPersonalArt(Turtle turtle) {
        // A "spirograph" rosette: draw many overlapping polygons, rotating a
        // little and cycling the pen color between each one. Uses only
        // forward(), turn(), and color() as the spec allows.
        PenColor[] palette = {
                PenColor.RED, PenColor.ORANGE, PenColor.YELLOW, PenColor.GREEN,
                PenColor.CYAN, PenColor.BLUE, PenColor.MAGENTA, PenColor.PINK
        };
        final int polygons = 36;      // number of polygons around the ring
        final int sides = 6;          // each polygon is a hexagon
        final int sideLength = 80;
        for (int i = 0; i < polygons; i++) {
            turtle.color(palette[i % palette.length]);
            drawRegularPolygon(turtle, sides, sideLength);
            // rotate before drawing the next polygon so they fan out
            turtle.turn(360.0 / polygons);
        }
    }

    /**
     * Main method.
     * 
     * This is the method that runs when you run "java TurtleSoup".
     * 
     * @param args unused
     */
    public static void main(String args[]) {
        DrawableTurtle turtle = new DrawableTurtle();

        drawSquare(turtle, 40);

        // draw the window
        turtle.draw();
    }

}
