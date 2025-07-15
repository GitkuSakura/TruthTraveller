package object;

import entity.Entity;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Objects;

public class OBJ_House extends Entity {
    public String targetMap = null;
    public int targetX = 0;
    public int targetY = 0;

    public OBJ_House(GamePanel gp) {
        super(gp);

        name = "house";
        collision = false;

        try {
            image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/res/objects/house.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        solidArea.x = 0;
        solidArea.y = 0;
        solidArea.width = 0;
        solidArea.height = 0;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }
}