package gui;

import org.ini4j.Ini;
import org.ini4j.Profile;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EndGameHandling {

    public static int timeOutBullet;
    public static int timeOutAttack;
    public static int bulletSpeed;
    public static int startTimeBullet;
    public static int startTimeAttack;
    public static int threadPoolBullet;
    public static int timeOutResetGame;
    public static int minCordBullet;
    public static int maxCordBullet;

    EndGameHandling() {
        try {
            Profile.Section modelSection = new Ini(new File("config.ini")).get("model");
            timeOutBullet = modelSection.get("timeOutBullet", Integer.class);
            bulletSpeed = modelSection.get("bulletSpeed", Integer.class);
            startTimeBullet = modelSection.get("startTimeBullet", Integer.class);
            startTimeAttack = modelSection.get("startTimeAttack", Integer.class);
            threadPoolBullet = modelSection.get("threadPoolBullet", Integer.class);
            timeOutResetGame = modelSection.get("timeOutResetGame", Integer.class);
            minCordBullet = modelSection.get("minCordBullet", Integer.class);
            maxCordBullet = modelSection.get("maxCordBullet", Integer.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int drawBullet = 12;

    public static int diamGameTarget = 10;

    private Map<String, Boolean> gameState = new HashMap<>();

    public void checkGameState(Point robotPosition, Point gameTargetPosition, ArrayList<Point> bullets, double zoom) {
        int newGameTargetX = (int) (gameTargetPosition.getX() * zoom);
        int newGameTargetY = (int) (gameTargetPosition.getY() * zoom);
        Point newGameTargetPosition = new Point(newGameTargetX, newGameTargetY);

        checkLose(robotPosition, bullets);
        checkWin(robotPosition, newGameTargetPosition);
    }


    private void checkLose(Point robotPosition, ArrayList<Point> bullets) {
        for (Point bullet : bullets) {
            if (isCollision(robotPosition, bullet, drawBullet)) {
                gameState.put("lose", true);
                break;
            }
        }
    }

    private void checkWin(Point robotPosition, Point gameTargetPosition) {
        if (isCollision(robotPosition, gameTargetPosition, diamGameTarget)) {
            gameState.put("win", true);
        }
    }

    public Map<String, Boolean> getGameState() {
        return gameState;
    }

    public void cleanGameState() {
        gameState = new HashMap<>();
    }

    private boolean isCollision(Point p1, Point p2, int diameter) {
        double distance = p1.distance(p2);
        return distance <= diameter;
    }

    public static void shootBullets(ArrayList<Point> towers, ArrayList<Point> bullets, double zoom) {

        ScheduledExecutorService bulletScheduler = Executors.newScheduledThreadPool(towers.size());

        for (Point let : towers) {

            double bulletDirection = Math.random() * 2 * Math.PI;

            Point bullet = new Point((int) (let.x * zoom), (int) (let.y * zoom));
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
            }, startTimeBullet, timeOutBullet, TimeUnit.MILLISECONDS);
        }
    }
}
