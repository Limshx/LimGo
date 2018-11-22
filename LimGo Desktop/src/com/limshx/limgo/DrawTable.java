package com.limshx.limgo;

import Kernel.Adapter;
import Kernel.GraphicsOperation;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class DrawTable extends JPanel implements GraphicsOperation {
    int windowSize = 600;
    Adapter adapter;
    private Graphics g;

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, int width) {
        g.setColor(Color.black);
        ((Graphics2D) g).setStroke(new BasicStroke(width));
        g.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void fillRect(int x, int y, int width, int height, int color) {
        g.setColor(new Color(color));
        g.fillRect(x, y, width, height);
    }

    @Override
    public void drawCircle(int x, int y, int r, int color, boolean fillCircle) {
        g.setColor(new Color(color));
        if (fillCircle) {
            g.fillOval(x - r, y - r, r * 2, r * 2);
        } else {
            g.drawOval(x - r, y - r, r * 2, r * 2);
        }
    }

//    @Override
//    public void drawString(String s, float x, float y) {
//        g.setColor(Color.BLACK);
//        g.setFont(new Font("圆体", Font.PLAIN, 32));
//        g.drawString(s, (int) x, (int) y);
//    }

    @Override
    public void showMessage(String s) {
        JOptionPane.showMessageDialog(null, s);
    }

    @Override
    public void doRepaint() {
        repaint();
    }

    DrawTable() {
        addMouseListener(new MouseListener() {
                             public void mousePressed(MouseEvent e) {
                                 if (MouseEvent.BUTTON1 == e.getButton()) {
                                     int x = e.getX();
                                     int y = e.getY();
                                     adapter.click(x, y);
                                 } else {
                                     adapter.placeStone();
                                 }
                                 doRepaint();
                             }//当用户按下鼠标按钮时发生

                             public void mouseReleased(MouseEvent e) {
                             }//当用户松开鼠标按钮时发生

                             public void mouseClicked(MouseEvent e) {
                             }

                             public void mouseEntered(MouseEvent e) {
                             }

                             public void mouseExited(MouseEvent e) {
                             }
                         }
        );
    }

    protected void paintComponent(Graphics g) {
        this.g = g; // 总想着getGraphics()云云如何获取g，没想到可以直接在这里获取
        //g.clearRect(0, 0, getWidth(), getHeight()); // 没这句就会有重影
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (null == adapter) {
            adapter = new Adapter(this, windowSize / 20);
        }
        adapter.drawBoard();
    }
}
