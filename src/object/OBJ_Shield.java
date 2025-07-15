package object;

import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;

import entity.Entity;
import main.GamePanel;

public class OBJ_Shield extends Entity {
    public OBJ_Shield(GamePanel gp) {
        super(gp);
        name = "Shield";
        try {
            image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("res/objects/shield.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        defenseValue = 1;
    }
    
    
}
