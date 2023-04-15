package gui;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;


public class WindowClosingHandler extends InternalFrameAdapter {

    @Override
    public void internalFrameClosing(InternalFrameEvent e) {
        if (shouldCloseWindow(e.getInternalFrame())) {
            e.getInternalFrame().dispose();
        } else {
            e.getInternalFrame().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
    }

    public static boolean shouldCloseWindow(Component window) {
        int option = JOptionPane.showConfirmDialog(
                window,
                "Are you sure you want to close this window?",
                "Confirm",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return option == JOptionPane.YES_OPTION;
    }
}
