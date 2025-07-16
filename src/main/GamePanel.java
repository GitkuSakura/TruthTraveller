package main;

import entity.Entity;
import entity.Player;
import entity.Particle;

import tile.TileManager;

import javax.swing.*;
import java.awt.*;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.awt.image.BufferedImage;
import java.util.concurrent.Future;
import java.util.Objects;
import java.util.stream.Collectors;

public class GamePanel extends JPanel implements Runnable {
    public JFrame window;
    public Font handwrittenFont;
    public int currentNpc;
    final int originalTileSize = 16;
    final int scale = 3;
    public int tileSize = originalTileSize * scale;
    public int maxScreenCol = 16;
    public int maxScreenRow = 12;
    public int screenWidth = tileSize * maxScreenCol;
    public int screenHeight = tileSize * maxScreenRow;
    public final int maxWorldCol = 50;
    public final int maxWorldRow = 50;
    public final int worldWidth = tileSize * maxScreenCol;
    public final int worldHeight = tileSize * maxScreenRow;
    int FPS = 60;
    public boolean isTesting;
    public TileManager TileM = new TileManager(this);

    public KeyHandler keyH = new KeyHandler(this);
    Sound music = new Sound();
    Sound se = new Sound();
    public CollisionChecker cChecker = new CollisionChecker(this);
    public AssetSetter aSetter = new AssetSetter(this);
    public MapManager mapManager = new MapManager(this);
    public DynamicSpawner spawner = new DynamicSpawner(this);
    public UI ui;
    Thread gameThread;

    // 昼夜系统变量
    public int dayState;
    public final int dawn = 0;
    public final int morning = 1;
    public final int noon = 2;
    public final int afternoon = 3;
    public final int dusk = 4;
    public final int night = 5;
    private int dayCounter;
    private final int periodDuration = 1800; // 30 seconds at 60 FPS
    public float filterAlpha = 0f;

    public Player player = new Player(this, keyH);
    public Entity[] obj = new Entity[10];
    public Entity npc[]= new Entity[10];
    public Entity monster[]= new Entity[20];
    public ArrayList<Particle> particleList = new ArrayList<>();
    public int gameState;
    public final int loadingState = -1;
    public final int titleState = 0;
    public final int playState = 1;
    public final int pauseState = 2;
    public final int dialogueState = 3;
    public final int deathState = 4;
    public final int inventoryState = 5;
    public final int tradeState = 6;
    public boolean showCharacterWindow = false;
    private BufferedImage screenBuffer;
    private Future<BufferedImage> titlePlayerImageFuture;
    public SpatialGrid spatialGrid;

    public GamePanel(JFrame window) {
        this.window = window;
        loadCustomFont();
        this.ui = new UI(this);
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.spatialGrid = new SpatialGrid(worldWidth, worldHeight, tileSize * 2);
        this.setBackground(Color.DARK_GRAY);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        screenBuffer = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
    }

    public GamePanel() {
        this.tileSize = 16 * 3;
        this.maxScreenCol = 16;
        this.maxScreenRow = 12;
        this.screenWidth = tileSize * maxScreenCol;
        this.screenHeight = tileSize * maxScreenRow;
        this.TileM = new TileManager(this);
    }

    public void setupGame(){
        stop();
        player.setDefaultValues();
        dayState = noon;
        dayCounter = 0;
        filterAlpha = 0f;
        mapManager.switchToMap("map01");
    }

    public void loadMap(String mapFilePath, int playerStartX, int playerStartY,
                        java.util.List<NpcPlacement> npcPlacements,
                        java.util.List<MonsterPlacement> monsterPlacements) {
        TileM.loadMap(mapFilePath);
        player.worldX = playerStartX;
        player.worldY = playerStartY;
        player.direction = "down";

        for (int i = 0; i < npc.length; i++) npc[i] = null;
        for (int i = 0; i < monster.length; i++) monster[i] = null;

        aSetter.setNpcs(npcPlacements);
        aSetter.setMonsters(monsterPlacements);
    }

    public void saveGame() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("savegame.dat"))) {
            StringBuilder saveData = new StringBuilder();

            saveData.append("Player:")
                    .append(player.worldX).append(",").append(player.worldY).append(",").append(player.life).append(",")
                    .append(player.maxLife).append(",").append(player.level).append(",").append(player.strength).append(",")
                    .append(player.dexterity).append(",").append(player.exp).append(",").append(player.nextLevelExp).append(",")
                    .append(player.coin);
            writer.write(SaveEncryptor.encrypt(saveData.toString()));
            writer.newLine();

            saveData.setLength(0);
            saveData.append("Map:").append(mapManager.getCurrentMapKey());
            writer.write(SaveEncryptor.encrypt(saveData.toString()));
            writer.newLine();

            saveData.setLength(0);
            String inventoryData = player.inventory.stream()
                    .map(item -> item.type + "§" + item.name + "§" + item.description)
                    .collect(Collectors.joining(";"));

            saveData.append("Inventory:").append(inventoryData);
            writer.write(SaveEncryptor.encrypt(saveData.toString()));
            writer.newLine();

            System.out.println("游戏已加密保存。");
            ui.addMessage("游戏已保存！");

        } catch (IOException e) {
            System.err.println("保存游戏失败: " + e.getMessage());
            ui.addMessage("保存失败！");
        }
    }

    public void loadGame() {
        File saveFile = new File("savegame.dat");
        if (!saveFile.exists()) {
            System.out.println("没有找到存档文件。");
            ui.addMessage("没有存档！");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
            String encryptedLine;
            while ((encryptedLine = reader.readLine()) != null) {
                String line = SaveEncryptor.decrypt(encryptedLine);
                if (line == null) continue;

                if (line.startsWith("Player:")) {
                    String[] data = line.substring(7).split(",");
                    player.worldX = Integer.parseInt(data[0]);
                    player.worldY = Integer.parseInt(data[1]);
                    player.life = Integer.parseInt(data[2]);
                    player.maxLife = Integer.parseInt(data[3]);
                    player.level = Integer.parseInt(data[4]);
                    player.strength = Integer.parseInt(data[5]);
                    player.dexterity = Integer.parseInt(data[6]);
                    player.exp = Integer.parseInt(data[7]);
                    player.nextLevelExp = Integer.parseInt(data[8]);
                    player.coin = Integer.parseInt(data[9]);
                    player.updateStats();
                } else if (line.startsWith("Map:")) {
                    String mapKeyOrName = line.substring(4);
                    if (mapManager.getAvailableMaps().contains(mapKeyOrName)) {
                        mapManager.switchToMap(mapKeyOrName);
                    } else {
                        String foundKey = null;
                        for (String key : mapManager.getAvailableMaps()) {
                            if (mapManager.maps.get(key).mapName.equals(mapKeyOrName)) {
                                foundKey = key;
                                break;
                            }
                        }
                        if (foundKey != null) {
                            mapManager.switchToMap(foundKey);
                        } else {
                            System.err.println("地图不存在: " + mapKeyOrName);
                        }
                    }
                } else if (line.startsWith("Inventory:")) {
                    player.inventory.clear();
                    String allItemsData = line.substring(10);
                    if (!allItemsData.isEmpty()) {
                        String[] items = allItemsData.split(";");
                        for (String itemData : items) {
                            String[] parts = itemData.split("§");
                            if (parts.length == 3) {
                                entity.Item.ItemType type = entity.Item.ItemType.valueOf(parts[0]);
                                String name = parts[1];
                                String description = parts[2];
                                java.awt.image.BufferedImage image = null;
                                try {
                                    switch (type) {
                                        case WEAPON: image = javax.imageio.ImageIO.read(getClass().getClassLoader().getResourceAsStream("res/objects/sword.png")); break;
                                        case SHIELD: image = javax.imageio.ImageIO.read(getClass().getClassLoader().getResourceAsStream("res/objects/shield.png")); break;
                                        case POTION: image = javax.imageio.ImageIO.read(getClass().getClassLoader().getResourceAsStream("res/objects/healingPotion_item.png")); break;
                                        case KEY: image = javax.imageio.ImageIO.read(getClass().getClassLoader().getResourceAsStream("res/objects/key.png")); break;
                                        default: break;
                                    }
                                } catch (Exception e) { image = null; }
                                entity.Item newItem = new entity.Item(image, name, description, type);
                                player.inventory.add(newItem);
                            }
                        }
                    }
                    player.syncKeyCountWithInventory();
                }
            }
            dayState = noon;
            dayCounter = 0;
            filterAlpha = 0f;

            System.out.println("游戏已从存档加载。");
            ui.addMessage("游戏已加载！");
            playMusic(0);

        } catch (IOException | NumberFormatException e) {
            System.err.println("加载游戏失败: " + e.getMessage());
            ui.addMessage("加载失败！");
        }
    }

    public void startGameThread() {
        gameState = loadingState;
        titlePlayerImageFuture = main.ResourceLoader.loadImageAsync("/res/player/down1.png");
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCounter = 0;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
                drawCounter++;
            }

            if (delta < 1) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (timer >= 1000000000 && keyH.checkDrawTime){
                ui.showFPS("FPS"+drawCounter);
                drawCounter = 0;
                timer = 0;
            }
        }
    }

    public void update() {
        if (gameState == loadingState) {
            if (titlePlayerImageFuture != null && titlePlayerImageFuture.isDone()) {
                try {
                    player.down1 = titlePlayerImageFuture.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                gameState = titleState;
            }
            return;
        }

        int devSpeed = 10;
        int playerDefaultSpeed = player.speed;
        boolean devMode = keyH.checkDrawTime;
        if (devMode) {
            player.speed = devSpeed;
        }
        ui.updateMessages();
        ui.updateMessages();
        spatialGrid.clear();
        for (Entity m : monster) {
            if (m != null) spatialGrid.add(m);
        }
        for (Entity n : npc) {
            if (n != null) spatialGrid.add(n);
        }
        spatialGrid.add(player);
        if (gameState == playState) {
            player.update();
            for (Entity entity : npc) {
                if (entity != null) entity.update();
            }
            for (Entity entity : monster) {
                if (entity != null) entity.update();
            }
            for (int i = particleList.size() - 1; i >= 0; i--) {
                Particle p = particleList.get(i);
                p.update();
                if (!p.alive) particleList.remove(i);
            }
            spawner.update();
            updateDayNightCycle();
        }
        else if (gameState == pauseState) {
            if (keyH.upPressed) {
                ui.commandNum--;
                if (ui.commandNum < 0) ui.commandNum = 2;
                keyH.upPressed = false;
            }
            if (keyH.downPressed) {
                ui.commandNum++;
                if (ui.commandNum > 2) ui.commandNum = 0;
                keyH.downPressed = false;
            }
            if (keyH.enterPressed) {
                if (ui.commandNum == 0) gameState = playState;
                if (ui.commandNum == 1) saveGame();
                if (ui.commandNum == 2) {
                    stop();
                    gameState = titleState;
                    ui.commandNum = 0;
                }
                keyH.enterPressed = false;
            }
        }
        else if (gameState == dialogueState) {
            if (keyH.enterPressed) {
                if (!ui.isDialogueAnimationFinished()) {
                    ui.finishDialogueAnimation();
                } else {
                    if (currentNpc >= 0 && currentNpc < npc.length && npc[currentNpc] != null) {
                        npc[currentNpc].speak();
                    } else {
                        gameState = playState;
                    }
                }
                keyH.enterPressed = false;
            }
        }
        else if (gameState == deathState) {
            if (keyH.upPressed) {
                ui.commandNum--;
                if (ui.commandNum < 0) ui.commandNum = 1;
                keyH.upPressed = false;
            }
            if (keyH.downPressed) {
                ui.commandNum++;
                if (ui.commandNum > 1) ui.commandNum = 0;
                keyH.downPressed = false;
            }
            if (keyH.enterPressed) {
                if (ui.commandNum == 0) {
                    setupGame();
                    gameState = playState;
                    ui.commandNum = 0;
                    playMusic(0);
                }
                if (ui.commandNum == 1) {
                    loadGame();
                    gameState = playState;
                }
                keyH.enterPressed = false;
            }
        }
        else if (gameState == titleState) {
            if (keyH.upPressed) {
                ui.commandNum--;
                if (ui.commandNum < 0) ui.commandNum = 2;
                keyH.upPressed = false;
            }
            if (keyH.downPressed) {
                ui.commandNum++;
                if (ui.commandNum > 2) ui.commandNum = 0;
                keyH.downPressed = false;
            }
            if (keyH.enterPressed) {
                if (ui.commandNum == 0) {
                    setupGame();
                    gameState = playState;
                    playMusic(0);
                    ui.commandNum = 0;
                }
                if (ui.commandNum == 1) {
                    loadGame();
                    gameState = playState;
                }
                if (ui.commandNum == 2) {
                    System.exit(0);
                }
                keyH.enterPressed = false;
            }
        }
        else if (gameState == inventoryState) {
            int cols = 5, rows = 3, maxRow = rows - 1, maxCol = cols - 1;
            if (keyH.upPressed) { if (ui.inventoryRow > 0) ui.inventoryRow--; keyH.upPressed = false; }
            if (keyH.downPressed) { if (ui.inventoryRow < maxRow) ui.inventoryRow++; keyH.downPressed = false; }
            if (keyH.leftPressed) { if (ui.inventoryCol > 0) ui.inventoryCol--; keyH.leftPressed = false; }
            if (keyH.rightPressed) { if (ui.inventoryCol < maxCol) ui.inventoryCol++; keyH.rightPressed = false; }
            if (keyH.enterPressed) {
                int selectIdx = ui.inventoryRow * cols + ui.inventoryCol;
                if (selectIdx < player.inventory.size()) {
                    entity.Item item = player.inventory.get(selectIdx);
                    switch (item.type) {
                        case WEAPON:
                            player.hasSword = true; player.swordCount += 1; player.updateStats();
                            ui.addMessage("已装备武器：" + item.name); player.inventory.remove(selectIdx); playSE(2); break;
                        case SHIELD:
                            player.shieldCount += 1; player.updateStats();
                            ui.addMessage("已装备盾牌，防御+1"); player.inventory.remove(selectIdx); playSE(2); break;
                        case POTION:
                            int heal = Math.max(1, (int)Math.ceil(player.maxLife * 0.2));
                            player.life = Math.min(player.life + heal, player.maxLife);
                            ui.addMessage("使用药水，恢复" + heal + "点生命值"); player.inventory.remove(selectIdx); playSE(2); break;
                        case KEY: ui.addMessage("钥匙只能在遇到门时自动消耗"); break;
                        default: ui.addMessage("该物品无法使用"); break;
                    }
                } else { ui.addMessage("空格子，无物品"); }
                keyH.enterPressed = false;
            }
        } else if (gameState == tradeState) {
            if (keyH.upPressed) { ui.commandNum--; if (ui.commandNum < 0) ui.commandNum = 2; keyH.upPressed = false; playSE(1); }
            if (keyH.downPressed) { ui.commandNum++; if (ui.commandNum > 2) ui.commandNum = 0; keyH.downPressed = false; playSE(1); }
            if (keyH.enterPressed) {
                int price = 2;
                if (player.coin >= price) {
                    player.coin -= price;
                    entity.Item purchasedItem = null;
                    try {
                        switch (ui.commandNum) {
                            case 0: purchasedItem = new entity.Item(javax.imageio.ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/res/objects/shield.png"))), "Shield", "A sturdy shield.", entity.Item.ItemType.SHIELD); ui.addMessage("购买了 盾牌!"); break;
                            case 1: purchasedItem = new entity.Item(javax.imageio.ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/res/objects/healingPotion_item.png"))), "Healing Potion", "Heals some life.", entity.Item.ItemType.POTION); ui.addMessage("购买了 治疗药水!"); break;
                            case 2: purchasedItem = new entity.Item(javax.imageio.ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/res/objects/sword.png"))), "Sword", "A basic sword.", entity.Item.ItemType.WEAPON); ui.addMessage("购买了 剑!"); break;
                        }
                    } catch (IOException e) { e.printStackTrace(); }
                    if (purchasedItem != null) { player.inventory.add(purchasedItem); playSE(2); }
                } else { ui.addMessage("金币不足!"); }
                keyH.enterPressed = false;
            }
        }
        if (devMode) {
            player.speed = playerDefaultSpeed;
        }
    }

    public void updateDayNightCycle() {
        dayCounter++;
        if (dayCounter > periodDuration) {
            dayCounter = 0;
            dayState = (dayState + 1) % 6;
        }

        // --- 核心修改：调整后的亮度值数组 ---
        // 索引: 0-Dawn, 1-Morning, 2-Noon, 3-Afternoon, 4-Dusk, 5-Night
        // 值越大越暗。现在Dawn(0.9f)是最暗的。
        float[] targetAlphas = {0.9f, 0.4f, 0.0f, 0.2f, 0.6f, 0.8f};

        float currentTargetAlpha = targetAlphas[dayState];
        float nextTargetAlpha = targetAlphas[(dayState + 1) % 6];
        float progress = (float) dayCounter / periodDuration;
        filterAlpha = currentTargetAlpha + (nextTargetAlpha - currentTargetAlpha) * progress;
        if (filterAlpha < 0) filterAlpha = 0;
        if (filterAlpha > 1) filterAlpha = 1;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2dBuffer = screenBuffer.createGraphics();
        drawGame(g2dBuffer);
        g2dBuffer.dispose();
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        double scale = Math.min(panelWidth * 1.0 / screenWidth, panelHeight * 1.0 / screenHeight);
        int drawWidth = (int)(screenWidth * scale);
        int drawHeight = (int)(screenHeight * scale);
        int x = (panelWidth - drawWidth) / 2;
        int y = (panelHeight - drawHeight) / 2;
        g.drawImage(screenBuffer, x, y, drawWidth, drawHeight, null);
    }

    public void drawGame(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, screenWidth, screenHeight);
    
        if (gameState == titleState) {
            ui.draw(g2);
        } else {
            // --- 优化的绘制逻辑 ---
    
            // 1. 绘制地砖
            TileM.draw(g2);
    
            // 2. 创建一个列表，只存储需要Y轴排序的动态实体 (NPC, Monster, Player)
            ArrayList<Entity> characterList = new ArrayList<>();
            
            // 3. 绘制所有静态对象 (如宝箱、掉落物)，并收集动态角色
            for (Entity entity : obj) {
                if (entity != null && isObjectVisible(entity)) {
                    // 像 instructionBoard 这种需要特殊绘制逻辑的，可以单独处理或保持原样
                    if ("InstructionBoard".equals(entity.name)) {
                        entity.draw(g2);
                    } else {
                        // 普通对象直接添加，也参与排序，以处理其在角色前后的情况
                        characterList.add(entity);
                    }
                }
            }
            for (Entity entity : npc) {
                if (entity != null && isEntityVisible(entity)) {
                    characterList.add(entity);
                }
            }
            for (Entity entity : monster) {
                if (entity != null && isEntityVisible(entity)) {
                    characterList.add(entity);
                }
            }
            characterList.add(player);
    
            // 4. 只对这个精简后的列表进行排序
            characterList.sort(Comparator.comparingInt(e -> e.worldY));
    
            // 5. 按排序后的顺序绘制
            for (Entity entity : characterList) {
                entity.draw(g2);
            }
    
            // 6. 绘制最顶层的效果 (粒子、剑气等)，它们总是在最前面
            particleList.removeIf(p -> p.life <= 0);
            for (Particle p : particleList) {
                if (isEntityVisible(p)) p.draw(g2);
            }
            if (player.swordBeam != null && player.swordBeam.isActive() && isEntityVisible(player.swordBeam)) {
                player.swordBeam.draw(g2);
            }
    
            // 7. 绘制光照和UI
            if (filterAlpha > 0f) {
                drawLighting(g2);
            }
            ui.draw(g2);
        }
    }

    public void drawLighting(Graphics2D g2) {
        // 光照半径，保持较小的值
        float lightRadius = 150f;

        float[] fractions = {0f, 1f};
        Color[] colors = {
                new Color(0, 0, 0, 0f),
                new Color(0, 0, 0, filterAlpha)
        };

        java.awt.RadialGradientPaint rgp = new java.awt.RadialGradientPaint(
                screenWidth / 2f, screenHeight / 2f, lightRadius, fractions, colors
        );

        g2.setPaint(rgp);
        g2.fillRect(0, 0, screenWidth, screenHeight);
    }

    private boolean isObjectVisible(Entity obj) {
        int margin = tileSize * 2;
        return obj.worldX + tileSize > player.worldX - player.screenX - margin &&
                obj.worldX - tileSize < player.worldX + player.screenX + margin &&
                obj.worldY + tileSize > player.worldY - player.screenY - margin &&
                obj.worldY - tileSize < player.worldY + player.screenY + margin;
    }

    private boolean isEntityVisible(Entity entity) {
        int margin = tileSize * 2;
        return entity.worldX + tileSize > player.worldX - player.screenX - margin &&
                entity.worldX - tileSize < player.worldX + player.screenX + margin &&
                entity.worldY + tileSize > player.worldY - player.screenY - margin &&
                entity.worldY - tileSize < player.worldY + player.screenY + margin;
    }

    public void playMusic(int i){
        music.setFile(i);
        music.play();
        music.loop();
    }

    public void stop(){
        music.stop();
    }

    public void playSE(int i){
        se.setFile(i);
        se.play();
    }

    public void loadCustomFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/res/font/pfst.ttf");
            handwrittenFont = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            handwrittenFont = new Font("Arial", Font.PLAIN, 15);
        }
    }

    public void enterFullScreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            window.dispose();
            window.setUndecorated(true);
            gd.setFullScreenWindow(window);
            window.setVisible(true);
        } else {
            window.dispose();
            window.setUndecorated(true);
            window.setExtendedState(JFrame.MAXIMIZED_BOTH);
            window.setVisible(true);
        }
    }

    public void exitFullScreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        gd.setFullScreenWindow(null);
        window.dispose();
        window.setUndecorated(false);
        window.setExtendedState(JFrame.NORMAL);
        window.setSize(screenWidth, screenHeight);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}
