package Kernel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

public class Adapter {
    private GraphicsOperation drawTable;

    public Adapter(GraphicsOperation graphicsOperation, int v) {
        drawTable = graphicsOperation;
        border = v;
        init(true);
    }

    private int border;
    private int x, y;
    private int color;
//    public boolean showScore;

    class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    class Stone {
        Point point;
        int color;

        Stone(int x, int y, int c) {
            point = new Point(x, y);
            color = c;
        }
    }

    public void init(boolean importStones) {
        x = -1;
        y = -1;
        color = Color.BLACK;
        stones = new Stone[19][19];
        boardForKo = new Stone[2][19][19];
        if (importStones) {
            importedStones = new LinkedList<>();
        }
        placedStones = new LinkedList<>();
    }

    public int getImportedStonesNum() {
        return importedStones.size() - 1;
    }

    public void jumpToStone(int index) {
        init(false);
        if (index < 0) {
            index = 0;
        }
        // 设个变量保存一下下面就不用调用两次了，算是空间复杂度换时间复杂度
        int placedStonesNum = getImportedStonesNum();
        if (index > placedStonesNum) {
            index = placedStonesNum;
        }
        currentStoneIndex = index;
        index += 1;
        for (int i = 0; i < index; i++) {
            genStone(importedStones.get(i).charAt(0) - baseASCII, importedStones.get(i).charAt(1) - baseASCII);
        }
    }

//    private int[][] score = new int[19][19];

    private Stone[][] stones; // 判断stones[i][j]是否为空即可，不需要单独定义一个boolean[][] hasStone
    private Stone[][][] boardForKo; // 用于判断提劫禁手的历史局面Stone[0][19][19]为上一个局面，Stone[1][19][19]为当前局面
    // 实现棋局导入导出功能，直觉或者说第一感就是用一个集合保存每一步棋，这里使用类似“AA”之双坐标字母表示落子位置，先求实现功能，至于怎样优雅优化、变量名方法名怎么起、方法放哪里云云是后面的事。
    private LinkedList<String> importedStones;
    private LinkedList<String> placedStones;
    private int currentStoneIndex;
    private final int baseASCII = 'A';
    private boolean followImportedStones = true; // 导入和跳转后有没有试下

    public boolean setPlacedStonesToFile(File file) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            for (String placedStone : placedStones) {
                fileOutputStream.write(placedStone.getBytes()); // 由于是严格按照每一步棋都是用双坐标字母表示位置，所以可以不用空格划分了，毕竟不需要给人看，不需要可读性。
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean getPlacedStonesFromFile(File file) {
        init(true);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            boolean gotTwoBytes = false;
            StringBuilder location = new StringBuilder();
            int b;
            while ((b = fileInputStream.read()) != -1) {
                location.append((char) b);
                if (gotTwoBytes) {
                    importedStones.add(location.toString());
                    location = new StringBuilder();
                }
                gotTwoBytes = !gotTwoBytes;
            }

            for (String importedStone : importedStones) {
                // System.out.println(importedStone);
                genStone(importedStone.charAt(0) - baseASCII, importedStone.charAt(1) - baseASCII);
            }
            currentStoneIndex = getImportedStonesNum();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // 使用字母表示坐标是因为字母可以直接写在一起之类似“AA”，而数字直接需要分隔符。
    private String genLocation(int x, int y) {
        String location = "";
        location += (char) (baseASCII + x);
        location += (char) (baseASCII + y);
        return location;
    }

//    private int abs(int a) {
//        if (a < 0) return -a;
//        return a;
//    }

//    private void updateScore(int x, int y, int color) {
//        int base = 1;
//        if (1 == color) base = -1;
//        for (int i = 0; i < 19; i++) {
//            for (int j = 0; j < 19; j++) {
//                score[i][j] += base * (19 * 2 - abs(x - i) - abs(y - j));
//            }
//        }
//    }

//    private void drawScore(int x, int y) {
//        drawTable.drawString(String.valueOf(score[x][y]), (float) (x + 0.5) * border, (float) (y + 1.25) * border);
//    }


    private boolean[][] visited;
    private LinkedList<Stone> visitedStones = new LinkedList<>();
    private boolean isAlive;

    // 除了像这样将judgeAlive()定义为void并定义一个全局boolean变量保存最终判断结果，另一种方法就是收集递归结果之类似将本函数定义为boolean然后boolean[] hasAliveFriends = new boolean[4];然后return hasAliveFriends[0] || hasAliveFriends[1] || hasAliveFriends[2] || hasAliveFriends[3];。
    private void judgeAlive(int x, int y, int color) {
        if (isAlive) return;
        if (null == stones[x][y]) { // 如果需要计算总气数，就是在这里统计，先是去掉if (isAlive) return;这一句，然后在这里加上visited[x][y] = true;表示或者说实现重叠气只计算一次。
            isAlive = true;
            return;
        } else if (stones[x][y].color != color) {
            return;
        }
        visited[x][y] = true;
        visitedStones.add(stones[x][y]);
        if (x != 0 && !visited[x - 1][y]) judgeAlive(x - 1, y, color);
        if (x != 18 && !visited[x + 1][y]) judgeAlive(x + 1, y, color);
        if (y != 0 && !visited[x][y - 1]) judgeAlive(x, y - 1, color);
        if (y != 18 && !visited[x][y + 1]) judgeAlive(x, y + 1, color);
    }

    private void findDeadEnemies(int x, int y, int color, boolean doRemove) {
        visited = new boolean[19][19];
//        visitedStones = new LinkedList<>(); // clear()似乎更好
        visitedStones.clear();
        isAlive = false;
        if (null != stones[x][y] && stones[x][y].color != color) {
            judgeAlive(x, y, getOppositeStoneColor(color));
            if (doRemove && !isAlive) {
//                while (!visitedStones.isEmpty()) {
//                    Stone stone = visitedStones.remove();
                // 这是用空间复杂度换时间复杂度，不用遍历19x19的visited数组，直接从visitedStones链表中获取，虽然似乎不够优雅。至于说这两者合并，比如将visitedStones定义为哈希表，key为x+" "+y之字符串，对应的value存在说明visited[x][y]为true，但这个判断比直接判断visited[x][y]效率上就差多了，这相当于或者说就是时间复杂度换空间复杂度了之又换回去了。暂时没有很好的方案，故暂搁置。
                for (Stone stone : visitedStones) {
                    // stone = null; // 这样是不行的
                    stones[stone.point.x][stone.point.y] = null;
                }
//                for (int i = 0; i < 19; i++) {
//                    for (int j = 0; j < 19; j++) {
//                        if (visited[i][j]) {
//                            stones[i][j] = null;
//                        }
//                    }
//                }
            }
        }
    }

    private int getOppositeStoneColor(int color) {
        if (Color.BLACK == color) {
            return Color.WHITE;
        } else {
            return Color.BLACK;
        }
    }

    private void copyBoard(Stone[][] from, Stone[][] to) {
        for (int i = 0; i < 19; i++) {
            System.arraycopy(from[i], 0, to[i], 0, 19);
        }
    }

    private int getStoneStatus(Stone stone) {
        if (null == stone) {
            return 0;
        } else {
            return stone.color;
        }
    }

    private boolean isTheSameBoard(Stone[][] a, Stone[][] b) {
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                if (getStoneStatus(a[i][j]) != getStoneStatus(b[i][j])) return false;
            }
        }
        return true;
    }

    // 根据importedStones和索引i生成局面
    public void genStone(int i) {
        String importedStone = importedStones.get(i);
        genStone(importedStone.charAt(0) - baseASCII, importedStone.charAt(1) - baseASCII);
    }

    public void genStone(int x, int y) {
//        int resultCode = 0; // 0表示正常，-1是自杀错误，1是提劫禁手
        boolean needToRecoverBoard = false;
        stones[x][y] = new Stone(x, y, color);

        if (x != 0) findDeadEnemies(x - 1, y, color, true);
        if (x != 18) findDeadEnemies(x + 1, y, color, true);
        if (y != 0) findDeadEnemies(x, y - 1, color, true);
        if (y != 18) findDeadEnemies(x, y + 1, color, true);

        findDeadEnemies(x, y, getOppositeStoneColor(color), false);
        if (!isAlive) {
            stones[x][y] = null;
            drawTable.showMessage("You cannot kill yourself!");
            return;
//            Toast.makeText(context, "You cannot kill yourself!", Toast.LENGTH_SHORT).show();
//            resultCode = -1;
        }

        // 劫作为一个或者说一种特例，似乎很难优雅地处理或者说实现，入低层级之拟合之比如必然是先提一子然后考察落的子周围分布之模式识别似乎很不靠谱。最终注意到如果提劫后直接提回来，棋局就会和上一步棋落子前一样，这就就可以作为提劫禁手的判断依据了。
        if (isTheSameBoard(stones, boardForKo[0])) {
            needToRecoverBoard = true;
            drawTable.showMessage("Forbidden move!");
//            Toast.makeText(context, "Forbidden move!", Toast.LENGTH_SHORT).show();
//            resultCode = 1;
        }

        // 为了判断提劫禁手，似乎太大费周章了，后面或者说后续看看有没有更好的解决方案吧。
        if (needToRecoverBoard) {
            copyBoard(boardForKo[1], stones);
        } else {
            placedStones.add(genLocation(this.x, this.y));
//            updateScore(x, y, color);
            drawTable.doRepaint();
//            invalidate();
            this.color = getOppositeStoneColor(color);
            copyBoard(boardForKo[1], boardForKo[0]);
            copyBoard(stones, boardForKo[1]);
        }
//        return resultCode;
    }

    private void drawStone(int x, int y, int color, boolean fillStone) {
        x += 1;
        y += 1;
//            paint.setStyle(Paint.Style.FILL);
        int radius = border >> 1;
        if (!fillStone) {
//            drawTable.drawCircle(border * x, border * y, radius, getOppositeStoneColor(color), true);
            drawTable.drawCircle(border * x, border * y, radius, color, true);
            drawTable.drawCircle(border * x, border * y, radius / 2, getOppositeStoneColor(color), false);
        } else {
            drawTable.drawCircle(border * x, border * y, radius, color, true);
        }
    }

    public void drawBoard() {
//        paint.setColor(Color.GREEN);
//        paint.setStyle(Paint.Style.FILL);
//        paint.setAntiAlias(true);
        drawTable.fillRect(0, 0, border * 20, border * 20, Color.GREEN);

//        paint.setColor(Color.BLACK);
        int[] width = {1, 3};
        int startX = border;
        int startY = border;
        for (int i = 1; i < 18; i++) {
            startX += border;
            startY += border;
            drawTable.drawLine(startX, border, startX, border * 19, width[0]);
            drawTable.drawLine(border, startY, border * 19, startY, width[0]);
        }

        // 线条宽度默认是0
        // System.out.println(paint.getStrokeWidth());

//        paint.setStrokeWidth(3);
        drawTable.drawLine(border, border, border, border * 19, width[1]);
        drawTable.drawLine(border, border, border * 19, border, width[1]);
        drawTable.drawLine(border, border * 19, border * 19, border * 19, width[1]);
        drawTable.drawLine(border * 19, border, border * 19, border * 19, width[1]);

        int radius = width[1];
        drawTable.drawCircle(border * 4, border * 4, radius, Color.BLACK, true);
        drawTable.drawCircle(border * 4, border * 10, radius, Color.BLACK, true);
        drawTable.drawCircle(border * 4, border * 16, radius, Color.BLACK, true);
        drawTable.drawCircle(border * 10, border * 4, radius, Color.BLACK, true);
        drawTable.drawCircle(border * 10, border * 10, radius, Color.BLACK, true);
        drawTable.drawCircle(border * 10, border * 16, radius, Color.BLACK, true);
        drawTable.drawCircle(border * 16, border * 4, radius, Color.BLACK, true);
        drawTable.drawCircle(border * 16, border * 10, radius, Color.BLACK, true);
        drawTable.drawCircle(border * 16, border * 16, radius, Color.BLACK, true);

        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                if (null != stones[i][j]) {
                    drawStone(i, j, stones[i][j].color, true);
                }
            }
        }

        if (x != -1 && null == stones[x][y]) {
            drawStone(x, y, color, false);
        }
//        paint.setStrokeWidth(0);
    }

    public void click(float x, float y) {
        if ((border * 0.5 <= x && x < border * 19.5) && (border * 0.5 <= y && y < border * 19.5)) {
            // 加上0.5是让识别点更接近触摸点
            this.x = (int) (x / border + 0.5) - 1;
            this.y = (int) (y / border + 0.5) - 1;
        } else {
            this.x = -1;
            this.y = -1;
        }
    }

    public void placeStone() {
        if (x == -1) {
            if (currentStoneIndex < getImportedStonesNum()) {
                currentStoneIndex += 1;
//                        drawTable.genStone(drawTable.importedStones.get(drawTable.currentStoneIndex).charAt(0) - drawTable.baseASCII, drawTable.importedStones.get(drawTable.currentStoneIndex).charAt(1) - drawTable.baseASCII, DrawTable.color);
                if (followImportedStones) {
                    genStone(currentStoneIndex);
                } else {
                    followImportedStones = true;
                    jumpToStone(currentStoneIndex); // 这样每下一步棋都需要重新下之前的每一步棋，似乎效率略低，虽然实际体验不出来有什么卡顿。不过这也是没办法的，毕竟支持复盘时试下，如何恢复棋局是个问题，暂搁置。
                }
            } else {
                drawTable.showMessage("Please select a point first!");
            }
            return;
        }
        if (null != stones[x][y]) {
            drawTable.showMessage("There is already a stone here!");
            return;
        }
        followImportedStones = false; // 当落点是自杀或提劫禁手其实也算没试下，但处理起来很麻烦。最简单的就是把genStone()的返回值改为boolean，但其他地方的返回值不处理会警告。这里简单处理，算是效率复杂度换优雅复杂度了，毕竟虽说没改变棋局，但确实是想要试下的。
        genStone(x, y);
    }

    private boolean doneRandomPlay = true;

    public void randomPlay() {
        if (!doneRandomPlay) {
            drawTable.showMessage("Please wait until random play is done!");
            return;
        }
        doneRandomPlay = false;
        init(false);
        LinkedList<Integer> availablePoints = new LinkedList<>();
        for (int i = 0; i < 361; i++) {
            availablePoints.add(i);
        }

        Random random = new Random();
        for (int i = 0; i < 361; i++) {
            int data = availablePoints.remove(random.nextInt(availablePoints.size()));
            x = data / 19;
            y = data - data / 19 * 19;
            drawTable.doRepaint();
            genStone(x, y);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        doneRandomPlay = true;
    }

    private int[] getStonesCount() {
        // stonesCount[0]是黑子总数，stonesCount[1]是白子总数。
        int[] stonesCount = {0, 0};
        int color;
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                color = getStoneStatus(stones[i][j]);
                if (Color.BLACK == color) {
                    stonesCount[0] += 1;
                } else if (Color.WHITE == color) {
                    stonesCount[1] += 1;
                }
            }
        }
        return stonesCount;
    }

    public void score() {
        int[] stonesCount = getStonesCount();
        int preColor, currentColor;
        int emptyCount = 0;
        for (int i = 0; i < 19; i++) {
            preColor = Color.GREEN;
            // 这里可以遍历到20之遍历至右边界，不过需要处理好stones的遍历，放到后面处理右边界即可。
            for (int j = 0; j < 19; j++) {
                currentColor = getStoneStatus(stones[i][j]);
                if (0 == currentColor) {
                    emptyCount += 1;
                } else {
                    if (currentColor == preColor || Color.GREEN == preColor) {
                        stonesCount[Color.BLACK == currentColor ? 0 : 1] += emptyCount;
                    }
                    emptyCount = 0;
                    preColor = currentColor;
                }
            }
            // 如下代码没有正确将emptyCount置零或者说清零。
//            if (0 != emptyCount && Color.GREEN != preColor) {
//                stonesCount[Color.BLACK == preColor ? 0 : 1] += emptyCount;
//                emptyCount = 0;
//            }
            if (0 != emptyCount) {
                if (Color.GREEN != preColor) {
                    stonesCount[Color.BLACK == preColor ? 0 : 1] += emptyCount;
                }
                emptyCount = 0;
            }
        }
        drawTable.showMessage("Black : " + stonesCount[0] + "\nWhite : " + stonesCount[1]);
    }
}
