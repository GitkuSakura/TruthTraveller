package entity;

import main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Entity {
    public GamePanel gp;
    public int worldX,worldY;
    public int speed;

    public BufferedImage down1,down2,up1,up2,left1,left2,right1,right2;
    public String direction;
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
        this.direction = "down";
        this.name = "";
    }


    public void speak() {

        if (dialogueIndex >= dialogues.length || dialogues[dialogueIndex] == null) {
            dialogueIndex = 0;
            gp.gameState = gp.playState;
        }
        else {
            gp.ui.currentDialogue = dialogues[dialogueIndex];
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

        if (name != null && !name.equals("girl") && !name.equals("monster") && !name.equals("trader")) {
            drawObject(g2);
            return;
        }

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

        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX && // 实体右边 > 屏幕左边
                worldX - gp.tileSize < gp.player.worldX + gp.player.screenX && // 实体左边 < 屏幕右边
                worldY + gp.tileSize > gp.player.worldY - gp.player.screenY && // 实体下边 > 屏幕上边
                worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) { // 实体上边 < 屏幕下边

            g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
            if (gp.keyH.checkDrawTime) {
                int boxX = screenX + solidArea.x;
                int boxY = screenY + solidArea.y;
                g2.setColor(new Color(255,0,0,120));
                g2.fillRect(boxX, boxY, solidArea.width, solidArea.height);
                g2.setColor(Color.RED);
                g2.drawRect(boxX, boxY, solidArea.width, solidArea.height);
            }
            if (name.equals("monster") && life < maxLife) {
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRect(screenX, screenY - 15, gp.tileSize, 8);

                int barWidth = gp.tileSize;
                int barHeight = 8;
                int lifeBarWidth = (int) ((double)life / maxLife * barWidth);

                g2.setColor(new Color(255, 0, 0, 180));
                g2.fillRect(screenX, screenY - 15, lifeBarWidth, barHeight);

                g2.setColor(Color.BLACK);
                g2.drawRect(screenX, screenY - 15, barWidth, barHeight);
            }
        }
    }

    public void drawObject(Graphics2D g2) {
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;
        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
                worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
                worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
                worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {
            g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
        }
    }

    public void facePlayer() {
        int playerX = gp.player.worldX;
        int playerY = gp.player.worldY;

        int dx = this.worldX - playerX; // NPC X - Player X
        int dy = this.worldY - playerY; // NPC Y - Player Y

        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx > 0) {
                direction = "left";
            } else {
                direction = "right";
            }
        } else {
            if (dy > 0) {
                direction = "up";
            } else {
                direction = "down";
            }
        }
    }

    public void interact(Player player) {}
}