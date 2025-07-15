package object;

import entity.Entity;
import main.GamePanel;
import entity.Player;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Objects;

public class OBJ_Chest extends Entity {
    private boolean opened = false;
    public OBJ_Chest(GamePanel gp) {
        super(gp);
        name = "chest";
        try {
            image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("res/objects/chest.png")));
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

    @Override
    public void interact(Player player) {
        if (!opened) {
            java.util.Random rand = new java.util.Random();
            int dropType = rand.nextInt(4); // 0:血药 1:金币 2:盾牌 3:剑
            String dropMsg = "";
            switch (dropType) {
                case 0: // 血药
                    int healAmount = Math.max(1, (int)Math.ceil(player.maxLife * 0.2));
                    java.awt.image.BufferedImage potionItemImg = null;
                    try {
                        potionItemImg = javax.imageio.ImageIO.read(
                            java.util.Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("res/objects/healingPotion_item.png")));
                    } catch (Exception e) { e.printStackTrace(); }
                    object.OBJ_HealingPotion potion = new object.OBJ_HealingPotion(player.gp, healAmount);
                    entity.Item itemPotion = new entity.Item(potionItemImg, "治疗药水", "恢复20%最大生命值", entity.Item.ItemType.POTION);
                    player.inventory.add(itemPotion);
                    dropMsg = "宝箱掉落：治疗药水";
                    break;
                case 1: // 金币
                    int coins = rand.nextInt(5) + 1; // 1~5
                    player.coin += coins;
                    dropMsg = "宝箱掉落：" + coins + " 金币";
                    break;
                case 2: // 盾牌
                    object.OBJ_Shield shield = new object.OBJ_Shield(player.gp);
                    entity.Item itemShield = new entity.Item(shield.image, "盾牌", "可以装备，提高防御力", entity.Item.ItemType.SHIELD);
                    player.inventory.add(itemShield);
                    dropMsg = "宝箱掉落：盾牌";
                    break;
                case 3: // 剑
                    object.OBJ_Sword sword = new object.OBJ_Sword(player.gp);
                    entity.Item itemSword = new entity.Item(sword.image, "剑", "可以装备，提高攻击力", entity.Item.ItemType.WEAPON);
                    player.inventory.add(itemSword);
                    dropMsg = "宝箱掉落：剑";
                    break;
            }
            player.gp.ui.addMessage(dropMsg);
            player.gp.ui.currentDialogue = dropMsg;
            player.gp.gameState = player.gp.dialogueState;
            player.gp.currentNpc = -1;
            // 让宝箱消失
            for (int i = 0; i < player.gp.obj.length; i++) {
                if (player.gp.obj[i] == this) {
                    player.gp.obj[i] = null;
                    break;
                }
            }
            opened = true;
        }
    }
}
