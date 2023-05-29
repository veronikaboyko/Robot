package gui.Window;

import gui.EndGameHandling;
import gui.GameVisualizer;
import gui.MainApplicationFrame;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;

public class DistanceToTarget extends JInternalFrame {

    public DistanceToTarget(ResourceBundle bundle) {
        super(bundle.getString("DistanceToTarget"), true, true, true, true);
        this.setLocation(680, 120);
        getDistance();
    }

    public void getDistance() {
        JTextArea distance = new JTextArea("",3, 2);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                ResourceBundle bundle = ResourceBundle.getBundle(MainApplicationFrame.locale);
                Point robotLocation = GameVisualizer.getPositionRobot();
                Point targetLocation = GameVisualizer.getPositionTarget();
                int result = (int) round(sqrt(
                        pow((targetLocation.getX() - robotLocation.getX()), 2) +
                                pow((targetLocation.getY() - robotLocation.getY()), 2)) -
                        EndGameHandling.diamGameTarget);
                result = result == -1 ? 0 : result;
                distance.setText(bundle.getString("DistanceToTarget") + ": \n" + (result));
            }
        }, 0, 20, TimeUnit.MILLISECONDS);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(distance, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        addInternalFrameListener(new WindowClosingHandler());
    }
}
