// 文件: mapeditor/MapPanel.java
package mapeditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class MapPanel extends JPanel {
    private final MapEditor editor;
    private final int TILE_SIZE = 32; // Editor can have a different tile size for better viewing
    private final ArrayList<BufferedImage> tileImages;

    public MapPanel(MapEditor editor, ArrayList<BufferedImage> tileImages) {
        this.editor = editor;
        this.tileImages = tileImages;
        setPreferredSize(new Dimension(editor.getMapCols() * TILE_SIZE, editor.getMapRows() * TILE_SIZE));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // 左键绘制瓦片
                    paintTile(e);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    // 右键显示坐标
                    showTileCoordinates(e);
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                paintTile(e);
            }
        });
    }

    private void paintTile(MouseEvent e) {
        int col = e.getX() / TILE_SIZE;
        int row = e.getY() / TILE_SIZE;

        if (col >= 0 && col < editor.getMapCols() && row >= 0 && row < editor.getMapRows()) {
            if (editor.getSelectedTile() != -1) {
                editor.setMapData(col, row, editor.getSelectedTile());
                repaint();
            }
        }
    }
    
    private void showTileCoordinates(MouseEvent e) {
        int col = e.getX() / TILE_SIZE;
        int row = e.getY() / TILE_SIZE;
        
        if (col >= 0 && col < editor.getMapCols() && row >= 0 && row < editor.getMapRows()) {
            System.out.println("Tile coordinates: (" + col + ", " + row + ")");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int[][] mapData = editor.getMapData();

        for (int row = 0; row < editor.getMapRows(); row++) {
            for (int col = 0; col < editor.getMapCols(); col++) {
                int tileId = mapData[col][row];
                if (tileId >= 0 && tileId < tileImages.size() && tileImages.get(tileId) != null) {
                    g.drawImage(tileImages.get(tileId), col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                }
                // Draw grid
                g.setColor(Color.DARK_GRAY);
                g.drawRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }
}