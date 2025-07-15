package main;

import entity.*;
import object.*;
import main.MapManager.EntityData;

/**
 * 实体工厂类 - 统一管理所有实体的创建
 */

public class EntityFactory {
    private GamePanel gp;

    public EntityFactory(GamePanel gp) {
        this.gp = gp;
    }

    public Entity createNpc(EntityData data) {
        Entity npc = null;

        switch (data.type.toLowerCase()) {
            case "girl":
                npc = new NPC_Girl(gp, data.dialogues);
                break;
            case "trader":
                npc = new NPC_Trader(gp, data.dialogues);
                break;
            case "merchant":
                break;
            case "oldman":
                break;
            default:
                System.err.println("未知的NPC类型: " + data.type);
                return null;
        }

        if (npc != null) {
            npc.worldX = data.worldX;
            npc.worldY = data.worldY;
        }

        return npc;
    }

    public Entity createMonster(String type, int worldX, int worldY, int maxHealth, int attackPower) {
        Entity monster = null;

        switch (type.toLowerCase()) {
            case "monster":
                monster = new Monster(gp, maxHealth, attackPower);
                break;
            case "goblin":
                break;
            case "skeleton":
                break;
            default:
                System.err.println("未知的怪物类型: " + type);
                return null;
        }

        if (monster != null) {
            monster.worldX = worldX;
            monster.worldY = worldY;
            monster.name = type.toLowerCase();
        }

        return monster;
    }

    public Entity createObject(String type, int worldX, int worldY, int parameter) {
        Entity obj = null;

        switch (type.toLowerCase()) {
            case "key":
                obj = new OBJ_Key(gp);
                break;
            case "door":
                obj = new OBJ_Door(gp);
                break;
            case "healingpotion":
                obj = new OBJ_HealingPotion(gp, parameter > 0 ? parameter : 2);
                break;
            case "instructionboard":
                obj = new OBJ_InstructionBoard(gp);
                break;
            case "sword":
                obj = new OBJ_Sword(gp);
                break;
            case "heart":
                obj = new OBJ_Heart(gp);
                break;
            case "chest":
                obj = new OBJ_Chest(gp);
                break;
            case "house":
                obj = new object.OBJ_House(gp);
                break;
            case "shield":
                obj = new OBJ_Shield(gp);
                break;
            default:
                System.err.println("未知的对象类型: " + type);
                return null;
        }

        if (obj != null) {
            obj.worldX = worldX;
            obj.worldY = worldY;
        }

        return obj;
    }

    public Entity createMonster(String type, int worldX, int worldY) {
        return createMonster(type, worldX, worldY, 4, 2);
    }

    public Entity createMonster(String type, int worldX, int worldY, int maxHealth) {
        return createMonster(type, worldX, worldY, maxHealth, 2);
    }

    public Entity createObject(String type, int worldX, int worldY) {
        return createObject(type, worldX, worldY, 0);
    }
}