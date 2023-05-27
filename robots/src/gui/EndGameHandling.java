package gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EndGameHandling {
    private Map<String, Boolean> gameState = new HashMap<>();

    public void checkGameState(Point robotPosition, Point gameTargetPosition, ArrayList<Point> bullets){
        checkLose(robotPosition, bullets);
        checkWin(robotPosition, gameTargetPosition);
    }

    private void checkLose(Point robotPosition, ArrayList<Point> bullets) {
        for (Point bullet : bullets) {
            if (isCollision(robotPosition, bullet, 12)) {
                gameState.put("lose", true);
                break;
            }
        }
    }
    private void checkWin(Point robotPosition, Point gameTargetPosition) {
        if (isCollision(robotPosition, gameTargetPosition, 10)) {
            gameState.put("win", true);
        }
    }

    public Map<String, Boolean> getGameState() {
        return gameState;
    }

    public void cleanGameState(){
        gameState = new HashMap<>();
    }

    private boolean isCollision(Point p1, Point p2, int diameter) {
        double distance = p1.distance(p2);
        return distance <= diameter;
    }
}
