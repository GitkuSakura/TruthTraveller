package main;

import entity.Player;
import java.awt.Rectangle;
public class EventHandler {
    
    GamePanel gp;
    Rectangle eventRect;
    int eventRectDefaultX, eventRectDefaultY;
    Player player;
    boolean eventHappened = false; // 标记事件是否已经发生
    boolean canReenter = true; // 标记是否可以重新进入触发区域
    
    EventHandler(GamePanel gp, Player player){
        this.gp = gp;
        this.player = player;
        eventRect = new Rectangle();
        // 事件位置：瓦片(7,3)的正中心（对应地图坐标的第8行第4列）
        eventRect.x = 7;
        eventRect.y = 3;
        // 事件区域大小：瓦片中心的一个小区域
        eventRect.width = 1;
        eventRect.height = 1;
        eventRectDefaultX = eventRect.x;
        eventRectDefaultY = eventRect.y;
    }
    
    public void checkEvent() {
        // 只在游戏进行状态检查事件，避免在对话状态重复触发
        if (gp.gameState == gp.playState) {
            if(hit(34, 46, "any")) {
                if (!eventHappened && canReenter) {
                    // 第一次触发事件
                    damagePit(gp.dialogueState);
                    eventHappened = true;
                    canReenter = false;
                }
            } else {
                // 玩家离开了触发区域，重置状态
                if (eventHappened) {
                    canReenter = true;
                    eventHappened = false;
                }
            }
        }
    }
    public boolean hit(int eventCol, int eventRow,String reqDirection) {
        boolean hit = false;
        gp.player.solidArea.x = gp.player.worldX + gp.player.solidArea.x;
        gp.player.solidArea.y = gp.player.worldY + gp.player.solidArea.y;
        
        // 计算事件区域的世界坐标：瓦片中心位置
        int eventWorldX = eventCol * gp.tileSize + gp.tileSize / 2;
        int eventWorldY = eventRow * gp.tileSize + gp.tileSize / 2;
        
        // 设置事件碰撞区域（瓦片中心的一个小区域）
        eventRect.x = eventWorldX - eventRect.width / 2;
        eventRect.y = eventWorldY - eventRect.height / 2;
        
        if(gp.player.solidArea.intersects(eventRect)){
            if(gp.player.direction.equals(reqDirection) || reqDirection.equals("any")){
                hit = true;
            }
        }
        
        gp.player.solidArea.x = gp.player.solidAreaDefaultX;
        gp.player.solidArea.y = gp.player.solidAreaDefaultY;
        eventRect.x = eventRectDefaultX;
        eventRect.y = eventRectDefaultY;
        return hit;
    }
    public void damagePit(int gameState) {
        gp.gameState = gameState;
        gp.ui.currentDialogue = "You got sucked!";
        gp.player.life--;
        gp.currentNpc = -1; // 确保没有NPC被选中，避免NPC对话覆盖事件对话
    }
}
