package mapeditor;

import tile.Tile;
import tile.TileManager;

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

    private void loadTileImages() {
        tileImages = new ArrayList<>();
        main.GamePanel tempGP = new main.GamePanel(null);
        TileManager tm = new TileManager(tempGP);
        for (Tile tile : tm.tile) {
            if (tile != null) {
                tileImages.add(tile.image);
            } else {
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
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createNewMap() {
        this.mapData = new int[MAP_COLS][MAP_ROWS];
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

    public int getSelectedTile() { return selectedTile; }
    public void setSelectedTile(int selectedTile) { this.selectedTile = selectedTile; }
    public int[][] getMapData() { return mapData; }
    public void setMapData(int col, int row, int tileId) { this.mapData[col][row] = tileId; }
    public int getMapCols() { return MAP_COLS; }
    public int getMapRows() { return MAP_ROWS; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MapEditor::new);
    }
}