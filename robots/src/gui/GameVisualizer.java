package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import javax.swing.*;
import javax.swing.Timer;

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
    private ArrayList<Point> towers;

    Map<String, Boolean> gameState = new HashMap<>();
    private volatile Point gameTargetPosition;
    private ArrayList<Point> bullets;

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

        bullets = new ArrayList<>();
        gameTargetPosition = generateRandomPositions(1).get(0);
        towers = generateRandomPositions(10);

        shootBullets();
    }

    private ArrayList<Point> generateRandomPositions(int count) {
        Random random = new Random();
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int x = random.nextInt(700 - 40) + 20;
            int y = random.nextInt(700 - 40) + 20;
            points.add(new Point(x, y));
        }
        return points;
    }

    private void drawGameTarget(Graphics2D g, int x, int y) {
        g.setColor(Color.MAGENTA);
        fillOval(g, x, y, 30, 30);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 30, 30);
    }

    private void drawLets(Graphics2D g) {
        for (Point point : towers) {
            g.setColor(Color.GRAY);
            fillOval(g, point.x, point.y, 30, 30);
            g.setColor(Color.BLACK);
            drawOval(g, point.x, point.y, 30, 30);
        }
    }

    private void checkGameCondition() {
        if (isCollision(robotPosition, gameTargetPosition, 10)) {
            gameState.put("win", true);
        }
        for (Point bullet : bullets) {
            if (isCollision(robotPosition, bullet, 12)) {
                gameState.put("lose", true);
                break;
            }
        }
    }

    public Map<String, Boolean> getGameState() {
        return gameState;
    }

    public void cleanGameState(){
        gameState = new HashMap<>();
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
        if (gameState.isEmpty()){
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

            if (newX < 2) {
                newX = 2;
            } else if (newX > screenWight - 2) {
                newX = screenWight - 2;
            }
            if (newY < 20) {
                newY = 20;
            } else if (newY > screenHeight - 2) {
                newY = screenHeight - 2;
            }

            robotPosition.setLocation(newX, newY);
            robotDirection = asNormalizedRadians(robotDirection + angularVelocity * duration);
        }

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
        screenWight = getWidth() * 2;
        screenHeight = getHeight() * 2;
        Graphics2D g2d = (Graphics2D) g;
        drawRobot(g2d, (int) robotPosition.getX(), (int) robotPosition.getY(), robotDirection);
        drawTarget(g2d, (int) targetPosition.getX(), (int) targetPosition.getY());
        drawGameTarget(g2d, gameTargetPosition.x, gameTargetPosition.y);
        drawLets(g2d);

        ArrayList<Point> bulletsCopy = new ArrayList<>(bullets);
        g.setColor(Color.GRAY);
        for (Point bullet : bulletsCopy) {
            fillOval(g, bullet.x, bullet.y, 12, 12);
        }
        checkGameCondition();
    }

    private boolean isCollision(Point p1, Point p2, int diameter) {
        double distance = p1.distance(p2);
        return distance <= diameter;
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

    private void shootBullets() {
        ScheduledExecutorService bulletScheduler = Executors.newScheduledThreadPool(towers.size());

        for (Point let : towers) {
            bulletScheduler.scheduleAtFixedRate(() -> {
                double bulletDirection = Math.random() * 2 * Math.PI;

                Point bullet = new Point(let.x, let.y);
                bullets.add(bullet);

                ScheduledExecutorService bulletMoveScheduler = Executors.newScheduledThreadPool(1);
                bulletMoveScheduler.scheduleAtFixedRate(() -> {

                    int bulletSpeed = 7;

                    int bulletX = bullet.x;
                    int bulletY = bullet.y;
                    bulletX += (int) (bulletSpeed * Math.cos(bulletDirection));
                    bulletY += (int) (bulletSpeed * Math.sin(bulletDirection));

                    bullet.setLocation(bulletX, bulletY);

                    if (bulletX < 0 || bulletX > 1000 || bulletY < 0 || bulletY > 1000) {
                        bullets.remove(bullet);
                        bulletMoveScheduler.shutdown();
                    }

                    repaint();
                }, 0, 20, TimeUnit.MILLISECONDS);
            }, 0, 3, TimeUnit.SECONDS);
        }
    }
}