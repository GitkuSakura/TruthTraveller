package object;

import entity.Entity;
import main.GamePanel;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Objects;

// 最佳实践建议：未来可以将这个类改名为 OBJ_TeleportAnchor 以提高代码可读性
public class OBJ_House extends Entity {
    // 传送目标属性
    public String targetMap = null;
    public int targetX = 0;
    public int targetY = 0;

    public OBJ_House(GamePanel gp) {
        super(gp);

        // 我们给它一个内部名字，方便识别，但它不再驱动逻辑
        name = "house"; 

        // <<< 核心修改 2：取消碰撞！ >>>
        // 如果它有碰撞，玩家就无法踩上去触发传送事件了。
        collision = false; 

        try {
            // 图片保持不变
            image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/res/objects/house.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // <<< 核心修改 3：移除实体碰撞区 >>>
        // 作为一个纯视觉对象，它不需要实体碰撞区来阻挡玩家
        // 我们可以将 solidArea 设置为 null 或一个 0x0 的矩形
        solidArea.x = 0;
        solidArea.y = 0;
        solidArea.width = 0;
        solidArea.height = 0;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }
}