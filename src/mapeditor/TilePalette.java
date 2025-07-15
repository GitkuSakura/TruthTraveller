// 文件: mapeditor/TilePalette.java
package mapeditor;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class TilePalette extends JPanel {
    private final MapEditor editor;
    private final ArrayList<BufferedImage> tileImages;
    private final int TILE_SIZE = 48;

    public TilePalette(MapEditor editor, ArrayList<BufferedImage> tileImages) {
        this.editor = editor;
        this.tileImages = tileImages;
        setPreferredSize(new Dimension(TILE_SIZE * 3, 600));
        setBackground(Color.LIGHT_GRAY);

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                int index = evt.getY() / (TILE_SIZE + 2);
                if (index < tileImages.size()) {
                    editor.setSelectedTile(index);
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < tileImages.size(); i++) {
            if (tileImages.get(i) != null) {
                g.drawImage(tileImages.get(i), 5, i * (TILE_SIZE + 2), TILE_SIZE, TILE_SIZE, null);
                g.setColor(Color.BLACK);
                g.drawString(String.valueOf(i), TILE_SIZE + 10, i * (TILE_SIZE + 2) + TILE_SIZE / 2);
            }
        }

        // Highlight selected tile
        int selected = editor.getSelectedTile();
        if (selected != -1) {
            g.setColor(Color.RED);
            g.drawRect(2, selected * (TILE_SIZE + 2) - 1, TILE_SIZE + 5, TILE_SIZE + 2);
        }
    }
}