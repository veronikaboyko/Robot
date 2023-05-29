package gui.Window;

import gui.GameVisualizer;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RobotPositionWindow extends JInternalFrame {

    private JTextArea cord = new JTextArea("", 3, 2);

    public RobotPositionWindow(ResourceBundle bundle) {
        super(bundle.getString("RobotPositionWindow"), true, true, true, true);
        this.setLocation(680, 25);
        getRobotCord();
    }

    private void getRobotCord() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Point robotLocation = GameVisualizer.getPositionRobot();
                cord.setText(
                        "X: " + robotLocation.getX() + "\n" +
                                "Y: " + robotLocation.getY());

            }
        }, 0, 30, TimeUnit.MILLISECONDS);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(cord, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        addInternalFrameListener(new WindowClosingHandler());
    }

}
