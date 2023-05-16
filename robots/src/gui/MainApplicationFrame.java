package gui;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.ResourceBundle;


import javax.swing.*;


import log.Logger;

import static java.util.Locale.ENGLISH;

public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();

    public MainApplicationFrame() {
        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset * 2, screenSize.height - inset * 2);

        setContentPane(desktopPane);

        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);

        GameWindow gameWindow = new GameWindow();
        gameWindow.setSize(400, 400);
        addWindow(gameWindow);

        setJMenuBar(generateMenuBar("locale_en_US"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    protected LogWindow createLogWindow() {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10, 10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }

    protected void addWindow(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
    }


    private JMenuBar generateMenuBar(String loc) {
        ResourceBundle bundle = ResourceBundle.getBundle(loc);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(lookAndFeelMenu(bundle));
        menuBar.add(locale(bundle));
        menuBar.add(logMenu(bundle));
        menuBar.add(exitButton(bundle));
        return menuBar;
    }

    private JMenu locale(ResourceBundle bundle) {
        JMenu locale = createMenu(bundle.getString("locale"), KeyEvent.VK_C, "Локализация",
                null);

        JMenuItem ruLocale = createItem(bundle.getString("ruLocale"), KeyEvent.VK_S,
                (event) -> {
                    Logger.debug("Русский язык");
                    this.invalidate();
                });

        ruLocale.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            Locale.setDefault(new Locale("ru", "RU", "RUSSIA"));
            this.removeAll();
            generateMenuBar("locale_tu_RU");
            setJMenuBar(getJMenuBar());
            revalidate();
            repaint();
        }));

        JMenuItem enLocale = createItem(bundle.getString("enLocale"), KeyEvent.VK_S,
                (event) -> {
                    Logger.debug("Анлийский язык");
                    this.invalidate();
                });

        enLocale.addActionListener(e -> SwingUtilities.invokeLater(() -> {
                    Locale.setDefault(ENGLISH);
                    this.removeAll();
                    generateMenuBar("locale_en_US");
                    setJMenuBar(getJMenuBar());
                    revalidate();
                    repaint();
                }
        ));
        locale.add(ruLocale);
        locale.add(enLocale);

        return locale;
    }

    private JMenu lookAndFeelMenu(ResourceBundle bundle) {

        JMenu lookAndFeelMenu = createMenu(bundle.getString("lookAndFeel"), KeyEvent.VK_V,
                "Управление режимом отображения приложения",
                createItem(bundle.getString("system"), KeyEvent.VK_S,
                        (event) -> {
                            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                            this.invalidate();
                        }
                ));

        lookAndFeelMenu.add(createItem(bundle.getString("universal"), KeyEvent.VK_S,
                (event) -> {
                    setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    this.invalidate();
                }));

        return lookAndFeelMenu;
    }

   private JMenu logMenu(ResourceBundle bundle) {
        return createMenu(bundle.getString("logs"), KeyEvent.VK_T, "Тестовые команды",
                createItem(bundle.getString("logMessage"), KeyEvent.VK_S,
                        (event) -> {
                            Logger.debug("Новая строка");
                            this.invalidate();
                        }
                ));
    }

    private JMenu exitButton(ResourceBundle bundle) {
        JMenu exit = createMenu(bundle.getString("exit"), KeyEvent.VK_X, "Закрытие приложения",
                createItem(bundle.getString("exit"), KeyEvent.VK_V,
                        (event) -> {
                            Logger.debug("Закрыть приложение");
                            this.invalidate();
                        }
                ));
        exit.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(MainApplicationFrame.this, "Вы уверены, что хотите выйти?", "Подтверждение выхода", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        });

        return exit;
    }


    private JMenuItem createItem(String data, int value, ActionListener actionListener) {
        JMenuItem jMenuItem = new JMenuItem(data, value);
        jMenuItem.addActionListener(actionListener);
        return jMenuItem;
    }

    private JMenu createMenu(String data, int value, String textDescription, JMenuItem item) {
        JMenu menu = new JMenu(data);
        menu.setMnemonic(value);
        menu.getAccessibleContext().setAccessibleDescription(textDescription);
        if (item != null)
            menu.add(item);
        return menu;
    }

    private void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            // just ignore
        }
    }
}
