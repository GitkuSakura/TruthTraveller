package entity;

import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import main.Tools;
import main.ResourceLoader;
import java.util.concurrent.Future;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

public class Monster extends Entity {
    
    // 死亡闪烁相关变量
    private boolean isDead = false;
    private int deathCounter = 0;
    private final int deathFlashTime = 60; // 1秒闪烁时间（60帧）
    
    // A*寻路相关变量
    public List<Point> path = null;
    private int pathIndex = 0;
    private int pathUpdateCounter = 0;
    private final int pathUpdateInterval = 20; // 每20帧重新寻路
    // 追踪感知半径
    private final int trackingRadius = 5; // 恢复默认4格
    
    // 追踪相关变量
    public boolean isTracking = false; // 是否正在追踪玩家
    
    // 击退相关变量
    private boolean isKnockback = false; // 是否正在被击退
    private int knockbackCounter = 0; // 击退时间计数器
    private final int knockbackTime = 30; // 击退持续时间（30帧 = 0.5秒）
    private String knockbackDirection = ""; // 击退方向
    private int knockbackDistance = 0; // 击退距离
    private int currentKnockbackDistance = 0; // 当前已击退距离
    
    // 智能脱困相关变量
    public int stuckCounter = 0;
    private int stuckCooldown = 0;
    private final int stuckThreshold = 5; // 连续5帧无法移动判定为卡住
    private final int stuckCooldownTime = 60; // 冷却60帧（1秒）
    
    // 记录上一次位置用于卡住判定
    private int lastWorldX = 0;
    private int lastWorldY = 0;
    
    // 临时避障格
    private int avoidCol = -1;
    private int avoidRow = -1;
    
    // 新增：异步图片加载Future
    private Future<BufferedImage> futureDown1, futureDown2, futureUp1, futureUp2, futureLeft1, futureLeft2, futureRight1, futureRight2;
    private static final BufferedImage PLACEHOLDER = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
    
    // 怪物与怪物之间体积碰撞开关
    public static boolean monsterCollisionEnabled = false;
    
    public Monster(GamePanel gp) {
        this(gp, 4, 2); // 默认4点血量，2点攻击力
    }
    
    public Monster(GamePanel gp, int maxHealth) {
        this(gp, maxHealth, 2); // 默认2点攻击力
    }
    
    public Monster(GamePanel gp, int maxHealth, int attackPower) {
        super(gp);
        direction = "down";
        speed = 2; // 怪物移动速度比玩家慢
        // 缩小碰撞箱体积为tileSize的60%
        int boxSize = (int)(gp.tileSize * 0.6);
        solidArea = new Rectangle((gp.tileSize - boxSize) / 2, (gp.tileSize - boxSize) / 2, boxSize, boxSize);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
        // 设置生命值和攻击力
        maxLife = maxHealth;
        life = maxLife;
        attack = attackPower; // 设置怪物攻击力
        // 设置怪物标识
        name = "monster";
        getMonsterImage();
    }
    
    public void getMonsterImage() {
        try {
            futureDown1 = ResourceLoader.loadImageAsync("/res/monster/monster_down.png");
            futureDown2 = ResourceLoader.loadImageAsync("/res/monster/monster_down.png");
            futureUp1 = ResourceLoader.loadImageAsync("/res/monster/monster_up.png");
            futureUp2 = ResourceLoader.loadImageAsync("/res/monster/monster_up.png");
            futureLeft1 = ResourceLoader.loadImageAsync("/res/monster/monster_down.png");
            futureLeft2 = ResourceLoader.loadImageAsync("/res/monster/monster_down.png");
            // 右侧图片用左侧翻转，异步后置
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void setAction() {
        // 冷却期间只做随机移动
        if (stuckCooldown > 0) {
            stuckCooldown--;
            path = null;
            pathIndex = 0;
            isTracking = false;
            randomMove();
            return;
        }
        // 检查是否应该追踪玩家
        if (shouldTrackPlayer()) {
            trackPlayerAStar();
        } else {
            // 如果不在追踪状态，进行随机移动
            path = null;
            pathIndex = 0;
            randomMove();
        }
    }
    
    // 检查是否应该追踪玩家
    private boolean shouldTrackPlayer() {
        // 只有当玩家有剑时才追踪
        if (!gp.player.hasSword) {
            isTracking = false;
            return false;
        }
        // 计算与玩家的距离
        int col = worldX / gp.tileSize;
        int row = worldY / gp.tileSize;
        int playerCol = gp.player.worldX / gp.tileSize;
        int playerRow = gp.player.worldY / gp.tileSize;
        int manhattanDistance = Math.abs(col - playerCol) + Math.abs(row - playerRow);
        // 如果玩家在追踪半径内，开始追踪
        if (manhattanDistance <= trackingRadius) {
            isTracking = true;
            return true;
        } else {
            // 如果玩家离开追踪半径，停止追踪
            isTracking = false;
            return false;
        }
    }

    // 使用A*算法追踪玩家
    private void trackPlayerAStar() {
        int col = worldX / gp.tileSize;
        int row = worldY / gp.tileSize;
        int playerCol = gp.player.worldX / gp.tileSize;
        int playerRow = gp.player.worldY / gp.tileSize;
        pathUpdateCounter++;
        // 每隔pathUpdateInterval帧或路径为空/走完时重新寻路
        if (path == null || pathIndex >= (path != null ? path.size() : 0) || pathUpdateCounter >= pathUpdateInterval) {
            path = main.Tools.findPathAStar(col, row, playerCol, playerRow, gp.TileM, gp.maxWorldCol, gp.maxWorldRow);
            pathIndex = 0;
            pathUpdateCounter = 0;
        }
        // 沿路径移动
        if (path != null && path.size() > 0 && pathIndex < path.size()) {
            Point next = path.get(pathIndex);
            int dx = next.x - col;
            int dy = next.y - row;
            if (dx == 1) direction = "right";
            else if (dx == -1) direction = "left";
            else if (dy == 1) direction = "down";
            else if (dy == -1) direction = "up";
            // 如果已经到达该格，进入下一个
            if (col == next.x && row == next.y && pathIndex < path.size() - 1) {
                pathIndex++;
            }
            stuckCounter = 0; // 能正常移动，重置卡住计数
        } else {
            // 路径为空或已到终点，判定为卡住
            stuckCounter++;
            if (stuckCounter >= stuckThreshold) {
                // 方案2：尝试斜向或后退移动
                boolean escaped = false;
                int oldX = worldX, oldY = worldY;
                String[] tryDirs;
                // 先尝试斜向
                if ("up".equals(direction) || "down".equals(direction)) {
                    tryDirs = new String[]{"left", "right"};
                } else {
                    tryDirs = new String[]{"up", "down"};
                }
                for (String tryDir : tryDirs) {
                    // 尝试斜向移动
                    int testX = worldX, testY = worldY;
                    switch (tryDir) {
                        case "up": testY -= speed; break;
                        case "down": testY += speed; break;
                        case "left": testX -= speed; break;
                        case "right": testX += speed; break;
                    }
                    // 临时移动
                    worldX = testX; worldY = testY;
                    collisionOn = false;
                    gp.cChecker.checkTile(this);
                    gp.cChecker.checkObject(this, false);
                    checkMonsterCollision();
                    if (!collisionOn) {
                        direction = tryDir;
                        escaped = true;
                        break;
                    }
                }
                // 如果斜向不行，尝试后退
                if (!escaped) {
                    worldX = oldX; worldY = oldY;
                    String backDir = "";
                    switch (direction) {
                        case "up": backDir = "down"; break;
                        case "down": backDir = "up"; break;
                        case "left": backDir = "right"; break;
                        case "right": backDir = "left"; break;
                    }
                    int testX = worldX, testY = worldY;
                    switch (backDir) {
                        case "up": testY -= speed; break;
                        case "down": testY += speed; break;
                        case "left": testX -= speed; break;
                        case "right": testX += speed; break;
                    }
                    worldX = testX; worldY = testY;
                    collisionOn = false;
                    gp.cChecker.checkTile(this);
                    gp.cChecker.checkObject(this, false);
                    checkMonsterCollision();
                    if (!collisionOn) {
                        direction = backDir;
                        escaped = true;
                    }
                }
                // 恢复原位
                worldX = oldX; worldY = oldY;
                if (escaped) {
                    // 立即移动一格
                    switch (direction) {
                        case "up": worldY -= speed; break;
                        case "down": worldY += speed; break;
                        case "left": worldX -= speed; break;
                        case "right": worldX += speed; break;
                    }
                    stuckCounter = 0;
                } else {
                    stuckCooldown = stuckCooldownTime;
                    stuckCounter = 0;
                    path = null;
                    pathIndex = 0;
                    isTracking = false;
                }
            }
            randomMove();
        }
        // 重置动作计数器，让追踪更流畅
        actionLockCounter = 0;
    }
    
    // 随机移动
    public void randomMove() {
        actionLockCounter++;
        if (actionLockCounter >= 120) { // 每120帧改变一次方向
            Random random = new Random();
            int i = random.nextInt(100) + 1;
            
            if (i <= 25) {
                direction = "up";
            } else if (i <= 50) {
                direction = "down";
            } else if (i <= 75) {
                direction = "left";
            } else {
                direction = "right";
            }
            
            actionLockCounter = 0;
        }
    }
    
    @Override
    public void update() {
        // 检查怪物是否死亡
        if (life <= 0 && !isDead) {
            isDead = true;
            deathCounter = 0;
        }
        
        // 如果怪物已死亡，处理闪烁效果
        if (isDead) {
            deathCounter++;
            if (deathCounter >= deathFlashTime) {
                // 闪烁结束，从游戏中移除
                removeFromGame();
                return;
            }
            return; // 死亡状态下不进行其他更新
        }
        
        // 更新击退效果（优先级最高）
        updateKnockback();
        
        // 如果正在被击退，不进行正常移动
        if (isKnockback) {
            // 只更新动画
            spriteCounter++;
            if (spriteCounter > 15) {
                if (spriteNum == 1) {
                    spriteNum = 2;
                } else if (spriteNum == 2) {
                    spriteNum = 1;
                }
                spriteCounter = 0;
            }
            return;
        }
        
        // --- AI主逻辑 ---
        setAction();
        collisionOn = false;
        gp.cChecker.checkTile(this);
        gp.cChecker.checkObject(this, false);
        
        // 检查与玩家的碰撞（防止穿过玩家）
        checkPlayerCollision();
        // 根据开关决定是否检测怪物间体积碰撞
        if (monsterCollisionEnabled) {
            checkMonsterCollision();
        }
        
        // 主动攻击玩家逻辑
        // 只要怪物与玩家距离小于一定值（如碰撞箱重叠或中心点距离小于阈值），玩家就受伤
        if (!isDead && !gp.player.invincible) {
            Rectangle monsterSolidArea = new Rectangle(
                worldX + solidArea.x,
                worldY + solidArea.y,
                solidArea.width,
                solidArea.height
            );
            Rectangle playerSolidArea = new Rectangle(
                gp.player.worldX + gp.player.solidArea.x,
                gp.player.worldY + gp.player.solidArea.y,
                gp.player.solidArea.width,
                gp.player.solidArea.height
            );
            // 距离阈值（像素），可根据实际体验调整
            int threshold = 8;
            // 检查碰撞箱是否重叠，或中心点距离小于阈值
            boolean overlap = monsterSolidArea.intersects(playerSolidArea);
            int monsterCenterX = monsterSolidArea.x + monsterSolidArea.width / 2;
            int monsterCenterY = monsterSolidArea.y + monsterSolidArea.height / 2;
            int playerCenterX = playerSolidArea.x + playerSolidArea.width / 2;
            int playerCenterY = playerSolidArea.y + playerSolidArea.height / 2;
            double centerDist = Math.hypot(monsterCenterX - playerCenterX, monsterCenterY - playerCenterY);
            if (overlap || centerDist < threshold) {
                // 造成伤害并让玩家进入无敌
                int monsterDamage = this.attack;
                gp.player.takeDamage(monsterDamage);
            }
        }
        
        // 1. 坐标换算统一用碰撞箱中心点
        int centerX = worldX + solidArea.x + solidArea.width / 2;
        int centerY = worldY + solidArea.y + solidArea.height / 2;
        int tileCol = centerX / gp.tileSize;
        int tileRow = centerY / gp.tileSize;
        
        // 恢复最初的卡住处理逻辑：仅A*寻路+基础卡住计数
        if (collisionOn) {
            stuckCounter++;
        } else {
            stuckCounter = 0;
        }
        if (stuckCounter > stuckThreshold) {
            // 进入冷却，短暂不追踪
            stuckCooldown = stuckCooldownTime;
            stuckCounter = 0;
            isTracking = false;
            path = null;
        }
        lastWorldX = worldX;
        lastWorldY = worldY;
        
        // 追踪距离判定，超出感知半径则放弃追踪
        int playerCenterX = gp.player.worldX + gp.player.solidArea.x + gp.player.solidArea.width / 2;
        int playerCenterY = gp.player.worldY + gp.player.solidArea.y + gp.player.solidArea.height / 2;
        int playerCol = playerCenterX / gp.tileSize;
        int playerRow = playerCenterY / gp.tileSize;
        int dist = Math.abs(playerCol - tileCol) + Math.abs(playerRow - tileRow);
        if (dist > trackingRadius) {
            isTracking = false;
            path = null;
        }
        
        // 移动逻辑
        if (!collisionOn) {
            switch (direction) {
                case "up": worldY -= speed; break;
                case "down": worldY += speed; break;
                case "left": worldX -= speed; break;
                case "right": worldX += speed; break;
            }
        } else {
            // 如果是因为怪物碰撞导致的卡住，尝试随机换方向
            randomMove();
        }
        
        // 动画更新
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
    
    // 从游戏中移除怪物
    private void removeFromGame() {
        Random random = new Random();
        // 0.3概率掉落金币（直接入账）
        if (random.nextDouble() < 0.3) {
            gp.player.coin += 1;
            gp.ui.addMessage("获得1金币");
        }
        // 掉落经验（攻击力//2，直接入账）
        int expGain = Math.max(1, this.attack / 4);
        gp.player.exp += expGain;
        gp.ui.addMessage("获得" + expGain + "经验");
        // 处理升级
        gp.player.checkLevelUp();
        // 保留原有掉落剑逻辑（如需可注释掉）
        if (random.nextDouble() < 0.3) {
            dropSword();
        }
        // 怪物数组中移除
        for (int i = 0; i < gp.monster.length; i++) {
            if (gp.monster[i] == this) {
                gp.monster[i] = null;
                break;
            }
        }
    }
    
    // 掉落剑
    private void dropSword() {
        // 在对象数组中查找空位
        for (int i = 0; i < gp.obj.length; i++) {
            if (gp.obj[i] == null) {
                // 创建剑对象
                gp.obj[i] = new object.OBJ_Sword(gp);
                gp.obj[i].worldX = this.worldX;
                gp.obj[i].worldY = this.worldY;
                break;
            }
        }
    }
    
    // 检查与玩家的碰撞
    public void checkPlayerCollision() {
        // 如果怪物已死亡，不进行碰撞检测
        if (isDead) {
            return;
        }
        
        // 获取怪物的碰撞箱
        Rectangle monsterSolidArea = new Rectangle(
            worldX + solidArea.x, 
            worldY + solidArea.y, 
            solidArea.width, 
            solidArea.height
        );
        
        // 获取玩家的碰撞箱
        Rectangle playerSolidArea = new Rectangle(
            gp.player.worldX + gp.player.solidArea.x, 
            gp.player.worldY + gp.player.solidArea.y, 
            gp.player.solidArea.width, 
            gp.player.solidArea.height
        );
        
        // 检查碰撞
        if (monsterSolidArea.intersects(playerSolidArea)) {
            collisionOn = true; // 设置碰撞标志，阻止移动
        }
    }
    
    // 检查与其他怪物的碰撞
    public void checkMonsterCollision() {
        // 如果怪物已死亡，不进行碰撞检测
        if (isDead) {
            return;
        }
        // 获取当前怪物的碰撞箱
        Rectangle currentMonsterSolidArea = new Rectangle(
            worldX + solidArea.x, 
            worldY + solidArea.y, 
            solidArea.width, 
            solidArea.height
        );
        // 检查与其他怪物的碰撞
        for (Entity otherMonster : gp.monster) {
            if (otherMonster != null && otherMonster != this && otherMonster.life > 0) {
                // 获取其他怪物的碰撞箱
                Rectangle otherMonsterSolidArea = new Rectangle(
                    otherMonster.worldX + otherMonster.solidArea.x, 
                    otherMonster.worldY + otherMonster.solidArea.y, 
                    otherMonster.solidArea.width, 
                    otherMonster.solidArea.height
                );
                // 检查碰撞
                if (currentMonsterSolidArea.intersects(otherMonsterSolidArea)) {
                    collisionOn = true; // 设置碰撞标志，阻止移动
                    // 如果是因为怪物碰撞导致的卡住，立即随机换方向
                    randomMove();
                    break; // 只处理第一个碰撞的怪物
                }
            }
        }
    }
    
    // 受到击退效果
    public void takeKnockback(String direction) {
        if (!isDead) {
            // 计算击退距离：0.5 + 0.1 * 玩家拥有的剑数量
            knockbackDistance = (int)((0.2 + 0.1 * gp.player.swordCount) * gp.tileSize);
            knockbackDirection = direction;
            currentKnockbackDistance = 0;
            isKnockback = true;
            knockbackCounter = 0;
        }
    }
    
    // 更新击退效果
    private void updateKnockback() {
        if (isKnockback) {
            knockbackCounter++;
            
            // 击退移动
            if (currentKnockbackDistance < knockbackDistance) {
                int knockbackSpeed = 4; // 击退速度
                
                // 临时保存当前位置
                int oldX = worldX;
                int oldY = worldY;
                
                // 根据击退方向移动
                switch (knockbackDirection) {
                    case "up":
                        worldY -= knockbackSpeed;
                        break;
                    case "down":
                        worldY += knockbackSpeed;
                        break;
                    case "left":
                        worldX -= knockbackSpeed;
                        break;
                    case "right":
                        worldX += knockbackSpeed;
                        break;
                }
                
                // 检查碰撞，如果撞到墙壁则停止击退
                collisionOn = false;
                gp.cChecker.checkTile(this);
                if (collisionOn) {
                    // 撞到墙壁，恢复位置并停止击退
                    worldX = oldX;
                    worldY = oldY;
                    isKnockback = false;
                    return;
                }
                
                currentKnockbackDistance += knockbackSpeed;
            }
            
            // 检查击退时间是否结束
            if (knockbackCounter >= knockbackTime || currentKnockbackDistance >= knockbackDistance) {
                isKnockback = false;
                knockbackCounter = 0;
            }
        }
    }
    
    // 受伤方法，生成红色粒子
    public void takeDamage(int damage) {
        if (!isDead) {
            java.util.Random rand = new java.util.Random();
            int particleNum = 12;
            for (int i = 0; i < particleNum; i++) {
                double angle = rand.nextDouble() * Math.PI * 2;
                double speed = 3 + rand.nextDouble() * 2;
                double vx = Math.cos(angle) * speed;
                double vy = Math.sin(angle) * speed - 2;
                java.awt.Color color = new java.awt.Color(220 + rand.nextInt(36), rand.nextInt(40), rand.nextInt(40));
                gp.particleList.add(new entity.Particle(gp, this, color, 6, vx, vy, 30 + rand.nextInt(10)));
            }
            life -= damage;
            if (life < 0) life = 0;
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
        g2.drawImage(image, worldX - gp.player.worldX + gp.player.screenX, worldY - gp.player.worldY + gp.player.screenY, gp.tileSize, gp.tileSize, null);
        // 其余血条等绘制逻辑保持不变
        // 如果怪物已死亡，处理闪烁效果
        if (isDead) {
            // 每10帧闪烁一次（每10帧显示/隐藏一次）
            if (deathCounter % 20 < 10) {
                super.draw(g2);
            }
            return;
        }
        
        // 正常绘制
        super.draw(g2);
    }

    // 4. A*寻路时动态避障和临时避障
    public void trackPlayerAStar(boolean avoidBlocked) {
        int startCol = (worldX + solidArea.x + solidArea.width / 2) / gp.tileSize;
        int startRow = (worldY + solidArea.y + solidArea.height / 2) / gp.tileSize;
        int goalCol = (gp.player.worldX + gp.player.solidArea.x + gp.player.solidArea.width / 2) / gp.tileSize;
        int goalRow = (gp.player.worldY + gp.player.solidArea.y + gp.player.solidArea.height / 2) / gp.tileSize;
        pathUpdateCounter++;
        // 每隔pathUpdateInterval帧或路径为空/走完时重新寻路
        if (path == null || pathIndex >= (path != null ? path.size() : 0) || pathUpdateCounter >= pathUpdateInterval) {
            path = Tools.findPathAStar(startCol, startRow, goalCol, goalRow, gp.TileM, gp.maxWorldCol, gp.maxWorldRow);
            pathIndex = 0;
            pathUpdateCounter = 0;
        }
        // 动态避障
        List<Point> avoidList = new ArrayList<>();
        if (avoidBlocked && avoidCol >= 0 && avoidRow >= 0) {
            avoidList.add(new Point(avoidCol, avoidRow));
        }
        for (Entity e : gp.monster) {
            if (e != this && e != null) {
                Monster m = (Monster) e;
                int mCol = (m.worldX + m.solidArea.x + m.solidArea.width / 2) / gp.tileSize;
                int mRow = (m.worldY + m.solidArea.y + m.solidArea.height / 2) / gp.tileSize;
                avoidList.add(new Point(mCol, mRow));
            }
        }
        // 玩家也可选加入避障
        // avoidList.add(new Point(goalCol, goalRow));
        // 调用A*时传入avoidList，A*实现需支持跳过这些格子
        // path = Tools.findPathAStar(startCol, startRow, goalCol, goalRow, avoidList);
        // pathIndex = 0;
    }

    // 新增：图片水平翻转方法
    private BufferedImage flipImageHorizontally(BufferedImage image) {
        java.awt.geom.AffineTransform tx = java.awt.geom.AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-image.getWidth(), 0);
        java.awt.image.AffineTransformOp op = new java.awt.image.AffineTransformOp(tx, java.awt.image.AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }
} 