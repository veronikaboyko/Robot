package gui;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.*;

public class DesktopState implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<FrameState> frames = new ArrayList<>();

    public void addFrame(JInternalFrame frame) {
        frames.add(new FrameState(frame, getFrameType(frame)));
    }


    public String getFrameType(JInternalFrame frame) {
        if (frame instanceof LogWindow) {
            return "LogWindow";
        } else if (frame instanceof GameWindow) {
            return "GameWindow";
        } else {
            return null;
        }
    }

    public List<FrameState> getFrames() {
        return frames;
    }

    public static class FrameState implements Serializable {

        private static final long serialVersionUID = 1L;

        private String frameType;
        private int x;
        private int y;
        private int width;
        private int height;
        private boolean icon;
        private boolean maximum;
        private boolean closed;
        private String title;
        private Locale locale;

        public String returnFrameType(){
            return frameType;
        }

        public String returnTitle(){
            return title;
        }

        public FrameState(JInternalFrame frame, String frameType) {
            this.frameType = frameType;
            x = frame.getX();
            y = frame.getY();
            width = frame.getWidth();
            height = frame.getHeight();
            icon = frame.isIcon();
            maximum = frame.isMaximum();
            closed = frame.isClosed();
            title = frame.getTitle();
            locale = frame.getLocale();
        }

        public void restore(JInternalFrame frame) {
            frame.setLocation(x, y);
            frame.setSize(width, height);
            frame.setTitle(title);
            frame.setLocale(locale);
            if (icon) {
                try {
                    frame.setIcon(true);
                } catch (java.beans.PropertyVetoException e) {
                    // do nothing
                }
            }
            if (maximum) {
                try {
                    frame.setMaximum(true);
                } catch (java.beans.PropertyVetoException e) {
                    // do nothing
                }
            }
            if (closed) {
                try {
                    frame.setClosed(true);
                } catch (java.beans.PropertyVetoException e) {
                    // do nothing
                }
            }
        }
    }
}