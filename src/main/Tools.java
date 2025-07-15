package main;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Tools {
    private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();

    private final String tool;

    private Tools(String tool) {
        this.tool = tool;
    }
    private void drawSubWindow(Graphics2D g2,int x, int y, int width, int height) {
        Color c = new Color(0,0,0,180);
        g2.setColor(c);
        g2.fillRoundRect(x,y,width,height,35,35);
        c  = new Color(255, 228, 228);
        g2.setColor(c);
        g2.setStroke(new BasicStroke(5));
        g2.drawRoundRect(x+5,y+5,width-10,height-10,35,35);
    }
    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth) {
        if (text == null) return;
        FontMetrics fm = g2.getFontMetrics();
        int lineHeight = fm.getHeight();

        StringBuilder line = new StringBuilder();
        int currentY = y;

        String[] words = text.split("");

        for (String word : words) {
            String testLine = line + word;
            int lineWidth = fm.stringWidth(testLine);

            if (lineWidth > maxWidth) {
                g2.drawString(line.toString(), x, currentY);
                line = new StringBuilder(word);
                currentY += lineHeight;
            } else {
                line.append(word);
            }
        }


        if (line.length() > 0) {
            g2.drawString(line.toString(), x, currentY);
        }
    }

    public static Tools getTools(String toolName) {
        return new Tools(toolName);
    }

    public void execute(Object... args) {
        String cacheKey = tool + "_" + args.length;
        Method methodToInvoke = METHOD_CACHE.get(cacheKey);

        try {
            if (methodToInvoke == null) {
                for (Method m : getClass().getDeclaredMethods()) {
                    if (m.getName().equals(tool) && m.getParameterCount() == args.length) {
                        m.setAccessible(true);
                        methodToInvoke = m;
                        METHOD_CACHE.put(cacheKey, methodToInvoke);
                        break;
                    }
                }
            }

            if (methodToInvoke == null) {
                throw new NoSuchMethodException("No such tool: " + tool + " with " + args.length + " arguments");
            }

            methodToInvoke.invoke(this, args);

        } catch (Exception e) {
            System.err.println("Tool execution failed for '" + tool + "'.");
            e.printStackTrace();
        }
    }

    /**
     * A*寻路算法，返回从起点到终点的路径（包含起点和终点），每个节点为Point(col, row)。
     * 若无路径，返回空列表。
     * @param startCol 起点列
     * @param startRow 起点行
     * @param endCol 终点列
     * @param endRow 终点行
     * @param tileManager 地图管理器
     * @param maxCol 地图最大列数
     * @param maxRow 地图最大行数
     * @return 路径点列表
     */
    public static java.util.List<Point> findPathAStar(
            int startCol, int startRow, int endCol, int endRow,
            tile.TileManager tileManager, int maxCol, int maxRow) {
        class Node implements Comparable<Node> {
            int col, row;
            int g, h, f;
            Node parent;
            Node(int col, int row, int g, int h, Node parent) {
                this.col = col; this.row = row; this.g = g; this.h = h; this.f = g + h; this.parent = parent;
            }
            @Override public int compareTo(Node o) { return Integer.compare(this.f, o.f); }
            @Override public boolean equals(Object o) {
                if (!(o instanceof Node)) return false;
                Node n = (Node)o;
                return n.col == col && n.row == row;
            }
            @Override public int hashCode() { return col * 10000 + row; }
        }
        java.util.PriorityQueue<Node> open = new java.util.PriorityQueue<>();
        java.util.HashSet<Point> closed = new java.util.HashSet<>();
        open.add(new Node(startCol, startRow, 0, Math.abs(endCol-startCol)+Math.abs(endRow-startRow), null));
        int step = 0;
        int maxStep = 4000; // 最大步数限制，防止死循环
        while (!open.isEmpty() && step < maxStep) {
            Node cur = open.poll();
            if (cur.col == endCol && cur.row == endRow) {
                java.util.LinkedList<Point> path = new java.util.LinkedList<>();
                while (cur != null) {
                    path.addFirst(new java.awt.Point(cur.col, cur.row));
                    cur = cur.parent;
                }
                return path;
            }
            closed.add(new java.awt.Point(cur.col, cur.row));
            int[][] dirs = {{0,1},{1,0},{0,-1},{-1,0}};
            for (int[] d : dirs) {
                int nc = cur.col + d[0], nr = cur.row + d[1];
                if (nc<0||nr<0||nc>=maxCol||nr>=maxRow) continue;
                if (closed.contains(new java.awt.Point(nc, nr))) continue;
                int tileNum = tileManager.mapTileNum[nc][nr];
                if (tileManager.tile[tileNum]!=null && tileManager.tile[tileNum].collision) continue;
                Node next = new Node(nc, nr, cur.g+1, Math.abs(endCol-nc)+Math.abs(endRow-nr), cur);
                open.add(next);
            }
            step++;
        }
        return java.util.Collections.emptyList();
    }
}