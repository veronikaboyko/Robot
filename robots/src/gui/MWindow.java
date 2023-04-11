package gui;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;

public class MWindow extends JInternalFrame {
    protected TextArea m_logContent;
    public MWindow(String title, Object content){
        super(title, true, true, true, true);
        JPanel panel = new JPanel(new BorderLayout());
        if (content.equals("game")){
            GameVisualizer visualizer = new GameVisualizer();
            panel.add(visualizer, BorderLayout.CENTER);
        }
        else {
            m_logContent = new TextArea("");
            panel.add(m_logContent, BorderLayout.CENTER);
        }
        getContentPane().add(panel);
        pack();
        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosing(InternalFrameEvent e) {
                int option = JOptionPane.showInternalConfirmDialog(
                        MWindow.this,
                        "close the window?",
                        "confirm",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (option == JOptionPane.YES_OPTION)
                    dispose();
                else
                    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            }
        });
    }
}
