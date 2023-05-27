package gui;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.util.ResourceBundle;

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
        ResourceBundle bundle = ResourceBundle.getBundle(MainApplicationFrame.locale);
        UIManager.put("OptionPane.yesButtonText", bundle.getString("yes"));
        UIManager.put("OptionPane.noButtonText", bundle.getString("no"));
        int option = JOptionPane.showConfirmDialog(
                window,
                bundle.getString("exitWindow"),
                bundle.getString("confirm"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return option == JOptionPane.YES_OPTION;
    }
}
