/* Copyright (c) 2026 appleweiping. MIT License.
 *
 * This is NOT part of the graded 6.005 skeleton. It is an auxiliary,
 * headless implementation of the Turtle interface that renders straight to a
 * PNG file instead of opening a Swing window, so that drawPersonalArt() can be
 * captured as evidence on a machine with no display. It does not modify any
 * course-provided file or interface.
 */
package turtle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * A {@link Turtle} that accumulates line segments and writes them to a PNG.
 */
public class PngTurtle implements Turtle {

    private static final Map<PenColor, Color> PEN_COLORS = new EnumMap<>(PenColor.class);
    static {
        PEN_COLORS.put(PenColor.BLACK, Color.BLACK);
        PEN_COLORS.put(PenColor.GRAY, Color.GRAY);
        PEN_COLORS.put(PenColor.RED, Color.RED);
        PEN_COLORS.put(PenColor.PINK, Color.PINK);
        PEN_COLORS.put(PenColor.ORANGE, Color.ORANGE);
        PEN_COLORS.put(PenColor.YELLOW, new Color(228, 228, 0));
        PEN_COLORS.put(PenColor.GREEN, Color.GREEN);
        PEN_COLORS.put(PenColor.CYAN, Color.CYAN);
        PEN_COLORS.put(PenColor.BLUE, Color.BLUE);
        PEN_COLORS.put(PenColor.MAGENTA, Color.MAGENTA);
    }

    private final List<LineSegment> lines = new ArrayList<>();
    private double x = 0, y = 0;
    private double heading = 0.0;         // clockwise from north (Logo semantics)
    private PenColor color = PenColor.BLACK;

    @Override
    public void forward(int units) {
        // Same geometry the course's DrawableTurtle uses.
        double newX = x + Math.cos(Math.toRadians(90 - heading)) * units;
        double newY = y + Math.sin(Math.toRadians(90 - heading)) * units;
        lines.add(new LineSegment(x, y, newX, newY, color));
        x = newX;
        y = newY;
    }

    @Override
    public void turn(double degrees) {
        degrees = ((degrees % 360) + 360) % 360;
        heading = (heading + degrees) % 360;
    }

    @Override
    public void color(PenColor c) {
        this.color = c;
    }

    @Override
    public void draw() {
        // no-op: use writePng() to persist the image
    }

    /**
     * Render the accumulated line segments to a PNG file, auto-fitting the
     * drawing to the canvas with a margin.
     *
     * @param path output PNG path
     * @param size square canvas size in pixels
     * @throws IOException if writing fails
     */
    public void writePng(String path, int size) throws IOException {
        double minX = 0, minY = 0, maxX = 0, maxY = 0;
        for (LineSegment s : lines) {
            minX = Math.min(minX, Math.min(s.start().x(), s.end().x()));
            minY = Math.min(minY, Math.min(s.start().y(), s.end().y()));
            maxX = Math.max(maxX, Math.max(s.start().x(), s.end().x()));
            maxY = Math.max(maxY, Math.max(s.start().y(), s.end().y()));
        }
        double drawW = Math.max(maxX - minX, 1);
        double drawH = Math.max(maxY - minY, 1);
        int margin = 24;
        double scale = Math.min((size - 2.0 * margin) / drawW, (size - 2.0 * margin) / drawH);

        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(1.5f));

        for (LineSegment s : lines) {
            // world -> screen: flip y (screen y grows downward), center the drawing
            int x1 = (int) Math.round(margin + (s.start().x() - minX) * scale);
            int y1 = (int) Math.round(size - margin - (s.start().y() - minY) * scale);
            int x2 = (int) Math.round(margin + (s.end().x() - minX) * scale);
            int y2 = (int) Math.round(size - margin - (s.end().y() - minY) * scale);
            g.setColor(PEN_COLORS.getOrDefault(s.color(), Color.BLACK));
            g.drawLine(x1, y1, x2, y2);
        }
        g.dispose();
        File out = new File(path);
        if (out.getParentFile() != null) {
            out.getParentFile().mkdirs();
        }
        ImageIO.write(img, "png", out);
    }

    /**
     * Draw {@link TurtleSoup#drawPersonalArt} headlessly and save it to PNG.
     *
     * @param args optional single argument: output PNG path
     * @throws IOException if writing fails
     */
    public static void main(String[] args) throws IOException {
        String path = args.length > 0 ? args[0] : "results/personal-art.png";
        PngTurtle t = new PngTurtle();
        TurtleSoup.drawPersonalArt(t);
        t.writePng(path, 800);
        System.out.println("Wrote " + path + " (" + t.lines.size() + " line segments)");
    }
}
