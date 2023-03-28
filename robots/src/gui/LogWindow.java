package gui;

import java.awt.EventQueue;
import java.awt.TextArea;

import log.LogChangeListener;
import log.LogEntry;
import log.LogWindowSource;

public class LogWindow extends MWindow implements LogChangeListener
{
    private LogWindowSource m_logSource;
    private TextArea m_logContent;

    public LogWindow(LogWindowSource logSource) 
    {
        super("Протокол работы", "log");
        m_logSource = logSource;
        m_logSource.registerListener(this);
        m_logContent = super.m_logContent;
        m_logContent.setSize(200, 500);

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
