package object;

import entity.Entity;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Objects;

public class OBJ_Door extends Entity {
    public OBJ_Door(GamePanel gp) {
        super(gp);
        name = "door";
        try {
            image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("res/objects/door.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        collision=true;
    // 设置门的碰撞区域
    solidArea.x = 6;
    solidArea.y = 18;
    solidArea.width = 18;
    solidArea.height = 32;
    solidAreaDefaultX = solidArea.x;
    solidAreaDefaultY = solidArea.y;
    }
}

