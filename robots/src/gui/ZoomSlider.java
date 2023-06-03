package gui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ZoomSlider extends JPanel {
    private static final int MIN_ZOOM = 1;
    private static final int MAX_ZOOM = 10;
    private static final int DEFAULT_ZOOM = 5;
    private double zoom = 1.0;
    private JSlider zoomSlider;
    private GameVisualizer gameVisualizer;

    public ZoomSlider(GameVisualizer gameVisualizer) {
        this.gameVisualizer = gameVisualizer;
        setLayout(new BorderLayout());

        zoomSlider = new JSlider(JSlider.HORIZONTAL, MIN_ZOOM, MAX_ZOOM, DEFAULT_ZOOM);
        zoomSlider.setMajorTickSpacing(1);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setPaintLabels(true);

        zoomSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int zoomValue = zoomSlider.getValue();
                double scale = zoomValue / 5.0;
                zoom = scale;
                gameVisualizer.repaint();
            }
        });

        add(zoomSlider, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    public double getScale() {
        return zoom;
    }

    public void setScale(double scale) {
        zoom = scale;
    }
}
