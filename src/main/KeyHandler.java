package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;

public class KeyHandler implements KeyListener {
    GamePanel gp;
    public boolean upPressed, downPressed, leftPressed, rightPressed, enterPressed, spacePressed;
    public boolean fPressed, gPressed, hPressed, ePressed; // 添加更多功能键
    public boolean checkDrawTime =  false;
    public  KeyHandler(GamePanel gp){
        this.gp = gp;
    }
    @Override
    public void keyTyped(KeyEvent e) {
        if (gp.ui.isChatOpen) {
            char c = e.getKeyChar();
            gp.ui.handleChatInput(c);
        }
    }

    // 文件: main/KeyHandler.java

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        // F11全屏切换
        if (code == KeyEvent.VK_F11) {
            if (gp.window != null) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice gd = ge.getDefaultScreenDevice();
                if (gd.getFullScreenWindow() == null) {
                    gp.enterFullScreen();
                } else {
                    gp.exitFullScreen();
                }
            }
            return;
        }
        // 聊天栏优先级最高
        if (gp.ui.isChatOpen) {
            if (code == KeyEvent.VK_ESCAPE) {
                gp.ui.isChatOpen = false;
            }
            return;
        }
        // ------------------ 标题状态 (Title State) ------------------
        if (gp.gameState == gp.titleState) {
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                upPressed = true;
            }
            if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                downPressed = true;
            }
            if (code == KeyEvent.VK_F || code == KeyEvent.VK_ENTER) {
                enterPressed = true;
            }
        }
        // ------------------ 游戏进行状态 (Play State) ------------------
        else if (gp.gameState == gp.playState) {
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                upPressed = true;
            }
            if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                downPressed = true;
            }
            if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                leftPressed = true;
            }
            if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                rightPressed = true;
            }
            if (code == KeyEvent.VK_ESCAPE) { // 使用 ESC 键暂停
                gp.gameState = gp.pauseState;
                gp.ui.commandNum = 0; // 重置暂停菜单选择
            }
            if (code == KeyEvent.VK_F || code == KeyEvent.VK_ENTER) {
                enterPressed = true;
            }
            if (code == KeyEvent.VK_F5) { // 使用 F5 键保存游戏，避免与 WASD 移动冲突
                gp.saveGame();
            }
            if (code == KeyEvent.VK_F3) {
                checkDrawTime = !checkDrawTime; // 更简洁的写法
            }
            // 地图切换按键
            // if (code == KeyEvent.VK_F1) {
            //     gp.mapManager.switchToMap("map01");
            // }
            // if (code == KeyEvent.VK_F2) {
            //     gp.mapManager.switchToMap("map02");
            // }
            // if (code == KeyEvent.VK_F4) {
            //     gp.mapManager.switchToMap("map03");
            // }
            // 剑气发射按键
            if (code == KeyEvent.VK_E) {
                ePressed = true;
            }
            // 新增：Q键切换角色信息界面
            if (code == KeyEvent.VK_Q) {
                gp.showCharacterWindow = !gp.showCharacterWindow;
            }
            // 新增T键打开聊天栏
            if (code == KeyEvent.VK_T) {
                gp.ui.isChatOpen = true;
                gp.ui.chatInput.setLength(0);
            }
            // 新增：Q键切换到背包界面
            if (code == KeyEvent.VK_Q) {
                gp.gameState = gp.inventoryState;
                gp.ui.inventoryRow = 0;
                gp.ui.inventoryCol = 0;
            }
        }
        // ------------------ 背包界面状态 (Inventory State) ------------------
        else if (gp.gameState == gp.inventoryState) {
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                upPressed = true;
            }
            if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                downPressed = true;
            }
            if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                leftPressed = true;
            }
            if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                rightPressed = true;
            }
            if (code == KeyEvent.VK_F || code == KeyEvent.VK_ENTER) {
                enterPressed = true;
            }
            // Q键关闭背包，返回游戏
            if (code == KeyEvent.VK_Q) {
                gp.gameState = gp.playState;
                gp.showCharacterWindow = false;
            }
        }
        // ------------------ 暂停状态 (Pause State) ------------------
        else if (gp.gameState == gp.pauseState) {
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                upPressed = true;
            }
            if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                downPressed = true;
            }
            if (code == KeyEvent.VK_F || code == KeyEvent.VK_ENTER) {
                enterPressed = true;
            }
            if (code == KeyEvent.VK_ESCAPE) { // 再次按 ESC 键取消暂停
                gp.gameState = gp.playState;
            }
            // 注意：在暂停状态下，S键只用于菜单选择，不用于保存游戏
            // Q键切换角色信息窗口显示
            if (code == KeyEvent.VK_Q) {
                gp.showCharacterWindow = !gp.showCharacterWindow;
            }
        }
        // ------------------ 对话状态 (Dialogue State) ------------------
        else if (gp.gameState == gp.dialogueState) {
            if (code == KeyEvent.VK_F || code == KeyEvent.VK_ENTER) {
                // 注意：这里的逻辑应该由 Player.update() 处理，而不是直接改变状态
                // KeyHandler 只负责设置 enterPressed = true
                enterPressed = true;
            }
            // Q键切换角色信息窗口显示
            if (code == KeyEvent.VK_Q) {
                gp.showCharacterWindow = !gp.showCharacterWindow;
            }
        }
        // ------------------ 死亡状态 (Death State) ------------------
        else if (gp.gameState == gp.deathState) {
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                upPressed = true;
            }
            if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                downPressed = true;
            }
            if (code == KeyEvent.VK_F || code == KeyEvent.VK_ENTER) {
                enterPressed = true;
            }
            // Q键切换角色信息窗口显示
            if (code == KeyEvent.VK_Q) {
                gp.showCharacterWindow = !gp.showCharacterWindow;
            }
        }
        // --- 新增：交易状态 (Trade State) ---
        else if (gp.gameState == gp.tradeState) {
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                upPressed = true;
            }
            if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                downPressed = true;
            }
            if (code == KeyEvent.VK_F || code == KeyEvent.VK_ENTER) {
                enterPressed = true;
            }
            if (code == KeyEvent.VK_ESCAPE) { // ESC关闭交易窗口
                gp.gameState = gp.playState;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            upPressed = false;
        }
        else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            downPressed = false;
        }
        else if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
        else if (code == KeyEvent.VK_F || code == KeyEvent.VK_ENTER) {
            enterPressed = false;
        }
    }
}