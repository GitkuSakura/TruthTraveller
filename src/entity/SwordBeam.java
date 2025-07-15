package entity;

import main.GamePanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Objects;

public class SwordBeam extends Entity {
    
    private int maxDistance; // 最大飞行距离
    private int currentDistance; // 当前飞行距离
    public int damage; // 伤害值
    private boolean active; // 是否激活
    private int startX, startY; // 起始位置
    
    public SwordBeam(GamePanel gp) {
        super(gp);
        
        name = "swordBeam";
        speed = 4; // 剑气速度
        maxDistance = gp.tileSize * 4; // 最大4格距离
        damage = 0; // 伤害将在发射时根据玩家攻击力计算
        active = false;
        
        // 设置碰撞箱
        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 8;
        solidArea.width = 32;
        solidArea.height = 32;
        
        getSwordBeamImage();
    }
    
    public void getSwordBeamImage() {
        try {
            // 加载剑气图片（月牙形）
            BufferedImage baseImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/res/objects/sword_beam.png")));
            
            // 为不同方向创建旋转的剑气图片
            down1 = baseImage; // 向下（原始方向）
            up1 = rotateImage(baseImage, 180); // 向上（旋转180度）
            left1 = rotateImage(baseImage, 90); // 向左（旋转90度）
            right1 = rotateImage(baseImage, -90); // 向右（旋转-90度）
        } catch (IOException e) {
            e.printStackTrace();
            // 如果图片不存在，创建一个简单的月牙形
            createDefaultSwordBeam();
        }
    }
    
    // 旋转图片的方法
    private BufferedImage rotateImage(BufferedImage image, double degrees) {
        double rads = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));
        
        int w = image.getWidth();
        int h = image.getHeight();
        
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);
        
        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        
        g2d.translate((newWidth - w) / 2, (newHeight - h) / 2);
        g2d.rotate(rads, w / 2, h / 2);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        
        return rotated;
    }
    
    // 创建默认的月牙形剑气图片
    private void createDefaultSwordBeam() {
        BufferedImage img = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        
        // 设置抗锯齿
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制月牙形剑气
        g2.setColor(new Color(255, 255, 255, 200)); // 半透明白色
        g2.fillOval(8, 8, 32, 32);
        g2.setColor(new Color(200, 200, 255, 150)); // 半透明蓝色
        g2.fillOval(12, 12, 24, 24);
        g2.setColor(new Color(150, 150, 255, 100)); // 更透明的蓝色
        g2.fillOval(16, 16, 16, 16);
        
        g2.dispose();
        
        // 为不同方向创建旋转的剑气图片
        down1 = img; // 向下（原始方向）
        up1 = rotateImage(img, 180); // 向上（旋转180度）
        left1 = rotateImage(img, 90); // 向左（旋转90度）
        right1 = rotateImage(img, -90); // 向右（旋转-90度）
    }
    
    // 发射剑气
    public void fire(int startX, int startY, String direction) {
        this.startX = startX;
        this.startY = startY;
        this.worldX = startX;
        this.worldY = startY;
        this.direction = direction;
        this.currentDistance = 0;
        this.active = true;
        
        // 根据玩家攻击力设置剑气伤害
        this.damage = gp.player.attack;
    }
    
    @Override
    public void update() {
        if (!active) return;
        
        // 移动剑气
        switch (direction) {
            case "up":
                worldY -= speed;
                currentDistance += speed;
                break;
            case "down":
                worldY += speed;
                currentDistance += speed;
                break;
            case "left":
                worldX -= speed;
                currentDistance += speed;
                break;
            case "right":
                worldX += speed;
                currentDistance += speed;
                break;
        }
        
        // 检查是否达到最大距离
        if (currentDistance >= maxDistance) {
            deactivate();
            return;
        }
        
        // 检查与怪物的碰撞
        checkMonsterCollision();
        
        // 检查与墙壁的碰撞
        checkWallCollision();
    }
    
    // 检查与怪物的碰撞
    private void checkMonsterCollision() {
        for (Entity monster : gp.monster) {
            if (monster != null && monster.life > 0) {
                // 获取剑气的碰撞箱
                Rectangle beamSolidArea = new Rectangle(
                    worldX + solidArea.x, 
                    worldY + solidArea.y, 
                    solidArea.width, 
                    solidArea.height
                );
                
                // 获取怪物的碰撞箱
                Rectangle monsterSolidArea = new Rectangle(
                    monster.worldX + monster.solidArea.x, 
                    monster.worldY + monster.solidArea.y, 
                    monster.solidArea.width, 
                    monster.solidArea.height
                );
                
                // 检查碰撞
                if (beamSolidArea.intersects(monsterSolidArea)) {
                    // 对怪物造成伤害
                    if (monster instanceof entity.Monster) {
                        ((entity.Monster)monster).takeDamage(damage);
                    }
                    
                    // 播放怪物受伤音效
                    gp.playSE(7); // 怪物受伤音效
                    
                    // 触发击退效果
                    if (monster instanceof Monster) {
                        ((Monster) monster).takeKnockback(direction);
                    }
                    
                    // 剑气消失
                    deactivate();
                    return;
                }
            }
        }
    }
    
    // 检查与墙壁的碰撞
    private void checkWallCollision() {
        collisionOn = false;
        gp.cChecker.checkTile(this);
        
        if (collisionOn) {
            deactivate();
        }
    }
    
    // 停用剑气
    public void deactivate() {
        active = false;
    }
    
    // 检查是否激活
    public boolean isActive() {
        return active;
    }
    
    @Override
    public void draw(Graphics2D g2) {
        if (!active) return;
        
        BufferedImage image = null;
        
        switch (direction) {
            case "up":
                image = up1;
                break;
            case "down":
                image = down1;
                break;
            case "left":
                image = left1;
                break;
            case "right":
                image = right1;
                break;
        }
        
        if (image != null) {
            // 计算屏幕坐标
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;
            
            // 绘制剑气
            g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
        }
    }
} 