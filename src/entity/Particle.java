package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import main.GamePanel;

public class Particle extends Entity{
    public boolean alive = true;
    Object generator;
    Color color;
    int size;
    double vx;
    double vy;
    double gravity = 0.4; // 重力加速度
    public Particle(GamePanel gp, Object generator, Color color, int size, double vx, double vy, int maxLife) {
        super(gp);
        this.generator = generator;
        this.color = color;
        this.vx = vx;
        this.vy = vy;
        this.size = size;
        life = maxLife;
        if (generator instanceof Entity) {
            worldX = ((Entity)generator).worldX;
            worldY = ((Entity)generator).worldY;
        }
    }

    public void update() {
        life--;
        vx *= 0.98; // 空气阻力
        vy += gravity; // 重力
        worldX += vx;
        worldY += vy;
        if (life < 0) {
            alive = false;
        }
    }
    public void draw(Graphics2D g2){
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;
        g2.setColor(color);
        g2.fillRect(screenX, screenY, size, size);
    }
}
