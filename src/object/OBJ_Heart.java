package object;

import entity.Entity;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Objects;

public class OBJ_Heart extends Entity {
    public OBJ_Heart(GamePanel gp) {
        super(gp);
        name = "heart";
        try {
            image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("res/objects/hf.png")));
            image2 = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("res/objects/hh.png")));
            image3 = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("res/objects/he.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        collision = true;
        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = 32;
        solidArea.height = 32;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }
}
