package Kernel;

public interface GraphicsOperation {
    void drawLine(int x1, int y1, int x2, int y2, int width);

    void fillRect(int x, int y, int width, int height, int color);

    void drawCircle(int cx, int cy, int radius, int color, boolean fillCircle);

    void showMessage(String s);

    void doRepaint();
}
