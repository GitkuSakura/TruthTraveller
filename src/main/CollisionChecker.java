package main;

import java.util.List;

import entity.Entity;

public class CollisionChecker {
    GamePanel gp;
    Entity entity;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkTile(Entity entity) {

        // --- 核心修复：在方法开头添加边界检查 ---
        // 确保实体的位置总是在有效的世界坐标内
        if (entity.worldX < 0 || entity.worldX >= gp.maxWorldCol * gp.tileSize ||
            entity.worldY < 0 || entity.worldY >= gp.maxWorldRow * gp.tileSize) {
            entity.collisionOn = true; // 如果实体在地图外，直接标记为碰撞，阻止其进一步移动
            return; // 提前退出，不再进行后续计算
        }
    
        // 计算实体碰撞箱的四个角的世界坐标
        int entityLeftWorldX = entity.worldX + entity.solidArea.x;
        int entityRightWorldX = entity.worldX + entity.solidArea.x + entity.solidArea.width;
        int entityTopWorldY = entity.worldY + entity.solidArea.y;
        int entityBottomWorldY = entity.worldY + entity.solidArea.y + entity.solidArea.height;
    
        // 根据世界坐标计算对应的瓦片列号和行号
        int entityLeftCol = entityLeftWorldX / gp.tileSize;
        int entityRightCol = entityRightWorldX / gp.tileSize;
        int entityTopRow = entityTopWorldY / gp.tileSize;
        int entityBottomRow = entityBottomWorldY / gp.tileSize;
    
        int tileNum1, tileNum2;
    
        switch (entity.direction) {
            case "up":
                // 预测实体向上移动后的顶部Y坐标所在的行
                entityTopRow = (entityTopWorldY - entity.speed) / gp.tileSize;
                
                // --- 再次添加更精确的边界检查，防止计算后的坐标越界 ---
                if (entityTopRow < 0) {
                    entity.collisionOn = true;
                    return;
                }
    
                tileNum1 = gp.TileM.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.TileM.mapTileNum[entityRightCol][entityTopRow];
                if (gp.TileM.tile[tileNum1].collision || gp.TileM.tile[tileNum2].collision) {
                    entity.collisionOn = true;
                }
                break;
            case "down":
                // 预测实体向下移动后的底部Y坐标所在的行
                entityBottomRow = (entityBottomWorldY + entity.speed) / gp.tileSize;
    
                // --- 边界检查 ---
                if (entityBottomRow >= gp.maxWorldRow) {
                    entity.collisionOn = true;
                    return;
                }
                
                tileNum1 = gp.TileM.mapTileNum[entityLeftCol][entityBottomRow];
                tileNum2 = gp.TileM.mapTileNum[entityRightCol][entityBottomRow];
                if (gp.TileM.tile[tileNum1].collision || gp.TileM.tile[tileNum2].collision) {
                    entity.collisionOn = true;
                }
                break;
            case "left":
                // 预测实体向左移动后的左侧X坐标所在的列
                entityLeftCol = (entityLeftWorldX - entity.speed) / gp.tileSize;
                
                // --- 边界检查 ---
                if (entityLeftCol < 0) {
                    entity.collisionOn = true;
                    return;
                }
    
                tileNum1 = gp.TileM.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.TileM.mapTileNum[entityLeftCol][entityBottomRow];
                if (gp.TileM.tile[tileNum1].collision || gp.TileM.tile[tileNum2].collision) {
                    entity.collisionOn = true;
                }
                break;
            case "right":
                // 预测实体向右移动后的右侧X坐标所在的列
                entityRightCol = (entityRightWorldX + entity.speed) / gp.tileSize;
    
                // --- 边界检查 ---
                if (entityRightCol >= gp.maxWorldCol) {
                    entity.collisionOn = true;
                    return;
                }
                
                tileNum1 = gp.TileM.mapTileNum[entityRightCol][entityTopRow];
                tileNum2 = gp.TileM.mapTileNum[entityRightCol][entityBottomRow];
                if (gp.TileM.tile[tileNum1].collision || gp.TileM.tile[tileNum2].collision) {
                    entity.collisionOn = true;
                }
                break;
        }
    }

    public int checkObject(Entity entity, boolean player) {
        int index = 999;
        for (int i = 0; i < gp.obj.length; i++) {
            if (gp.obj[i] != null) {
                // Update the entity's solid area position
                entity.solidArea.x = entity.worldX + entity.solidArea.x;
                entity.solidArea.y = entity.worldY + entity.solidArea.y;
    
                // Update the object's solid area position
                if (gp.obj[i].solidArea != null) {
                    gp.obj[i].solidArea.x = gp.obj[i].worldX + gp.obj[i].solidArea.x;
                    gp.obj[i].solidArea.y = gp.obj[i].worldY + gp.obj[i].solidArea.y;
                }
    
                // Adjust the entity's solid area position based on its direction
                switch (entity.direction) {
                    case "up": entity.solidArea.y -= entity.speed; break;
                    case "down": entity.solidArea.y += entity.speed; break;
                    case "left": entity.solidArea.x -= entity.speed; break;
                    case "right": entity.solidArea.x += entity.speed; break;
                }
    
                // Check for collision
                if (gp.obj[i].solidArea != null && entity.solidArea.intersects(gp.obj[i].solidArea)) {
                    if (gp.obj[i].collision) {
                        entity.collisionOn = true;
                    }
                    if (player) {
                        index = i;
                        // house传送门逻辑
                        if (gp.obj[i] instanceof object.OBJ_House) {
                            object.OBJ_House house = (object.OBJ_House) gp.obj[i];
                            if (house.targetMap != null) {
                                gp.mapManager.switchToMap(house.targetMap);
                                gp.player.worldX = house.targetX;
                                gp.player.worldY = house.targetY;
                                if (gp.se != null) gp.playSE(3); // 播放door.wav音效
                            }
                        }
                    }
                }
                
                // Reset the solid area position for BOTH entity and object
                // 这两部分都应该在 if (gp.obj[i] != null) 内部
                entity.solidArea.x = entity.solidAreaDefaultX;
                entity.solidArea.y = entity.solidAreaDefaultY;
    
                // *** 核心修正：将这部分代码移入 if 块内 ***
                if (gp.obj[i].solidArea != null) {
                    gp.obj[i].solidArea.x = gp.obj[i].solidAreaDefaultX;
                    gp.obj[i].solidArea.y = gp.obj[i].solidAreaDefaultY;
                }
            }
        }
        return index;
    }
    public int checkEntity(Entity entity, Entity[] target_DEPRECATED){
        int index = 999;
        List<Entity> nearbyEntities = gp.spatialGrid.getNearbyEntities(entity);
        for (Entity target : nearbyEntities) {
            // 确保不与自身检测，且目标是我们感兴趣的类型（例如NPC）
            if (target != entity && target.name.equals("girl") || target.name.equals("trader")) {
                // Update the entity's solid area position
                entity.solidArea.x = entity.worldX + entity.solidArea.x;
                entity.solidArea.y = entity.worldY + entity.solidArea.y;
                // Update the object's solid area position
                target.solidArea.x = target.worldX + target.solidArea.x;
                target.solidArea.y = target.worldY + target.solidArea.y;

                // Adjust the entity's solid area position based on its direction
                switch (entity.direction) {
                    case "up":
                        entity.solidArea.y -= entity.speed;
                        break;
                    case "down":
                        entity.solidArea.y += entity.speed;
                        break;
                    case "left":
                        entity.solidArea.x -= entity.speed;
                        break;
                    case "right":
                        entity.solidArea.x += entity.speed;
                        break;
                }

                // Check for collision
                if (entity.solidArea.intersects(target.solidArea)) {
                    entity.collisionOn=true;
                    index = nearbyEntities.indexOf(target);
                }

                // Reset the solid area position
                entity.solidArea.x = entity.solidAreaDefaultX;
                entity.solidArea.y = entity.solidAreaDefaultY;
                target.solidArea.x = target.solidAreaDefaultX;
                target.solidArea.y = target.solidAreaDefaultY;
            }
        }
        return index;

    }
    public void checkPlayer(Entity entity){

        // Update the entity's solid area position
        entity.solidArea.x = entity.worldX + entity.solidArea.x;
        entity.solidArea.y = entity.worldY + entity.solidArea.y;
        // Update the object's solid area position
        gp.player.solidArea.x = gp.player.worldX + gp.player.solidArea.x;
        gp.player.solidArea.y = gp.player.worldY + gp.player.solidArea.y;

        // Adjust the entity's solid area position based on its direction
        switch (entity.direction) {
            case "up":
                entity.solidArea.y -= entity.speed;
                break;
            case "down":
                entity.solidArea.y += entity.speed;
                break;
            case "left":
                entity.solidArea.x -= entity.speed;
                break;
            case "right":
                entity.solidArea.x += entity.speed;
                break;
        }

        // Check for collision
        if (entity.solidArea.intersects(gp.player.solidArea)) {
            entity.collisionOn=true;
        }

        // Reset the solid area position
        entity.solidArea.x = entity.solidAreaDefaultX;
        entity.solidArea.y = entity.solidAreaDefaultY;
        gp.player.solidArea.x = gp.player.solidAreaDefaultX;
        gp.player.solidArea.y = gp.player.solidAreaDefaultY;


    }
}
