package gui;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;


import javax.swing.*;


import log.Logger;

import static java.util.Locale.ENGLISH;

public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();
    JMenuBar menuBar;
    LogWindow logWindow;
    GameWindow gameWindow;
    ResourceBundle bundle;
    static Boolean flagCloseWindow = true;

    public MainApplicationFrame() {
        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset * 2, screenSize.height - inset * 2);

        setContentPane(desktopPane);

        logWindow = createLogWindow();
        addWindow(logWindow);

        gameWindow = new GameWindow();
        gameWindow.setSize(400, 400);
        addWindow(gameWindow);

        bundle = ResourceBundle.getBundle("locale_en_US");
        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        restoreDesktopState();
        pack();
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


    private JMenuBar generateMenuBar() {
        menuBar = new JMenuBar();
        menuBar.add(lookAndFeelMenu());
        menuBar.add(locale());
        menuBar.add(logMenu());
        menuBar.add(exitButton());
        return menuBar;
    }

    private JMenu locale() {
        JMenu locale = createMenu(bundle.getString("locale"), KeyEvent.VK_C, "Локализация",
                null);

        JMenuItem ruLocale = createItem(bundle.getString("ruLocale"), KeyEvent.VK_S,
                (event) -> {
                    Logger.debug("Русский язык");
                    flagCloseWindow = false;
                });

        ruLocale.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                Locale.setDefault(new Locale("ru", "RU", "RUSSIA"));
                bundle = ResourceBundle.getBundle("locale_ru_RU");
                updateLocale();
            });
        });

        JMenuItem enLocale = createItem(bundle.getString("enLocale"), KeyEvent.VK_S,
                (event) -> {
                    Logger.debug("English language");
                    flagCloseWindow = true;
                    this.invalidate();
                });

        enLocale.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                Locale.setDefault(ENGLISH);
                bundle = ResourceBundle.getBundle("locale_en_US");
                updateLocale();
            });
        });
        locale.add(ruLocale);
        locale.add(enLocale);

        return locale;
    }

    private void updateLocale() {
        menuBar.removeAll();
        generateMenuBar();
        setJMenuBar(menuBar);
        for (JInternalFrame frame : desktopPane.getAllFrames())
            frame.setTitle(bundle.getString(frame.getClass().getSimpleName()));
        revalidate();
        repaint();
    }

    private JMenu lookAndFeelMenu() {

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

    private JMenu logMenu() {
        return createMenu(bundle.getString("logs"), KeyEvent.VK_T, "Тестовые команды",
                createItem(bundle.getString("logMessage"), KeyEvent.VK_S,
                        (event) -> {
                            Logger.debug("Новая строка");
                            this.invalidate();
                        }
                ));
    }

    private JMenu exitButton() {
        JMenu exitMenu = new JMenu(bundle.getString("exit"));
        JMenuItem exitItem = new JMenuItem(bundle.getString("exit"));
        exitItem.addActionListener(
                e -> {
                    if (WindowClosingHandler.shouldCloseWindow(this)) {
                        saveDesktopState();
                        dispose();
                        System.exit(0);
                    }
                }
        );
        exitMenu.add(exitItem);
        return exitMenu;
    }

    private void saveDesktopState() {
        DesktopState state = new DesktopState();
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            state.addFrame(frame);
        }
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(Paths.get("window_state.dat")))) {
            out.writeObject(state);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restoreDesktopState() {
        File file = new File("window_state.dat");
        if (file.exists()) {
            JDesktopPane newPane = new JDesktopPane();
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file.toPath()))) {
                DesktopState state = (DesktopState) in.readObject();
                for (DesktopState.FrameState frameState : state.getFrames()) {
                    JInternalFrame frame;
                    if (frameState.returnFrameType().equals("LogWindow")) {
                        frame = createLogWindow();
                    } else {
                        frame = new GameWindow();
                        if (frameState.returnTitle().equals("Game window")) {
                            bundle = ResourceBundle.getBundle("locale_en_US");
                            Locale.setDefault(ENGLISH);
                            flagCloseWindow = true;
                        } else {
                            bundle = ResourceBundle.getBundle("locale_ru_RU");
                            Locale.setDefault(new Locale("ru", "RU", "RUSSIA"));
                            flagCloseWindow = false;
                        }
                    }
                    frameState.restore(frame);
                    newPane.add(frame);
                    frame.setVisible(true);
                }
                UIManager.put("OptionPane.yesButtonText", bundle.getString("yes"));
                UIManager.put("OptionPane.noButtonText", bundle.getString("no"));
                int option = JOptionPane.showConfirmDialog(
                        this,
                        bundle.getString("saveWindow"),
                        bundle.getString("confirm"),
                        JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    for (JInternalFrame frame : desktopPane.getAllFrames())
                        frame.dispose();
                    for (JInternalFrame frame : newPane.getAllFrames())
                        addWindow(frame);
                    updateLocale();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
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
