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
    private static String homeDirectory = System.getProperty("user.dir") + "/";

    public static void main(String[] args) {
        Main drawRect = new Main("LimGo");
        try {
            drawRect.setIconImage(ImageIO.read(drawRect.getClass().getResource("limgo.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // macOS下菜单栏放到全局菜单栏
        if (System.getProperty("os.name").contains("Mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        DrawTable drawTable = new DrawTable();
        drawRect.add(drawTable);
        JMenuBar jMenuBar = new JMenuBar();
        JMenu jMenu;
        JMenuItem[] jMenuItems = new JMenuItem[4];
        jMenu = new JMenu("项目");
        jMenuItems[0] = new JMenuItem("导入");
        jMenuItems[0].addActionListener(actionEvent -> {
            JFileChooser jFileChooser = new JFileChooser(homeDirectory);
            jFileChooser.setSelectedFile(openedFile);
            int result = jFileChooser.showOpenDialog(null);
            if (JOptionPane.YES_OPTION != result) {
                return;
            }
            File file = jFileChooser.getSelectedFile();
            if (null != file) {
                openedFile = file;
                homeDirectory = openedFile.getParent() + "/";
                JOptionPane.showMessageDialog(null, "已导入 \"" + openedFile.getName() + "\"");
                drawTable.adapter.getPlacedStonesFromFile(openedFile);
                drawTable.doRepaint();
            }
        });
        jMenuItems[1] = new JMenuItem("导出");
        jMenuItems[1].addActionListener(actionEvent -> {
            JFileChooser jFileChooser = new JFileChooser(homeDirectory);
//            jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            jFileChooser.setSelectedFile(openedFile);
            int result = jFileChooser.showOpenDialog(null);
            if (JOptionPane.YES_OPTION != result) {
                return;
            }
            File file = jFileChooser.getSelectedFile();
            if (null != file) {
                openedFile = file;
                homeDirectory = openedFile.getParent() + "/";
                if (openedFile.exists()) {
                    result = JOptionPane.showConfirmDialog(null, "项目 \"" + openedFile.getName() + "\" 已存在，覆盖？");
                    if (JOptionPane.YES_OPTION != result) {
                        return;
                    }
                }
                drawTable.adapter.setPlacedStonesToFile(openedFile);
            }
        });
        jMenuItems[2] = new JMenuItem("清空");
        jMenuItems[2].addActionListener(actionEvent -> {
            int result = JOptionPane.showConfirmDialog(null, "清空工作区？");
            if (JOptionPane.YES_OPTION != result) {
                return;
            }
            drawTable.adapter.init(true);
            drawTable.doRepaint();
        });
        jMenu.add(jMenuItems[0]);
        jMenu.add(jMenuItems[1]);
        jMenu.add(jMenuItems[2]);
        jMenuBar.add(jMenu);
        jMenu = new JMenu("工具");
        jMenuItems[0] = new JMenuItem("跳转");
        jMenuItems[1] = new JMenuItem("随机");
        jMenuItems[2] = new JMenuItem("数目");
        jMenuItems[3] = new JMenuItem("关于");
        jMenuItems[0].addActionListener(actionEvent -> {
            if (-1 == drawTable.adapter.getImportedStonesNum()) {
                drawTable.showMessage("请先导入项目！");
                return;
            }
            int importedStonesNum = drawTable.adapter.getImportedStonesNum();
            try {
                int index = Integer.parseInt(JOptionPane.showInputDialog("0 ~ " + importedStonesNum + " :", "0"));
                drawTable.adapter.jumpToStone(index);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "请输入整数！");
            }
        });
        jMenuItems[1].addActionListener(actionEvent -> new Thread(() -> drawTable.adapter.randomPlay()).start());
        jMenuItems[2].addActionListener(actionEvent -> drawTable.adapter.score());
        jMenuItems[3].addActionListener(actionEvent -> drawTable.showMessage("左键单击棋盘线框内区域选择落点，点击棋盘线框外绿色区域取消选中；右键点击确认落子。"));
        jMenu.add(jMenuItems[0]);
        jMenu.add(jMenuItems[1]);
        jMenu.add(jMenuItems[2]);
        jMenu.add(jMenuItems[3]);
        jMenuBar.add(jMenu);
        drawRect.setJMenuBar(jMenuBar);
        drawRect.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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