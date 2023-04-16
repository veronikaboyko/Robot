package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

public class GameVisualizer extends JPanel implements ActionListener {
    private volatile Point robotPosition = new Point(300, 300);
    private volatile double robotDirection = 0;

    private volatile Point targetPosition = new Point(150, 100);

    private static final double maxVelocity = 0.1;
    private static final double maxAngVelocity = 0.01;
    private int screenWight;
    private int screenHeight;

    private double distance;
    private double angleTo;

    public GameVisualizer() {
        Timer timer = new Timer(3, this);
        timer.start();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(this::onModelUpdateEvent, 0, 10, TimeUnit.MILLISECONDS);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setTargetPosition(e.getPoint());
                repaint();
            }
        };
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setTargetPosition(e.getPoint());
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                addMouseMotionListener(mouseAdapter);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                removeMouseMotionListener(mouseAdapter);
            }
        });
        setDoubleBuffered(true);

    }

    protected void setTargetPosition(Point point) {
        targetPosition.setLocation(point);
    }

    protected void onRedrawEvent() {
        repaint();
    }


    private static double angleBetweenPoints(Point p1, Point p2) {
        double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);
        return asNormalizedRadians(angle);
    }

    protected void onModelUpdateEvent() {
        distance = targetPosition.distance(robotPosition);
        double velocity = maxVelocity;
        double angularVelocity;
        angleTo = angleBetweenPoints(targetPosition, robotPosition);
        double angle = asNormalizedRadians(angleTo - robotDirection);
        if (angle > Math.PI) {
            angularVelocity = maxAngVelocity;
        } else {
            angularVelocity = -maxAngVelocity;
        }


        if (Math.abs(angle) >= 0.1)
            velocity = distance * Math.abs(angularVelocity) / 2;


        moveRobot(velocity, angularVelocity, 10);
    }

    private static double applyLimits(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    private synchronized void moveRobot(double velocity, double angularVelocity, double duration) {
        velocity = applyLimits(velocity, 0, maxVelocity);
        angularVelocity = applyLimits(angularVelocity, -maxAngVelocity, maxAngVelocity);
        double newX = robotPosition.getX() + velocity / angularVelocity *
                (Math.sin(robotDirection + angularVelocity * duration) -
                        Math.sin(robotDirection));
        if (!Double.isFinite(newX)) {
            newX = robotPosition.getX() + velocity * duration * Math.cos(robotDirection);
        }
        double newY = robotPosition.getY() - velocity / angularVelocity *
                (Math.cos(robotDirection + angularVelocity * duration) -
                        Math.cos(robotDirection));
        if (!Double.isFinite(newY)) {
            newY = robotPosition.getY() + velocity * duration * Math.sin(robotDirection);
        }

        if (newX < 20) {
            newX = 20;
        } else if (newX > screenWight - 20) {
            newX = screenWight - 20;
        }
        if (newY < 20) {
            newY = 20;
        } else if (newY > screenHeight - 20) {
            newY = screenHeight - 20;
        }

        robotPosition.setLocation(newX, newY);
        robotDirection = asNormalizedRadians(robotDirection + angularVelocity * duration);
    }

    private static double asNormalizedRadians(double angle) {
        angle %= 2 * Math.PI;
        if (angle < 0) {
            angle += 2 * Math.PI;
        }
        return angle;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        screenWight = getWidth();
        screenHeight = getHeight();
        Graphics2D g2d = (Graphics2D) g;
        drawRobot(g2d, (int) robotPosition.getX(), (int) robotPosition.getY(), robotDirection);
        drawTarget(g2d, (int) targetPosition.getX(), (int) targetPosition.getY());
    }

    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2) {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2) {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private void drawRobot(Graphics2D g, int x, int y, double direction) {
        AffineTransform t = AffineTransform.getRotateInstance(direction, x, y);
        g.setTransform(t);
        g.setColor(Color.MAGENTA);
        fillOval(g, x, y, 30, 10);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 30, 10);
        g.setColor(Color.WHITE);
        fillOval(g, x + 10, y, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, x + 10, y, 5, 5);
    }

    private void drawTarget(Graphics2D g, int x, int y) {
        AffineTransform t = AffineTransform.getRotateInstance(0, 0, 0);
        g.setTransform(t);
        g.setColor(Color.GREEN);
        fillOval(g, x, y, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 5, 5);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        onRedrawEvent();
    }
}