package entity;

import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class NPC_Girl extends Entity {

    /**
     * 主构造函数，用于 MapManager 创建 NPC。
     * 它接收一个对话数组。
     */
    public NPC_Girl(GamePanel gp, String[] dialogues) {
        super(gp);
        
        // 1. 设置特定的对话
        this.dialogues = dialogues;
        
        // 2. 执行所有通用的初始化
        initialize();
    }
    
    /**
     * 默认构造函数，用于旧的 AssetSetter 或快速测试。
     * 它会使用默认的对话。
     */
    public NPC_Girl(GamePanel gp) {
        super(gp);
        
        // 1. 设置默认的对话
        setDialogue();
        
        // 2. 执行所有通用的初始化
        initialize();
    }
    
    /**
     * 包含所有通用初始化逻辑的私有方法。
     * 这样可以避免代码重复，并确保每个NPC都被正确设置。
     */
    private void initialize() {
        direction = "down";
        speed = 1;
        name = "girl"; // 关键：设置名字
        
        // 设置碰撞箱
        solidArea = new Rectangle();
        solidArea.x = 12;
        solidArea.y = 24;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
        solidArea.width = 24;
        solidArea.height = 24;
        
        // 关键：加载图片资源
        getGirlImage();
    }

    public void getGirlImage() {
        try {
            // 使用 ResourceLoader 异步加载可以提升性能，但为保持简单，这里用回同步加载
            down1 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/res/npc/girl.png")));
            down2 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/res/npc/girl.png")));
            up1 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/res/npc/girl_back.png")));
            up2 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/res/npc/girl_back.png")));
            left1 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/res/npc/girl.png")));
            left2 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/res/npc/girl.png")));

            right1 = left1;
            right2 = left2;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 设置默认对话内容
    public void setDialogue(){
        dialogues[0] = "你好，我是默认的女孩NPC。";
        dialogues[1] = "祝你游戏愉快！";
    }

    @Override
    public void setAction(){
        actionLockCounter++;
        if(actionLockCounter == 120){
            Random random = new Random();
            int i = random.nextInt(100)+1;
            if (i<=25) direction = "up";
            if (i>25 && i<=50) direction = "down";
            if (i>50 && i<75) direction="left";
            if (i>75 ) direction="right";
            actionLockCounter = 0;
        }
    }

    @Override
    public void update() {
        // 让NPC在玩家靠近时面向玩家，而不是随机走动
        int playerTileX = gp.player.worldX / gp.tileSize;
        int playerTileY = gp.player.worldY / gp.tileSize;
        int npcTileX = this.worldX / gp.tileSize;
        int npcTileY = this.worldY / gp.tileSize;

        // 计算曼哈顿距离
        if (Math.abs(playerTileX - npcTileX) + Math.abs(playerTileY - npcTileY) <= 2) {
            facePlayer();
        } else {
             // 只有当玩家离得远时才随机移动
            setAction();
            super.update(); // 调用父类的移动逻辑
        }
    }

    @Override
    public void speak(){
        // 先让NPC面向玩家
        facePlayer();
        // 然后调用父类的通用对话逻辑
        super.speak();
        
        // (此处的特殊逻辑可以保留)
        if (gp.gameState == gp.playState) {
            if ("神秘森林".equals(gp.mapManager.getCurrentMap().mapName) && !hasMonsters()) {
                gp.ui.addMessage("你清除了所有威胁，女孩带你前往了宝藏之地...");
                gp.mapManager.switchToMap("map03");
            }
        }
    }
    
    private boolean hasMonsters() {
        for (Entity monster : gp.monster) {
            if (monster != null && monster.life > 0) {
                return true;
            }
        }
        return false;
    }
}