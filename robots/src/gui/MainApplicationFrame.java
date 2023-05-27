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
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import javax.swing.*;


import log.Logger;

import static java.util.Locale.ENGLISH;

public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();
    JMenuBar menuBar;
    LogWindow logWindow;
    ResourceBundle bundle;
    String locale = "locale_en_US";
    static Boolean flagLanguage = true;

    public MainApplicationFrame() {
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset * 2, screenSize.height - inset * 2);

        setContentPane(desktopPane);

        logWindow = createLogWindow();
        addWindow(logWindow);

        GameWindow gameWindow = new GameWindow();
        addWindow(gameWindow);

        bundle = ResourceBundle.getBundle(locale);
        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        restoreDesktopState();
        pack();
        checkState(gameWindow);

    }

    public void checkState(GameWindow gameWindow) {
        Map<String, Boolean> gameState = gameWindow.returnVisualizer().getEndGame().getGameState();

        boolean lose = gameState.getOrDefault("lose", false);
        boolean win = gameState.getOrDefault("win", false);
        UIManager.put("OptionPane.yesButtonText", bundle.getString("restart"));
        UIManager.put("OptionPane.noButtonText", bundle.getString("no"));
        if (lose)
            windowEndGame("sorry", "gameOver", gameWindow);
        else if (win)
            windowEndGame("congrat", "youWin", gameWindow);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                checkState(gameWindow);
                gameWindow.returnVisualizer().getEndGame().cleanGameState();
            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    public void windowEndGame(String title, String message, GameWindow gameWindow){
        int option = JOptionPane.showConfirmDialog(null,
                bundle.getString(message),
                bundle.getString(title),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        gameWindow.returnVisualizer().getEndGame().cleanGameState();
        if (option == JOptionPane.YES_OPTION) {
            gameWindow.dispose();
            GameWindow newGameWindow = new GameWindow();
            addWindow(newGameWindow);
            checkState(newGameWindow);
        }
        else
            gameWindow.dispose();
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
                    this.invalidate();
                });

        ruLocale.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                updateLocale("locale_ru_RU", new Locale("ru", "RU", "RUSSIA"));
            });
        });

        JMenuItem enLocale = createItem(bundle.getString("enLocale"), KeyEvent.VK_S,
                (event) -> {
                    Logger.debug("English language");
                    this.invalidate();
                });

        enLocale.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                updateLocale("locale_en_US", ENGLISH);
            });
        });
        locale.add(ruLocale);
        locale.add(enLocale);

        return locale;
    }

    private void updateLocale(String loc, Locale newLocale) {
        locale = loc;
        flagLanguage = locale.equals("locale_en_US");
        bundle = ResourceBundle.getBundle(locale);
        Locale.setDefault(newLocale);
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
            out.writeUTF(locale);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restoreDesktopState() {
        File file = new File("window_state.dat");
        if (file.exists()) {
            JDesktopPane newPane = new JDesktopPane();
            String locale;
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file.toPath()))) {
                DesktopState state = (DesktopState) in.readObject();
                locale = in.readUTF();
                for (DesktopState.FrameState frameState : state.getFrames()) {
                    JInternalFrame frame;
                    if (frameState.returnFrameType().equals("LogWindow"))
                        frame = createLogWindow();
                    else {
                        frame = new GameWindow();
                        checkState((GameWindow) frame);
                    }

                    frameState.restore(frame);
                    newPane.add(frame);
                    frame.setVisible(true);
                }
                bundle = ResourceBundle.getBundle(locale);
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
                    if (locale.equals("locale_en_US"))
                        updateLocale(locale, ENGLISH);
                    else
                        updateLocale(locale, new Locale("ru", "RU", "RUSSIA"));
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

    static public ResourceBundle localeChange() {
        String prop;
        if (MainApplicationFrame.flagLanguage)
            prop = "locale_en_US";
        else
            prop = "locale_ru_RU";
        return ResourceBundle.getBundle(prop);
    }
}
