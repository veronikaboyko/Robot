package gui;

import java.awt.*;
import java.util.ResourceBundle;

import log.LogChangeListener;
import log.LogEntry;
import log.LogWindowSource;

import javax.swing.*;

public class LogWindow extends JInternalFrame implements LogChangeListener{
    private LogWindowSource m_logSource;
    private TextArea m_logContent;

    public LogWindow(LogWindowSource logSource, ResourceBundle bundle)
    {
        super(bundle.getString("LogWindow"), true, true, true, true);
        m_logSource = logSource;
        m_logSource.registerListener(this);
        m_logContent = new TextArea("");
        m_logContent.setSize(200, 500);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_logContent, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        addInternalFrameListener(new WindowClosingHandler());
        updateLogContent();

    }

    private void updateLogContent()
    {
        StringBuilder content = new StringBuilder();
        for (LogEntry entry : m_logSource.all())
        {
            content.append(entry.getMessage()).append("\n");
        }
        m_logContent.setText(content.toString());
        m_logContent.invalidate();
    }
    
    @Override
    public void onLogChanged()
    {
        EventQueue.invokeLater(this::updateLogContent);
    }

    @Override
    public void doDefaultCloseAction() {
        try {
            m_logSource.unregisterListener(this);
        } finally {
            super.doDefaultCloseAction();
        }
    }
}
