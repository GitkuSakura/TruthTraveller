package entity;

import main.GamePanel;
import main.KeyHandler;
import main.ResourceLoader;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;
import java.util.ArrayList;
import entity.Item;
import object.OBJ_Sword;
import object.OBJ_HealingPotion;
import object.OBJ_Key;
import object.OBJ_Shield;

public class Player extends Entity {
    KeyHandler keyH;
    public final int screenX;
    public final int screenY;
    public int hasKey = 0;
    public boolean chestIsOpen;
    public boolean bonusIsGot;
    
    // 伤害系统相关变量
    public boolean invincible = false; // 无敌状态
    public int invincibleCounter = 0; // 无敌时间计数器
    public final int invincibleTime = 60; // 无敌时间（60帧 = 1秒）
    
    // 剑和剑气系统
    public boolean hasSword = false; // 是否拥有剑
    public int swordCount = 0; // 剑的数量
    public SwordBeam swordBeam; // 剑气实例
    public int shieldCount = 0; // 盾牌数量
    public ArrayList<Item> inventory = new ArrayList<>();
    // 新增：异步图片加载Future
    private Future<BufferedImage> futureDown1, futureDown2, futureUp1, futureUp2, futureLeft1, futureLeft2, futureRight1, futureRight2;
    private static final BufferedImage PLACEHOLDER = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
    public Player(GamePanel gp, KeyHandler keyH) {
        super(gp);
        this.keyH = keyH;
        screenX = gp.screenWidth/2 - (gp.tileSize/2);
        screenY = gp.screenHeight/2 - (gp.tileSize/2);
        solidArea = new Rectangle();
        solidArea.x = 12;
        solidArea.y = 24;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
        solidArea.width = 24;
        solidArea.height = 24;
        setDefaultValues();
        getPlayerImage();
        
        // 初始化剑气
        swordBeam = new SwordBeam(gp);
        inventory = new ArrayList<>();
    }

    public void setDefaultValues() {
        worldX = 148;
        worldY = 100;
        speed = 4;
        direction = "down";
        maxLife = 10;
        life = maxLife-1;
        hasKey = 0; // 重置钥匙数量
        level = 1;
        strength = 1;
        dexterity = 1;
        exp = 0;
        nextLevelExp = 5;
        coin = 3;
        currentShield = null;
        hasSword = false;
        swordCount = 0;
        updateStats();
        if (inventory != null) inventory.clear();
    }
    
    // 更新玩家属性
    public void updateStats() {
        attack = getAttack();
        defense = getDefense();
    }
    
    // 计算攻击力：基础攻击力 + 剑的加成
    public int getAttack() {
        int baseAttack = strength * level; // 基础攻击力
        int swordBonus = swordCount; // 每把剑增加2点攻击力
        return baseAttack + swordBonus;
    }
    
    // 计算防御力：基础防御力 + 盾牌的加成
    public int getDefense() {
        int baseDefense = dexterity * level; // 基础防御力
        int shieldBonus = shieldCount; // 每面盾牌+1防御
        return baseDefense + shieldBonus;
    }
    public BufferedImage flipImageHorizontally(BufferedImage image) {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-image.getWidth(), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }
    public void getPlayerImage() {
        try {
            futureDown1 = ResourceLoader.loadImageAsync("/res/player/down1.png");
            futureDown2 = ResourceLoader.loadImageAsync("/res/player/down2.png");
            futureUp1 = ResourceLoader.loadImageAsync("/res/player/up1.png");
            futureUp2 = ResourceLoader.loadImageAsync("/res/player/up2.png");
            futureLeft1 = ResourceLoader.loadImageAsync("/res/player/left1.png");
            futureLeft2 = ResourceLoader.loadImageAsync("/res/player/left2.png");
            // 右侧图片用左侧翻转，异步后置
            // right1/right2在draw时处理
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void update() {
        // 检查玩家是否死亡
        if (life <= 0 && gp.gameState != gp.deathState) {
            gp.gameState = gp.deathState;
            gp.ui.commandNum = 0; // 重置选择为第一个选项
            return;
        }
        
        // 更新无敌时间
        updateInvincibleTime();
        

        
        // 1. 检查游戏状态
        if (gp.gameState == gp.playState) {
            // 玩家处于可以自由行动的状态

            // 首先检查是否有动作发生（比如与NPC对话）
            // 这个检查需要每一帧都进行，无论是否移动
            interacctNPC();

            // 然后处理移动
            if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {
                if (keyH.upPressed) direction = "up";
                if (keyH.downPressed) direction = "down";
                if (keyH.leftPressed) direction = "left";
                if (keyH.rightPressed) direction = "right";

                // 碰撞检测
                collisionOn = false;
                gp.cChecker.checkTile(this);
                int objIndex = gp.cChecker.checkObject(this, true);
                pickUpObject(objIndex);

                // 检查与NPC的碰撞（阻止移动，但不触发对话）
                gp.cChecker.checkEntity(this, gp.npc);
                
                // 检查与怪物的碰撞（造成伤害，但不阻止移动）
                
                // 检查事件
                gp.mapManager.checkEvents();
                // 如果没有碰撞，则移动
                if (!collisionOn) {
                    switch (direction) {
                        case "up": worldY -= speed; break;
                        case "down": worldY += speed; break;
                        case "left": worldX -= speed; break;
                        case "right": worldX += speed; break;
                    }
                }

                // 更新动画精灵
                spriteCounter++;
                if (spriteCounter > 15) {
                    spriteNum = (spriteNum == 1) ? 2 : 1;
                    spriteCounter = 0;
                }
            }
            
            // 检查剑气发射
            checkSwordBeamFire();

        } else if (gp.gameState == gp.dialogueState) {
            // 玩家处于对话状态，不能移动
            // 等待玩家按F键（Enter键）来推进对话
            if (keyH.enterPressed) {
                if (gp.currentNpc >= 0 && gp.currentNpc < gp.npc.length && gp.npc[gp.currentNpc] != null) {
                    // NPC对话
                    gp.npc[gp.currentNpc].speak();
                } else {
                    // 事件对话，退出对话状态
                    gp.gameState = gp.playState;
                }
                // 消耗掉这次按键，防止一次按键触发多次对话
                keyH.enterPressed = false;
            }
        }
    }

    public void pickUpObject(int i){
        if(i!=999 && gp.obj[i] != null){
            String objectName = gp.obj[i].name;
            Item item = null;
            switch(objectName) {
                case "healingPotion":
                    OBJ_HealingPotion potion = (OBJ_HealingPotion) gp.obj[i];
                    BufferedImage potionImg = potion.image;
                    try {
                        potionImg = ImageIO.read(getClass().getClassLoader().getResourceAsStream("res/objects/healingPotion_item.png"));
                    } catch (Exception e) {}
                    item = new Item(potionImg, "治疗药水", "恢复" + potion.healAmount + "点生命值", Item.ItemType.POTION);
                    break;
                case "key":
                    OBJ_Key key = (OBJ_Key) gp.obj[i];
                    item = new Item(key.image, "钥匙", "可以打开一扇门", Item.ItemType.KEY);
                    break;
                case "sword":
                    OBJ_Sword sword = (OBJ_Sword) gp.obj[i];
                    item = new Item(sword.image, "剑", "可以装备，提高攻击力", Item.ItemType.WEAPON);
                    break;
                case "shield":
                case "Shield":
                    OBJ_Shield shield = (OBJ_Shield) gp.obj[i];
                    item = new Item(shield.image, "盾牌", "可以装备，提高防御力", Item.ItemType.SHIELD);
                    break;
                default:
                    // 其他对象可扩展
                    break;
            }
            if(item != null) {
                inventory.add(item);
                gp.ui.addMessage("获得物品：" + item.name);
                gp.playSE(2); // coins音效
                gp.obj[i] = null; // 移除地图上的物品
            }
        }
    }
    public void contactMonster(int i){
        if(i!=999){
            String monsterName = gp.monster[i].name;
            switch(monsterName){
                case "monster":
                    life--;
                    break;
            }
        }
    }
    // 新增：检测玩家面前一格是否有可交互物体（如宝箱）
    public boolean interactWithFrontObject() {
        int frontX = worldX;
        int frontY = worldY;
        int offset = gp.tileSize; // 一格距离
        switch (direction) {
            case "up":
                frontY -= offset;
                break;
            case "down":
                frontY += offset;
                break;
            case "left":
                frontX -= offset;
                break;
            case "right":
                frontX += offset;
                break;
        }
        for (int i = 0; i < gp.obj.length; i++) {
            if (gp.obj[i] != null && gp.obj[i].name.equals("chest")) {
                // 判断宝箱中心是否在玩家面前一格的范围内
                int objCenterX = gp.obj[i].worldX + gp.obj[i].solidArea.x + gp.obj[i].solidArea.width / 2;
                int objCenterY = gp.obj[i].worldY + gp.obj[i].solidArea.y + gp.obj[i].solidArea.height / 2;
                int playerCenterX = frontX + solidArea.x + solidArea.width / 2;
                int playerCenterY = frontY + solidArea.y + solidArea.height / 2;
                int tolerance = gp.tileSize / 2; // 容差
                if (Math.abs(objCenterX - playerCenterX) <= tolerance && Math.abs(objCenterY - playerCenterY) <= tolerance) {
                    gp.obj[i].interact(this);
                    gp.keyH.enterPressed = false;
                    return true;
                }
            }
        }
        return false;
    }
    public void interacctNPC() {
        // 检查玩家是否按下了互动键
        if (gp.keyH.enterPressed) {
            // 新增：优先检测面前一格是否有可交互物体（如宝箱）
            if (interactWithFrontObject()) {
                return;
            }
            // 查找玩家附近的NPC
            int npcIndex = gp.cChecker.checkEntity(this, gp.npc);

            if (npcIndex != 999) { // 如果玩家碰到了一个NPC
                // 进入对话状态
                gp.npc[npcIndex].facePlayer();
                gp.gameState = gp.dialogueState;
                gp.currentNpc = npcIndex; // 记录正在对话的NPC
                gp.npc[npcIndex].speak(); // 开始第一句对话
            } else {
                // 检查是否与门交互
                syncKeyCountWithInventory();
                int doorIndex = checkDoorInteraction();
                if (doorIndex != 999) {
                    // 尝试开门
                    if (hasKey > 0) {
                        removeKeyFromInventory();
                        syncKeyCountWithInventory();
                        gp.obj[doorIndex] = null; // 移除门
                        gp.gameState = gp.dialogueState;
                        gp.ui.setCurrentDialogue("门被打开了！");
                        gp.currentNpc = -1;
                        gp.playSE(3); // 播放开门音效
                    } else {
                        gp.gameState = gp.dialogueState;
                        gp.ui.setCurrentDialogue("门被锁住了，需要钥匙。");
                        gp.currentNpc = -1;
                    }
                }
            }

            // 无论是否找到NPC，消耗掉这次按键，防止移动时卡顿
            gp.keyH.enterPressed = false;
        }
    }

    // 同步hasKey和背包钥匙数量
    public void syncKeyCountWithInventory() {
        int count = 0;
        for (Item item : inventory) {
            if (item.type == Item.ItemType.KEY) count++;
        }
        hasKey = count;
    }
    // 移除背包中的一个钥匙
    public void removeKeyFromInventory() {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).type == Item.ItemType.KEY) {
                inventory.remove(i);
                break;
            }
        }
    }
    
    // 检查门交互的方法
    public int checkDoorInteraction() {
        for (int i = 0; i < gp.obj.length; i++) {
            if (gp.obj[i] != null && gp.obj[i].name.equals("door")) {
                // 检查玩家是否在门附近
                int distanceX = Math.abs(worldX - gp.obj[i].worldX);
                int distanceY = Math.abs(worldY - gp.obj[i].worldY);
                
                // 如果玩家在门的附近（一个瓦片范围内）
                if (distanceX <= gp.tileSize && distanceY <= gp.tileSize) {
                    return i;
                }
            }
        }
        return 999; // 没有找到门
    }
    
    // 检查与怪物的碰撞并造成伤害
    public void checkMonsterCollision() {
        if (invincible) {
            return; // 如果处于无敌状态，不检查碰撞
        }
        
        for (Entity monster : gp.monster) {
            if (monster != null && monster.life > 0) {
                // 获取玩家的碰撞箱
                Rectangle playerSolidArea = new Rectangle(
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
                if (playerSolidArea.intersects(monsterSolidArea)) {
                    // 使用怪物的攻击力作为伤害
                    int monsterDamage = monster.attack;
                    takeDamage(monsterDamage);
                    break; // 只处理第一个碰撞的怪物
                }
            }
        }
    }
    
    // 受到伤害：敌人伤害 - 防御力
    public void takeDamage(int enemyDamage) {
        if (!invincible) {
            // 生成红色粒子
            java.util.Random rand = new java.util.Random();
            int particleNum = 12;
            for (int i = 0; i < particleNum; i++) {
                double angle = rand.nextDouble() * Math.PI * 2;
                double speed = 3 + rand.nextDouble() * 2; // 3~5
                double vx = Math.cos(angle) * speed;
                double vy = Math.sin(angle) * speed - 2; // 初始有向上的分量
                java.awt.Color color = new java.awt.Color(220 + rand.nextInt(36), rand.nextInt(40), rand.nextInt(40));
                gp.particleList.add(new entity.Particle(gp, this, color, 6, vx, vy, 30 + rand.nextInt(10)));
            }
            // 计算实际伤害：敌人伤害 - 防御力，最小为1
            int actualDamage = Math.max(1, enemyDamage - defense);
            
            life -= actualDamage;
            if (life < 0) life = 0;
            
            // 进入无敌状态
            invincible = true;
            invincibleCounter = 0;
            
            // 播放玩家受伤音效
            gp.playSE(6); // 玩家受伤音效
        }
    }
    
    // 更新无敌时间
    public void updateInvincibleTime() {
        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter >= invincibleTime) {
                invincible = false;
                invincibleCounter = 0;
            }
        }
    }
    
    // 检查剑气发射
    public void checkSwordBeamFire() {
        // 检查是否按E键发射剑气
        if (gp.keyH.ePressed && hasSword && !swordBeam.isActive()) {
            // 计算剑气起始位置（从玩家位置发射）
            int beamStartX = worldX;
            int beamStartY = worldY;
            
            // 根据方向调整起始位置
            switch (direction) {
                case "up":
                    beamStartY -= gp.tileSize;
                    break;
                case "down":
                    beamStartY += gp.tileSize;
                    break;
                case "left":
                    beamStartX -= gp.tileSize;
                    break;
                case "right":
                    beamStartX += gp.tileSize;
                    break;
            }
            
            // 发射剑气
            swordBeam.fire(beamStartX, beamStartY, direction);
            
            // 播放挥剑音效
            gp.playSE(5); // 挥剑音效
            
            // 重置按键状态
            gp.keyH.ePressed = false;
        } else if (gp.keyH.ePressed) {
            gp.keyH.ePressed = false;
        }
        
        // 更新剑气
        if (swordBeam.isActive()) {
            swordBeam.update();
        }
    }

    // 新增：经验足够时自动升级
    public void checkLevelUp() {
        while (exp >= nextLevelExp) {
            exp -= nextLevelExp;
            level++;
            strength++;
            dexterity++;
            maxLife += 2;
            life = maxLife;
            nextLevelExp = nextLevelExp + level * 2; // 升级所需经验递增
            updateStats();
            gp.ui.addMessage("升级了！等级:" + level + " 生命+2 力量+1 敏捷+1");
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        try {
            if (futureDown1 != null && futureDown1.isDone()) { down1 = futureDown1.get(); futureDown1 = null; }
            if (futureDown2 != null && futureDown2.isDone()) { down2 = futureDown2.get(); futureDown2 = null; }
            if (futureUp1 != null && futureUp1.isDone()) { up1 = futureUp1.get(); futureUp1 = null; }
            if (futureUp2 != null && futureUp2.isDone()) { up2 = futureUp2.get(); futureUp2 = null; }
            if (futureLeft1 != null && futureLeft1.isDone()) { left1 = futureLeft1.get(); futureLeft1 = null; }
            if (futureLeft2 != null && futureLeft2.isDone()) { left2 = futureLeft2.get(); futureLeft2 = null; }
            // 右侧图片用左侧翻转
            if (left1 != null && right1 == null) right1 = flipImageHorizontally(left1);
            if (left2 != null && right2 == null) right2 = flipImageHorizontally(left2);
        } catch (Exception e) { /* 忽略未加载完成异常 */ }
        // 选择图片，未加载完成用PLACEHOLDER
        BufferedImage image = null;
        switch (direction) {
            case "up": image = (spriteNum == 1 ? (up1 != null ? up1 : PLACEHOLDER) : (up2 != null ? up2 : PLACEHOLDER)); break;
            case "down": image = (spriteNum == 1 ? (down1 != null ? down1 : PLACEHOLDER) : (down2 != null ? down2 : PLACEHOLDER)); break;
            case "left": image = (spriteNum == 1 ? (left1 != null ? left1 : PLACEHOLDER) : (left2 != null ? left2 : PLACEHOLDER)); break;
            case "right": image = (spriteNum == 1 ? (right1 != null ? right1 : PLACEHOLDER) : (right2 != null ? right2 : PLACEHOLDER)); break;
        }
        if (invincible) {
            // 闪烁：每4帧消失一次，显示时为半透明
            if ((invincibleCounter / 4) % 2 == 0) {
                Composite old = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
                g2.setComposite(old);
            }
            // else 不画，达到闪烁效果
        } else {
            g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
        }
        // 注意：剑气现在在GamePanel中绘制，避免重复绘制
    }
}