package entity;

import main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Entity {
    public GamePanel gp;
    public int worldX,worldY;
    public int speed;

    // 图片资源移至父类，供所有子类继承
    public BufferedImage down1,down2,up1,up2,left1,left2,right1,right2;
    public String direction;
    // 对象相关属性
    public BufferedImage image, image2, image3;
    public String name;
    public boolean collision;

    public int spriteCounter = 0;
    public int spriteNum = 1;
    public Rectangle solidArea = new Rectangle(0,0,48,48);
    public int solidAreaDefaultX,solidAreaDefaultY;
    public boolean collisionOn;
    public int actionLockCounter = 0;
    public String dialogues[] = new String[20];
    int dialogueIndex = 0;
    public int maxLife;
    public int life;
    public int level;
    public int strength;
    public int dexterity;
    public int defense;
    public int attack;
    public int exp;
    public int nextLevelExp;
    public int coin;
    public Entity currentShield;
    public int defenseValue;
    public Entity(GamePanel gp) {
        this.gp = gp;
        this.direction = "down"; // 默认朝向，防止空指针
        this.name = ""; // 默认名称，防止空指针
    }


    public void speak() {

        // --- 核心修复：调换了if语句中的判断顺序 ---
        // 1. 先检查索引是否越界 (dialogueIndex >= dialogues.length)
        // 2. 如果索引没有越界，再检查当前对话内容是否为null
        // 这样可以确保在访问 dialogues[dialogueIndex] 之前，dialogueIndex 一定是有效的。
        if (dialogueIndex >= dialogues.length || dialogues[dialogueIndex] == null) {
            dialogueIndex = 0; // 重置对话索引，为下一次互动做准备
            gp.gameState = gp.playState; // 结束对话，让玩家可以再次移动
        }
        // 如果对话还在进行中
        else {
            // 在UI上显示当前的对话内容
            gp.ui.currentDialogue = dialogues[dialogueIndex];
            // 将索引向前推进，准备下一句话
            dialogueIndex++;
        }
    }
    public void setAction(){
    }
    public void update(){
        setAction();
        collisionOn = false;
        gp.cChecker.checkTile(this);
        gp.cChecker.checkObject(this,false);
        gp.cChecker.checkPlayer(this);
        if(!collisionOn){
            switch (direction){
                case "up":worldY -= speed;break;
                case "down":worldY += speed;break;
                case "left":worldX -= speed;break;
                case "right":worldX += speed;break;
            }
        }

        spriteCounter++;
        if (spriteCounter > 15) {
            if (spriteNum == 1) {
                spriteNum = 2;
            } else if (spriteNum == 2) {
                spriteNum = 1;
            }
            spriteCounter = 0;
        }
    }
    public void draw(Graphics2D g2){
        BufferedImage image = null;

        // --- 核心修复：在这里添加对 "trader" 的判断 ---
        // 如果是对象（有name属性且不是已知的NPC类型），使用对象的绘制逻辑
        if (name != null && !name.equals("girl") && !name.equals("monster") && !name.equals("trader")) {
            drawObject(g2);
            return;
        }

        // 1. 根据方向和动画帧选择正确的图片
        switch (direction) {
            case "up":
                if (spriteNum == 1) { image = up1; }
                else if (spriteNum == 2) { image = up2; }
                break;
            case "down":
                if (spriteNum == 1) { image = down1; }
                else if (spriteNum == 2) { image = down2; }
                break;
            case "left":
                if (spriteNum == 1) { image = left1; }
                else if (spriteNum == 2) { image = left2; }
                break;
            case "right":
                if (spriteNum == 1) { image = right1; }
                else if (spriteNum == 2) { image = right2; }
                break;
        }

        // 2. 计算实体在屏幕上的坐标
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        // 3. 视野裁切
        // 只有当实体在屏幕的可视范围内时才进行绘制
        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX && // 实体右边 > 屏幕左边
                worldX - gp.tileSize < gp.player.worldX + gp.player.screenX && // 实体左边 < 屏幕右边
                worldY + gp.tileSize > gp.player.worldY - gp.player.screenY && // 实体下边 > 屏幕上边
                worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) { // 实体上边 < 屏幕下边

            g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
            // 仅在F3开发者模式下绘制碰撞箱
            if (gp.keyH.checkDrawTime) {
                int boxX = screenX + solidArea.x;
                int boxY = screenY + solidArea.y;
                g2.setColor(new Color(255,0,0,120));
                g2.fillRect(boxX, boxY, solidArea.width, solidArea.height);
                g2.setColor(Color.RED);
                g2.drawRect(boxX, boxY, solidArea.width, solidArea.height);
            }
            if (name.equals("monster") && life < maxLife) {
                // 绘制血条背景
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRect(screenX, screenY - 15, gp.tileSize, 8);

                // 计算血条长度
                int barWidth = gp.tileSize;
                int barHeight = 8;
                int lifeBarWidth = (int) ((double)life / maxLife * barWidth);

                // 绘制血条（红色）
                g2.setColor(new Color(255, 0, 0, 180));
                g2.fillRect(screenX, screenY - 15, lifeBarWidth, barHeight);

                // 绘制血条边框
                g2.setColor(Color.BLACK);
                g2.drawRect(screenX, screenY - 15, barWidth, barHeight);
            }
        }
    }

    // 对象绘制方法
    public void drawObject(Graphics2D g2) {
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;
        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
                worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
                worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
                worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {
            // 剑应该直接显示，不需要chestIsOpen条件
            g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
        }
    }

    // 文件: entity/Entity.java (更精确的 facePlayer 方法)

    public void facePlayer() {
        int playerX = gp.player.worldX;
        int playerY = gp.player.worldY;

        int dx = this.worldX - playerX; // NPC X - Player X
        int dy = this.worldY - playerY; // NPC Y - Player Y

        // 判断是水平方向更远还是垂直方向更远
        if (Math.abs(dx) > Math.abs(dy)) {
            // 水平方向是主要方向
            if (dx > 0) {
                direction = "left"; // NPC在玩家右边，所以朝左看
            } else {
                direction = "right"; // NPC在玩家左边，所以朝右看
            }
        } else {
            // 垂直方向是主要方向
            if (dy > 0) {
                direction = "up"; // NPC在玩家下边，所以朝上看
            } else {
                direction = "down"; // NPC在玩家上边，所以朝下看
            }
        }
    }

    // 多态交互方法，默认无操作
    public void interact(Player player) {}
}