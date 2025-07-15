package main;

import entity.*;
import object.*;
import main.MapManager.EntityData; // Import the nested EntityData class

/**
 * 实体工厂类 - 统一管理所有实体的创建
 */

public class EntityFactory {
    private GamePanel gp;

    public EntityFactory(GamePanel gp) {
        this.gp = gp;
    }

    /**
     * 创建NPC
     * --- MODIFIED: This method now accepts an EntityData object to handle dialogues ---
     */
    public Entity createNpc(EntityData data) {
        Entity npc = null;

        // Use the type from the data object
        switch (data.type.toLowerCase()) {
            case "girl":
                // Pass the dialogue list from the data object to the NPC's constructor
                npc = new NPC_Girl(gp, data.dialogues);
                break;
            case "trader": // --- ADDED: A case for the trader ---
                npc = new NPC_Trader(gp, data.dialogues);
                break;
            case "merchant":
                // npc = new NPC_Merchant(gp, data.dialogues);
                break;
            case "oldman":
                // npc = new NPC_OldMan(gp, data.dialogues);
                break;
            default:
                System.err.println("未知的NPC类型: " + data.type);
                return null;
        }

        if (npc != null) {
            // Set position from the data object
            npc.worldX = data.worldX;
            npc.worldY = data.worldY;
            // The name is typically set within the NPC's constructor for consistency,
            // but you could enforce it here if needed.
            // e.g., npc.name = data.type.toLowerCase();
        }

        return npc;
    }

    /**
     * 创建怪物
     * --- UNCHANGED: This method signature is still valid as used by MapManager ---
     */
    public Entity createMonster(String type, int worldX, int worldY, int maxHealth, int attackPower) {
        Entity monster = null;

        switch (type.toLowerCase()) {
            case "monster":
                monster = new Monster(gp, maxHealth, attackPower);
                break;
            case "goblin":
                // monster = new Goblin(gp, maxHealth, attackPower);
                break;
            case "skeleton":
                // monster = new Skeleton(gp, maxHealth, attackPower);
                break;
            default:
                System.err.println("未知的怪物类型: " + type);
                return null;
        }

        if (monster != null) {
            monster.worldX = worldX;
            monster.worldY = worldY;
            monster.name = type.toLowerCase(); // Setting the monster's name is useful
        }

        return monster;
    }

    /**
     * 创建对象
     * --- UNCHANGED: This method signature is still valid as used by MapManager ---
     */
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
                obj = new OBJ_HealingPotion(gp, parameter > 0 ? parameter : 2); // Use parameter as heal amount, default 2
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
            // --- ADDED: A case for the shield to be spawned by events ---
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
            // GOOD PRACTICE: Do not override the 'name' property here.
            // Object classes (like OBJ_Key) set their own specific names in their constructors.
            // Overriding it here would break logic that depends on the specific name (e.g., "Key").
        }

        return obj;
    }

    // The convenience overload methods are helpful and can remain as they are
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