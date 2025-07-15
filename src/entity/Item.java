package entity;

import java.awt.image.BufferedImage;

public class Item {
    public BufferedImage image;
    public String name;
    public String description;
    public ItemType type;

    public enum ItemType {
        WEAPON, SHIELD, POTION, KEY, OTHER
    }

    public Item(BufferedImage image, String name, String description, ItemType type) {
        this.image = image;
        this.name = name;
        this.description = description;
        this.type = type;
    }
} 