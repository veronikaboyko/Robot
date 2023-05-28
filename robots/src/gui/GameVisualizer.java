package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.Profile;

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
    private int screenHeight;
    private int screenWight;
    private int timeOutAttack;
    private int timeOutBullet;
    private int bulletSpeed;
    private int startTimeBullet;
    private int startTimeAttack;
    private int threadPoolBullet;
    private int threadPoolGame;
    private int starTimeGame;
    private int timeOutResetGame;
    private int diamBullet;
    private int minCordBullet;
    private int maxCordBullet;
    private int diamDrawTarget;
    private int diamTarget;
    private int diamWhiteConst;
    private int diamBlackConst;
    private double distance;
    private double angleTo;
    private ArrayList<Point> towers;
    private volatile Point gameTargetPosition;
    private ArrayList<Point> bullets;
    private EndGameHandling endGameHandling = new EndGameHandling();

    public GameVisualizer() {
        try {
            Profile.Section modelSection = new Ini(new File("config.ini")).get("model");
            timeOutBullet = modelSection.get("timeOutBullet", Integer.class);
            timeOutAttack = modelSection.get("timeOutAttack", Integer.class);
            bulletSpeed = modelSection.get("bulletSpeed", Integer.class);
            startTimeBullet = modelSection.get("startTimeBullet", Integer.class);
            startTimeAttack = modelSection.get("startTimeAttack", Integer.class);
            threadPoolBullet = modelSection.get("threadPoolBullet", Integer.class);
            threadPoolGame = modelSection.get("threadPoolGame", Integer.class);
            starTimeGame = modelSection.get("starTimeGame", Integer.class);
            timeOutResetGame = modelSection.get("timeOutResetGame", Integer.class);
            diamBullet = modelSection.get("diamBullet", Integer.class);
            minCordBullet = modelSection.get("minCordBullet", Integer.class);
            maxCordBullet = modelSection.get("maxCordBullet", Integer.class);
            diamDrawTarget = modelSection.get("diamDrawTarget", Integer.class);
            diamTarget = modelSection.get("diamTarget", Integer.class);
            diamWhiteConst = modelSection.get("diamWhiteConst", Integer.class);
            diamBlackConst = modelSection.get("diamBlackConst", Integer.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Timer timer = new Timer(3, this);
        timer.start();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(threadPoolGame);
        scheduler.scheduleAtFixedRate(this::onModelUpdateEvent, starTimeGame, timeOutResetGame, TimeUnit.MILLISECONDS);

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


    public EndGameHandling getEndGame() {
        return endGameHandling;
    }

    private ArrayList<Point> generateRandomPositions(int count) {
        Random random = new Random();
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int x = random.nextInt(660) + 20;
            int y = random.nextInt(660) + 20;
            points.add(new Point(x, y));
        }
        return points;
    }

    private void drawGameTarget(Graphics2D g, int x, int y) {
        g.setColor(Color.MAGENTA);
        fillOval(g, x, y, diamDrawTarget, diamDrawTarget);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, diamDrawTarget, diamDrawTarget);
    }

    private void drawLets(Graphics2D g) {
        for (Point point : towers) {
            g.setColor(Color.GRAY);
            fillOval(g, point.x, point.y, diamDrawTarget, diamDrawTarget);
            g.setColor(Color.BLACK);
            drawOval(g, point.x, point.y, diamDrawTarget, diamDrawTarget);
        }
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
        if (endGameHandling.getGameState().isEmpty()) {
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

            if (newX < 2)
                newX = 2;
            else if (newX > screenWight - 2)
                newX = screenWight - 2;
            if (newY < 2)
                newY = 2;
            else if (newY > screenHeight - 2)
                newY = screenHeight - 2;

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
        screenHeight = getHeight() * 2;
        screenWight = getWidth() * 2;
        Graphics2D g2d = (Graphics2D) g;
        drawRobot(g2d, (int) robotPosition.getX(), (int) robotPosition.getY(), robotDirection);
        drawTarget(g2d, (int) targetPosition.getX(), (int) targetPosition.getY());
        drawGameTarget(g2d, gameTargetPosition.x, gameTargetPosition.y);
        drawLets(g2d);

        ArrayList<Point> bulletsCopy = new ArrayList<>(bullets);
        g.setColor(Color.GRAY);
        for (Point bullet : bulletsCopy) {
            fillOval(g, bullet.x, bullet.y, diamBullet, diamBullet);
        }
        endGameHandling.checkGameState(robotPosition, gameTargetPosition, bullets);
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
        fillOval(g, x, y, diamDrawTarget, diamBlackConst);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, diamDrawTarget, diamWhiteConst);
        g.setColor(Color.WHITE);
        fillOval(g, x + diamWhiteConst, y, diamTarget, diamTarget);
        g.setColor(Color.BLACK);
        drawOval(g, x + diamBlackConst, y, diamTarget, diamTarget);
    }

    private void drawTarget(Graphics2D g, int x, int y) {
        AffineTransform t = AffineTransform.getRotateInstance(0, 0, 0);
        g.setTransform(t);
        g.setColor(Color.GREEN);
        fillOval(g, x, y, diamTarget, diamTarget);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, diamTarget, diamTarget);
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

                ScheduledExecutorService bulletMoveScheduler = Executors.newScheduledThreadPool(threadPoolBullet);
                bulletMoveScheduler.scheduleAtFixedRate(() -> {

                    int bulletX = bullet.x;
                    int bulletY = bullet.y;

                    bulletX += (int) (bulletSpeed * Math.cos(bulletDirection));
                    bulletY += (int) (bulletSpeed * Math.sin(bulletDirection));

                    bullet.setLocation(bulletX, bulletY);

                    if (bulletX < minCordBullet || bulletX > maxCordBullet ||
                            bulletY < minCordBullet || bulletY > maxCordBullet) {
                        bullets.remove(bullet);
                        bulletMoveScheduler.shutdown();
                    }

                    repaint();
                }, startTimeBullet, timeOutBullet, TimeUnit.MILLISECONDS);
            }, startTimeAttack, timeOutAttack, TimeUnit.SECONDS);
        }
    }

}