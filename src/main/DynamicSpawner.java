package main;

import entity.Entity;
import entity.Monster;
import object.OBJ_Chest;

import java.util.Random;

public class DynamicSpawner {

    private GamePanel gp;
    private Random random = new Random();

    // 计时器变量
    private long lastMonsterSpawnTime;
    private long lastChestSpawnTime;

    // 间隔时间（单位：纳秒）
    private static final long MONSTER_SPAWN_INTERVAL = 5_000_000_000L; // 5秒
    private static final long CHEST_SPAWN_INTERVAL = 10_000_000_000L; // 10秒

    public DynamicSpawner(GamePanel gp) {
        this.gp = gp;
        resetTimers();
    }

    /**
     * 重置计时器，通常在切换地图时调用
     */
    public void resetTimers() {
        long currentTime = System.nanoTime();
        lastMonsterSpawnTime = currentTime;
        lastChestSpawnTime = currentTime;
    }

    /**
     * 每帧调用的主更新方法
     */
    public void update() {
        // 检查当前地图是否开启了动态生成
        if (gp.mapManager.getCurrentMap() == null || !gp.mapManager.getCurrentMap().dynamicSpawningEnabled) {
            return;
        }

        long currentTime = System.nanoTime();

        // 检查是否到了生成怪物的时间
        if (currentTime - lastMonsterSpawnTime > MONSTER_SPAWN_INTERVAL) {
            spawnMonster();
            lastMonsterSpawnTime = currentTime;
        }

        // 检查是否到了生成宝箱的时间
        if (currentTime - lastChestSpawnTime > CHEST_SPAWN_INTERVAL) {
            spawnChest();
            lastChestSpawnTime = currentTime;
        }
    }

    // 在 DynamicSpawner.java 中
private void spawnMonster() {
    // 寻找一个空位来放置怪物
    for (int i = 0; i < gp.monster.length; i++) {
        if (gp.monster[i] == null) {
            
            // --- 核心修改：根据玩家等级计算怪物属性 ---
            
            // 1. 获取玩家当前等级
            int playerLevel = gp.player.level;

            // 2. 设计属性计算公式
            // 基础血量为 4，每级增加 2 点血量
            int monsterMaxHealth = 10 + (playerLevel - 1) * 20;
            
            // 基础攻击力为 2，每 2 级增加 1 点攻击力
            int monsterAttackPower = (playerLevel - 1) * 5;

            // 为了增加随机性，可以给属性一个小的随机浮动范围 (例如 ±10%)
            Random rand = new Random();
            monsterMaxHealth = (int)(monsterMaxHealth * (0.9 + rand.nextDouble() * 0.2)); // 90% ~ 110%
            // 攻击力最好保持整数，避免过于复杂的计算
            // monsterAttackPower = (int)(monsterAttackPower * (0.9 + rand.nextDouble() * 0.2));

            // 确保属性值不低于基础值
            monsterMaxHealth = Math.max(4, monsterMaxHealth);
            monsterAttackPower = Math.max(2, monsterAttackPower);
            
            // ---------------------------------------------

            // 3. 寻找一个安全的生成坐标
            int[] coords = findSafeSpawnCoordinates();
            if (coords != null) {
                // 使用带有属性参数的构造函数来创建怪物
                gp.monster[i] = new Monster(gp, monsterMaxHealth, monsterAttackPower);
                gp.monster[i].worldX = coords[0];
                gp.monster[i].worldY = coords[1];
                
                // gp.ui.addMessage("一个更强的怪物出现了！"); // (可选) 修改提示信息
                break; // 已生成一个，退出循环
            }
        }
    }
}

    private void spawnChest() {
        // 寻找一个空位来放置对象
        for (int i = 0; i < gp.obj.length; i++) {
            if (gp.obj[i] == null) {
                // 找到了空位，现在寻找一个安全的生成坐标
                int[] coords = findSafeSpawnCoordinates();
                if (coords != null) {
                    gp.obj[i] = new OBJ_Chest(gp);
                    gp.obj[i].worldX = coords[0];
                    gp.obj[i].worldY = coords[1];
                    gp.ui.addMessage("一个神秘的宝箱出现了！"); // (可选)
                    break; // 已生成一个，退出循环
                }
            }
        }
    }

    /**
     * 寻找一个安全的、在玩家视野外的生成坐标
     * @return 返回一个包含 {x, y} 坐标的数组，如果找不到则返回 null
     */
    private int[] findSafeSpawnCoordinates() {
        int maxAttempts = 50; // 最多尝试50次，防止死循环
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // 随机选择一个地图格子
            int col = random.nextInt(gp.maxWorldCol);
            int row = random.nextInt(gp.maxWorldRow);

            int worldX = col * gp.tileSize;
            int worldY = row * gp.tileSize;

            // 检查该位置是否安全
            if (isSpawnLocationSafe(worldX, worldY)) {
                return new int[]{worldX, worldY};
            }
        }
        return null; // 尝试多次后仍未找到安全位置
    }

    /**
     * 检查给定的世界坐标是否适合生成实体
     * @param worldX 世界X坐标
     * @param worldY 世界Y坐标
     * @return 如果安全则返回 true
     */
    private boolean isSpawnLocationSafe(int worldX, int worldY) {
        // 1. 检查是否在玩家视野内
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        if (screenX > -gp.tileSize && screenX < gp.screenWidth &&
            screenY > -gp.tileSize && screenY < gp.screenHeight) {
            return false; // 在视野内，不安全
        }

        // 2. 检查瓦片碰撞
        int col = worldX / gp.tileSize;
        int row = worldY / gp.tileSize;
        
        // 确保坐标在地图边界内
        if (col < 0 || col >= gp.maxWorldCol || row < 0 || row >= gp.maxWorldRow) {
            return false;
        }

        int tileNum = gp.TileM.mapTileNum[col][row];
        if (gp.TileM.tile[tileNum].collision) {
            return false; // 该瓦片是障碍物，不安全
        }

        // 3. 检查是否与其他实体（NPC, 怪物, 对象）重叠
        // 创建一个临时的碰撞矩形来检查
        java.awt.Rectangle spawnArea = new java.awt.Rectangle(worldX, worldY, gp.tileSize, gp.tileSize);
        for (Entity npc : gp.npc) {
            if (npc != null && spawnArea.intersects(npc.solidArea)) return false;
        }
        for (Entity monster : gp.monster) {
            if (monster != null && spawnArea.intersects(monster.solidArea)) return false;
        }
        for (Entity obj : gp.obj) {
            if (obj != null && spawnArea.intersects(obj.solidArea)) return false;
        }

        // 所有检查都通过，是安全位置
        return true;
    }
}