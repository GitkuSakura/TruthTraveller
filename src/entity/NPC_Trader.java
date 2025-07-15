package entity;

import main.GamePanel;
import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Objects;

public class NPC_Trader extends Entity {

    public NPC_Trader(GamePanel gp, String[] dialogues) {
        super(gp);
        this.dialogues = dialogues;
        initialize();
    }

    public NPC_Trader(GamePanel gp) {
        super(gp);
        initialize();
        setDialogue();
    }

    private void initialize() {
        direction = "down";
        speed = 0; // 商人通常站着不动
        name = "trader"; // 关键: 设置名字为 "trader"

        solidArea = new Rectangle();
        solidArea.x = 12;
        solidArea.y = 24;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
        solidArea.width = 24;
        solidArea.height = 24;

        getTraderImage();
    }

    public void getTraderImage() {
        try {
            // 按照要求，复用女孩的图片资源
            down1 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/res/npc/girl.png")));
            down2 = down1;
            up1 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/res/npc/girl_back.png")));
            up2 = up1;
            left1 = down1;
            left2 = down1;
            right1 = down1;
            right2 = down1;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDialogue(){
        dialogues[0] = "你好，旅行者。想看看我的商品吗？";
    }

    @Override
    public void speak() {
        // 首先，调用父类的speak方法来处理对话的正常进行
        super.speak();

        // 父类方法在对话结束时，会将游戏状态设置为playState。
        // 我们在这里拦截这个行为，并转而将状态切换到交易状态。
        if (gp.gameState == gp.playState) {
            gp.gameState = gp.tradeState;
            gp.ui.commandNum = 0; // 重置交易菜单的选项
        }
    }

    @Override
    public void update() {
        // 商人只在玩家靠近时面向玩家
        int playerTileX = gp.player.worldX / gp.tileSize;
        int playerTileY = gp.player.worldY / gp.tileSize;
        int npcTileX = this.worldX / gp.tileSize;
        int npcTileY = this.worldY / gp.tileSize;

        if (Math.abs(playerTileX - npcTileX) + Math.abs(playerTileY - npcTileY) <= 2) {
            facePlayer();
        }
    }
}