package tile;

import main.GamePanel;
import main.UtilityTool;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class TileManager {
    private static final int TILE_LAWN = 0;
    private static final int TILE_WATER = 1;
    private static final int TILE_WALL = 2;
    private static final int TILE_EARTH = 3;
    private static final int TILE_TREE = 4;
    private static final int TILE_SAND = 5;
    private static final int TILE_WATER_UP = 6;
    private static final int TILE_WATER_DOWN = 7;
    private static final int TILE_WATER_LEFT = 8;
    private static final int TILE_WATER_RIGHT = 9;
    private static final int TILE_WATER_LU = 10;
    private static final int TILE_WATER_LD = 11;
    private static final int TILE_WATER_RU = 12;
    private static final int TILE_WATER_RD = 13;
    private static final int TILE_GRASS = 14;
    private static final int TILE_c1 = 15;
    private static final int TILE_c2 = 16;
    private static final int TILE_c3 = 17;
    private static final int TILE_c4 = 18;
    private static final int TILE_HOUSE = 19; // 新增house贴图编号


    GamePanel gp;
    public Tile[] tile;
    public int[][] mapTileNum;
    
    // 性能优化：缓存计算结果
    private int lastPlayerWorldX = -1;
    private int lastPlayerWorldY = -1;
    private int cachedWorldColStart, cachedWorldColEnd, cachedWorldRowStart, cachedWorldRowEnd;
    private boolean cacheValid = false;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        tile = new Tile[50];
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];

        loadTileImages();
        loadMap("/res/maps/map01.txt");
    }

    private void loadTileImages() {
        try {
            tile[TILE_LAWN] = loadTile("/res/tiles/lawn.png");
            tile[TILE_GRASS] = loadTile("/res/tiles/grass.png");
            tile[TILE_WATER] = loadTile("/res/tiles/water.png");
            tile[TILE_WATER].collision = true;
            tile[TILE_WATER_UP] = loadTile("/res/tiles/water_up.png");
            tile[TILE_WATER_UP].collision = true;
            tile[TILE_WATER_DOWN] = loadTile("/res/tiles/water_down.png");
            tile[TILE_WATER_DOWN].collision = true;
            tile[TILE_WATER_LEFT] = loadTile("/res/tiles/water_side.png");
            tile[TILE_WATER_LEFT].collision = true;

            // Flip image for TILE_WATER_RIGHT
            tile[TILE_WATER_RIGHT] = new Tile();
            tile[TILE_WATER_RIGHT].image = flipImageHorizontally(tile[TILE_WATER_LEFT].image);
            tile[TILE_WATER_RIGHT].collision = true;

            tile[TILE_WATER_LU] = loadTile("/res/tiles/side_up.png");
            tile[TILE_WATER_LU].collision = true;
            tile[TILE_WATER_LD] = loadTile("/res/tiles/side_down.png");
            tile[TILE_WATER_LD].collision = true;

            // Flip images for TILE_WATER_RU and TILE_WATER_RD
            tile[TILE_WATER_RU] = new Tile();
            tile[TILE_WATER_RU].image = flipImageHorizontally(tile[TILE_WATER_LU].image);
            tile[TILE_WATER_RU].collision = true;

            tile[TILE_WATER_RD] = new Tile();
            tile[TILE_WATER_RD].image = flipImageHorizontally(tile[TILE_WATER_LD].image);
            tile[TILE_WATER_RD].collision = true;

            tile[TILE_WALL] = loadTile("/res/tiles/wall.png");
            tile[TILE_WALL].collision = true;
            tile[TILE_EARTH] = loadTile("/res/tiles/earth.png");
            tile[TILE_TREE] = loadTile("/res/tiles/tree.png");
            tile[TILE_TREE].collision = true;
            tile[TILE_SAND] = loadTile("/res/tiles/sand.png");


            tile[TILE_c1] = loadTile("/res/tiles/corner.png");
            tile[TILE_c1].collision = true;

            tile[TILE_c2] = new Tile();
            tile[TILE_c2].image = flipImageHorizontally(tile[TILE_c1].image);
            tile[TILE_c2].collision = true;
            tile[TILE_c3] = new Tile();
            tile[TILE_c3].image = flipImageVertically(tile[TILE_c1].image);
            tile[TILE_c3].collision = true;
            tile[TILE_c4] = new Tile();
            tile[TILE_c4].image = flipImageHorizontally((tile[TILE_c3].image));
            tile[TILE_c4].collision = true;
            // 注册house贴图
            tile[TILE_HOUSE] = loadTile("/res/tiles/house.png");
            tile[TILE_HOUSE].collision = true; // 如果房子不可穿越

        } catch (IOException e) {
            System.err.println("Error loading tile images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //    public void setup(int index,String imagePath,boolean collision){
//        UtilityTool uTool = new UtilityTool();
//        try{
//            tile[index] = new Tile();
//            tile[index].image = ImageIO.read(getClass())
//        }catch (IOException e){
//            e.printStackTrace();
//
//        }
//    }
    private Tile loadTile(String path) throws IOException {
        Tile tile = new Tile();
        tile.image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream(path)));
        return tile;
    }

    private BufferedImage flipImageVertically(BufferedImage image) {
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -image.getHeight());
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }

    public void loadMap(String filePath) {
        try (InputStream is = getClass().getResourceAsStream(filePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(is)))) {

            int col = 0;
            int row = 0;

            while (row < gp.maxWorldRow) {
                String line = br.readLine();
                if (line == null) break;

                String[] numbers = line.split(" ");
                while (col < gp.maxWorldCol) {
                    int num = Integer.parseInt(numbers[col]);
                    mapTileNum[col][row] = num;
                    col++;
                }

                if (col == gp.maxWorldCol) {
                    col = 0;
                    row++;
                }
            }
            
            // 性能优化：地图加载后使缓存失效
            cacheValid = false;
        } catch (Exception e) {
            System.err.println("Error loading map: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private BufferedImage flipImageHorizontally(BufferedImage image) {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-image.getWidth(), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }

    public void draw(Graphics2D g2) {
        // 性能优化：检查玩家位置是否改变，如果没改变则使用缓存
        if (lastPlayerWorldX != gp.player.worldX || lastPlayerWorldY != gp.player.worldY || !cacheValid) {
            calculateVisibleTiles();
            lastPlayerWorldX = gp.player.worldX;
            lastPlayerWorldY = gp.player.worldY;
            cacheValid = true;
        }
        
        // 使用缓存的可见瓦片范围
        for (int worldRow = cachedWorldRowStart; worldRow <= cachedWorldRowEnd; worldRow++) {
            for (int worldCol = cachedWorldColStart; worldCol <= cachedWorldColEnd; worldCol++) {
                int tileNum = mapTileNum[worldCol][worldRow];

                if (tileNum >= 0 && tileNum < tile.length && tile[tileNum] != null) {
                    int worldX = worldCol * gp.tileSize;
                    int worldY = worldRow * gp.tileSize;
                    int screenX = worldX - gp.player.worldX + gp.player.screenX;
                    int screenY = worldY - gp.player.worldY + gp.player.screenY;

                    g2.drawImage(tile[tileNum].image, screenX, screenY, gp.tileSize, gp.tileSize, null);
                }
            }
        }
    }
    
    private void calculateVisibleTiles() {
        int numTileCols = gp.screenWidth / gp.tileSize + 2;
        int numTileRows = gp.screenHeight / gp.tileSize + 2;

        cachedWorldColStart = Math.max(0, gp.player.worldX / gp.tileSize - numTileCols / 2);
        cachedWorldColEnd = Math.min(gp.maxWorldCol - 1, (gp.player.worldX + gp.screenWidth) / gp.tileSize + numTileCols / 2);
        cachedWorldRowStart = Math.max(0, gp.player.worldY / gp.tileSize - numTileRows / 2);
        cachedWorldRowEnd = Math.min(gp.maxWorldRow - 1, (gp.player.worldY + gp.screenHeight) / gp.tileSize + numTileRows / 2);
    }
}