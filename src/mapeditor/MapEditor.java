// 文件: mapeditor/MapEditor.java
package mapeditor;

import tile.Tile; // 需要导入你游戏中的Tile类
import tile.TileManager; // 导入游戏中的TileManager以加载图片

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class MapEditor extends JFrame {
    private final int MAP_COLS = 50;
    private final int MAP_ROWS = 50;

    private int[][] mapData;
    private int selectedTile = 0;

    private MapPanel mapPanel;
    private ArrayList<BufferedImage> tileImages;

    public MapEditor() {
        super("MikuSakura's Map Editor");
        this.mapData = new int[MAP_COLS][MAP_ROWS];

        loadTileImages();

        initUI();
    }

    // 这个方法模仿你的TileManager来加载所有瓦片图片
    private void loadTileImages() {
        tileImages = new ArrayList<>();
        // 我们需要一个临时的GamePanel实例来创建TileManager
        main.GamePanel tempGP = new main.GamePanel(null); // 传递null
        TileManager tm = new TileManager(tempGP);
        for (Tile tile : tm.tile) {
            if (tile != null) {
                tileImages.add(tile.image);
            } else {
                // 添加一个空的占位符，以保持索引正确
                tileImages.add(null);
            }
        }
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        mapPanel = new MapPanel(this, tileImages);
        JScrollPane scrollPane = new JScrollPane(mapPanel);
        add(scrollPane, BorderLayout.CENTER);

        TilePalette tilePalette = new TilePalette(this, tileImages);
        add(tilePalette, BorderLayout.EAST);

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem newItem = new JMenuItem("New");
        newItem.addActionListener(e -> createNewMap());

        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(e -> openMap());

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> saveMap());

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        pack();
        setLocationRelativeTo(null); // Center window
        setVisible(true);
    }

    private void createNewMap() {
        this.mapData = new int[MAP_COLS][MAP_ROWS]; // 全部填充为0
        mapPanel.repaint();
    }

    private void saveMap() {
        JFileChooser fileChooser = new JFileChooser("./res/maps");
        fileChooser.setDialogTitle("Save Map");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().endsWith(".txt")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".txt");
            }
            try (PrintWriter writer = new PrintWriter(fileToSave)) {
                for (int row = 0; row < MAP_ROWS; row++) {
                    for (int col = 0; col < MAP_COLS; col++) {
                        writer.print(mapData[col][row] + " ");
                    }
                    writer.println();
                }
                JOptionPane.showMessageDialog(this, "Map saved successfully!");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving map!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openMap() {
        JFileChooser fileChooser = new JFileChooser("./res/maps");
        fileChooser.setDialogTitle("Open Map");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            try (Scanner scanner = new Scanner(fileToOpen)) {
                for (int row = 0; row < MAP_ROWS; row++) {
                    for (int col = 0; col < MAP_COLS; col++) {
                        if (scanner.hasNextInt()) {
                            mapData[col][row] = scanner.nextInt();
                        }
                    }
                }
                mapPanel.repaint();
                JOptionPane.showMessageDialog(this, "Map loaded successfully!");
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading map!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Getter and Setter methods for child components to use
    public int getSelectedTile() { return selectedTile; }
    public void setSelectedTile(int selectedTile) { this.selectedTile = selectedTile; }
    public int[][] getMapData() { return mapData; }
    public void setMapData(int col, int row, int tileId) { this.mapData[col][row] = tileId; }
    public int getMapCols() { return MAP_COLS; }
    public int getMapRows() { return MAP_ROWS; }

    public static void main(String[] args) {
        // Run the editor
        SwingUtilities.invokeLater(MapEditor::new);
    }
}