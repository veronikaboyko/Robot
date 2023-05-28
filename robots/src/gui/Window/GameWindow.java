package gui.Window;

import gui.GameVisualizer;

import java.awt.BorderLayout;
import java.util.ResourceBundle;
import javax.swing.*;


public class GameWindow extends JInternalFrame
{
    private final GameVisualizer m_visualizer;
    public GameWindow(ResourceBundle bundle)
    {
        super(bundle.getString("GameWindow"), true, true, true, true);
        m_visualizer = new GameVisualizer();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        addInternalFrameListener(new WindowClosingHandler());
        this.setLocation(250, 10);
        this.setSize(400, 400);

    }

    public GameVisualizer returnVisualizer() {
        return m_visualizer;
    }
}