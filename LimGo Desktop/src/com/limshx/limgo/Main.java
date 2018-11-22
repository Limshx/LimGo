package com.limshx.limgo;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

class Main extends JFrame {
    private Main(String s) {
        super(s);
    }
    private static File openedFile;
    private static String homeDirectory;

    public static void main(String[] args) {
        Main drawRect = new Main("LimGo");
        try {
            drawRect.setIconImage(ImageIO.read(drawRect.getClass().getResource("limgo.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        homeDirectory = System.getProperty("user.dir") + "/";

        // macOS下菜单栏放到全局菜单栏
        if (System.getProperty("os.name").contains("Mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

//        if (System.getProperty("os.name").contains("Win")) {
//            homeDirectory = System.getProperty("user.dir") + "/";
//        }

//        Container cont = drawRect.getContentPane();
        DrawTable drawTable = new DrawTable();
        drawRect.add(drawTable);
//        drawTable.setLayout(null);
//        JScrollPane scr1 = new JScrollPane(drawTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//        cont.add(drawTable);

        JMenuBar jMenuBar = new JMenuBar();
        JMenu jMenu;
        JMenuItem[] jMenuItems = new JMenuItem[4];
        jMenu = new JMenu("File");
        jMenuItems[0] = new JMenuItem("Import");
        jMenuItems[0].addActionListener(actionEvent -> {
            JFileChooser jFileChooser = new JFileChooser(new File(homeDirectory));
            jFileChooser.showOpenDialog(null);
            File file = jFileChooser.getSelectedFile();
            if (null != file) {
                openedFile = file;
                JOptionPane.showMessageDialog(null, "Imported \"" + openedFile.getName() + "\"");
                drawTable.adapter.getPlacedStonesFromFile(openedFile);
                drawTable.doRepaint();
            }
        });
        jMenuItems[1] = new JMenuItem("Export");
        jMenuItems[1].addActionListener(actionEvent -> {
            String fileName = JOptionPane.showInputDialog("Please input a file name :", null != openedFile ? openedFile.getName() : null);
            if (null != fileName) {
                if (new File(homeDirectory + fileName).exists()) {
                    int result = JOptionPane.showConfirmDialog(null, "File \"" + fileName + "\" exists, overwrite it?");
                    if (JOptionPane.YES_OPTION != result) {
                        return;
                    }
                }
                drawTable.adapter.setPlacedStonesToFile(new File(homeDirectory + fileName));
            }
        });
        jMenuItems[2] = new JMenuItem("Clear");
        jMenuItems[2].addActionListener(actionEvent -> {
            drawTable.adapter.init(true);
            drawTable.doRepaint();
        });
        jMenu.add(jMenuItems[0]);
        jMenu.add(jMenuItems[1]);
        jMenu.add(jMenuItems[2]);
        jMenuBar.add(jMenu);
        jMenu = new JMenu("Tools");
        jMenuItems[0] = new JMenuItem("Jump");
        jMenuItems[1] = new JMenuItem("Random Play");
        jMenuItems[2] = new JMenuItem("Score");

        jMenuItems[0].addActionListener(actionEvent -> {
            if (-1 == drawTable.adapter.getImportedStonesNum()) {
                drawTable.showMessage("There are no imported stones!");
                return;
            }
            int importedStonesNum = drawTable.adapter.getImportedStonesNum();
            drawTable.adapter.jumpToStone(Integer.parseInt(JOptionPane.showInputDialog("0 ~ " + importedStonesNum + " :")));
        });
        jMenuItems[1].addActionListener(actionEvent -> new Thread(() -> drawTable.adapter.randomPlay()).start());
        jMenuItems[2].addActionListener(actionEvent -> drawTable.adapter.score());
        jMenu.add(jMenuItems[0]);
        jMenu.add(jMenuItems[1]);
        jMenu.add(jMenuItems[2]);
        jMenuBar.add(jMenu);
        jMenu = new JMenu("Help");
        jMenuItems[0] = new JMenuItem("About");
        jMenuItems[0].addActionListener(actionEvent -> drawTable.showMessage("左键单击棋盘线框内区域选择落点，点击棋盘线框外绿色区域取消选中；右键点击确认落子。"));
        jMenu.add(jMenuItems[0]);
        jMenuBar.add(jMenu);
        drawRect.setJMenuBar(jMenuBar);

//        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(drawRect.getGraphicsConfiguration());

        drawRect.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        drawRect.setSize(800, 860);
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        drawRect.setLocation(screenWidth / 2 - drawTable.windowSize / 2, screenHeight / 2 - drawTable.windowSize / 2);

        // JFrame适应JPanel大小
        drawTable.setPreferredSize(new Dimension(drawTable.windowSize, drawTable.windowSize));
        drawRect.setResizable(false);
        drawRect.pack();
        drawRect.setVisible(true);
    }
}