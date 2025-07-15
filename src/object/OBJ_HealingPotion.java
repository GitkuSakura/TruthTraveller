package object;

import entity.Entity;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Objects;

public class OBJ_HealingPotion extends Entity {
    public int healAmount; // 回复量
    
    public OBJ_HealingPotion(GamePanel gp) {
        this(gp, 2); // 默认回复2点生命值
    }
    
    public OBJ_HealingPotion(GamePanel gp, int healAmount) {
        super(gp);
        name = "healingPotion";
        this.healAmount = healAmount;
        try {
            image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("res/objects/healingPotion.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 设置药水的碰撞区域
        collision = true;
        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = 32;
        solidArea.height = 32;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }
} 