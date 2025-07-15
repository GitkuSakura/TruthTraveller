package object;

import entity.Entity;
import main.GamePanel;

import java.awt.*;

public class OBJ_InstructionBoard extends Entity {

    public OBJ_InstructionBoard(GamePanel gp) {
        super(gp);
        name = "InstructionBoard";
        collision = true;
    }

    /**
     * 重写父类的draw方法，来实现绘制文字而不是图片
     */
    @Override
    public void draw(Graphics2D g2) {
        // --- 计算物体在屏幕上的位置 ---
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        // --- 修正视野裁切 ---

        int objectOnScreenWidth = gp.tileSize * 3;
        int objectOnScreenHeight = gp.tileSize * 4; // 修正为4倍tileSize，保证所有内容都在区域内
        // 用矩形相交判定，保证只要有一部分在屏幕内就显示
        Rectangle boardRect = new Rectangle(worldX, worldY, objectOnScreenWidth, objectOnScreenHeight);
        Rectangle screenRect = new Rectangle(
            gp.player.worldX - gp.player.screenX,
            gp.player.worldY - gp.player.screenY,
            gp.screenWidth,
            gp.screenHeight
        );
        if (boardRect.intersects(screenRect)) {
            // 先绘制半透明背景，防止重影
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(screenX, screenY, objectOnScreenWidth, objectOnScreenHeight, 20, 20);

            g2.setFont(gp.handwrittenFont.deriveFont(Font.BOLD, 30f));
            g2.setColor(Color.WHITE);

            String text1 = "操作方法:";
            String text2 = "移动: WASD";
            String text3 = "互动: F";
            String text4 = "角色信息: Q"; // 新增Q键说明

            int textY = screenY + gp.tileSize / 3;
            g2.drawString(text1, screenX + 5, textY);
            g2.drawString(text2, screenX + 5, textY + 40);
            g2.drawString(text3, screenX + 5, textY + 80);
            g2.drawString(text4, screenX + 5, textY + 120);
        }
    }
}