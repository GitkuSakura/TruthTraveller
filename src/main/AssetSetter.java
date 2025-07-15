package main;

import entity.NPC_Girl;
import entity.Monster;
import object.OBJ_InstructionBoard; // 导入我们刚刚创建的新类
import object.OBJ_Key;
import object.OBJ_Door;



class NpcPlacement {
    String type; // NPC的类型, e.g., "girl", "oldman"
    int worldX;
    int worldY;

    public NpcPlacement(String type, int worldX, int worldY) {
        this.type = type;
        this.worldX = worldX;
        this.worldY = worldY;
    }
}

class MonsterPlacement {
    int worldX;
    int worldY;

    public MonsterPlacement(int worldX, int worldY) {
        this.worldX = worldX;
        this.worldY = worldY;
    }
}

public class AssetSetter {
    GamePanel gp;
    public AssetSetter(GamePanel gp){
        this.gp = gp;
    }

    public void setObject(){
        gp.obj[0] = new OBJ_InstructionBoard(gp);
        gp.obj[0].worldX = -gp.tileSize * 4;
        gp.obj[0].worldY = gp.tileSize * 1;
        
        // 添加治疗药水在位置(37, 25)
        gp.obj[1] = new object.OBJ_HealingPotion(gp);
        gp.obj[1].worldX = gp.tileSize * 37;
        gp.obj[1].worldY = gp.tileSize * 25;
        
        // 添加钥匙在位置(40, 47)
        gp.obj[2] = new OBJ_Key(gp);
        gp.obj[2].worldX = gp.tileSize * 40;
        gp.obj[2].worldY = gp.tileSize * 47;
        
        // 添加门在位置(37, 27)
        gp.obj[3] = new OBJ_Door(gp);
        gp.obj[3].worldX = gp.tileSize * 37;
        gp.obj[3].worldY = gp.tileSize * 27;
        

    }

    public void setNPC(){
        gp.npc[0] =  new NPC_Girl(gp);
        gp.npc[0].worldX = gp.tileSize * 10;
        gp.npc[0].worldY = gp.tileSize * 21;
    }
    
    public void setMonster(){
        // 添加怪物在位置(35, 6)
        gp.monster[0] = new Monster(gp);
        gp.monster[0].worldX = gp.tileSize * 35;
        gp.monster[0].worldY = gp.tileSize * 6;
    }
    
    public void setNpcs(java.util.List<NpcPlacement> placements) {
        for (int i = 0; i < placements.size(); i++) {
            if (i >= gp.npc.length) break; // 防止数组越界

            NpcPlacement placement = placements.get(i);

            // 根据类型创建不同的NPC实例
            switch (placement.type) {
                case "girl":
                    gp.npc[i] = new NPC_Girl(gp);
                    break;
                case "oldman":
                    // gp.npc[i] = new NPC_OldMan(gp); // 假设你有老人NPC
                    break;
                // 在这里可以添加更多case来处理不同类型的NPC
                default:
                    System.err.println("Unknown NPC type: " + placement.type);
                    continue; // 跳过未知的NPC类型
            }

            // 设置NPC的坐标
            gp.npc[i].worldX = placement.worldX;
            gp.npc[i].worldY = placement.worldY;
        }
    }
    
    public void setMonsters(java.util.List<MonsterPlacement> placements) {
        for (int i = 0; i < placements.size(); i++) {
            if (i >= gp.monster.length) break; // 防止数组越界

            MonsterPlacement placement = placements.get(i);

            // 创建怪物实例
            gp.monster[i] = new Monster(gp);
            gp.monster[i].worldX = placement.worldX;
            gp.monster[i].worldY = placement.worldY;
        }
    }
}