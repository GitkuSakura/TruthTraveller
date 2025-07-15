package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.Paint;
import java.awt.LinearGradientPaint;
import java.util.LinkedList;
import java.util.Queue;

import object.OBJ_Heart;
import object.OBJ_Key;

public class UI {
    GamePanel gp;
    Graphics2D g2;
    Font pfst;
    public int commandNum = 0;
    public boolean messageOn;
    public String FPS = "";
    public String message = "";
    public int messageCounter = 0;
    public String currentDialogue;
    BufferedImage heartFull, heartHalf, heartEmpty;
    BufferedImage keyImage;
    BufferedImage swordImage;
    BufferedImage shieldImage;
    private final LinkedList<String> messageQueue = new LinkedList<>();
    private final int maxMessages = 10;
    private final int messageDisplayTime = 3600;
    private final LinkedList<Integer> messageTimers = new LinkedList<>();

    public boolean isChatOpen = false;
    public StringBuilder chatInput = new StringBuilder();
    public int inventoryRow = 0;
    public int inventoryCol = 0;

    private int charIndex = 0;
    private String combinedText = "";
    private int dialogueFrameCounter = 0;

    private static final BufferedImage PLACEHOLDER = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);

    public UI(GamePanel gp) {
        this.gp = gp;
        pfst = gp.handwrittenFont.deriveFont(Font.BOLD, 80f);
        OBJ_Key key = new OBJ_Key(gp);
        keyImage = key.image;
        OBJ_Heart heart = new OBJ_Heart(gp);
        heartFull = heart.image;
        heartHalf = heart.image2;
        heartEmpty = heart.image3;
        object.OBJ_Sword sword = new object.OBJ_Sword(gp);
        swordImage = sword.image;
        try {
            shieldImage = javax.imageio.ImageIO.read(
                    getClass().getClassLoader().getResourceAsStream("res/objects/shield.png"));
        } catch (Exception e) { shieldImage = null; }
    }

    public void showFPS(String text) {
        FPS = text;
    }

    public void addMessage(String msg) {
        messageQueue.addLast(msg);
        messageTimers.addLast(messageDisplayTime);
        if (messageQueue.size() > maxMessages) {
            messageQueue.removeFirst();
            messageTimers.removeFirst();
        }
    }

    @Deprecated
    public void showMessage(String text) {
        addMessage(text);
    }
    public void drawTitleScreen() {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setFont(pfst);
        String title = "Truth Traveller";
        int x = getXforCenteredText(title);
        int y = gp.tileSize * 3;
        g2.setColor(Color.GRAY);
        g2.drawString(title, x + 5, y + 5);
        g2.setColor(Color.WHITE);
        g2.drawString(title, x, y);

        x = gp.screenWidth / 2 - (gp.tileSize * 2) / 2;
        y += gp.tileSize * 2;
        BufferedImage playerImg = gp.player.down1 != null ? gp.player.down1 : PLACEHOLDER;
        g2.drawImage(playerImg, x, y, gp.tileSize * 2, gp.tileSize * 2, null);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 48f));

        String text = "NEW GAME";
        x = getXforCenteredText(text);
        y += gp.tileSize * 3.5;
        g2.drawString(text, x, y);
        if (commandNum == 0) {
            g2.drawString(">", x - gp.tileSize, y);
        }

        text = "LOAD GAME";
        x = getXforCenteredText(text);
        y += gp.tileSize;
        g2.drawString(text, x, y);
        if (commandNum == 1) {
            g2.drawString(">", x - gp.tileSize, y);
        }

        text = "QUIT";
        x = getXforCenteredText(text);
        y += gp.tileSize;
        g2.drawString(text, x, y);
        if (commandNum == 2) {
            g2.drawString(">", x - gp.tileSize, y);
        }
    }
    public void draw(Graphics2D g2) {
        this.g2 = g2;

        if (gp.gameState == gp.loadingState) {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 48f));
            g2.setColor(Color.WHITE);
            String text = "Loading...";
            int x = getXforCenteredText(text);
            int y = gp.screenHeight / 2;
            g2.drawString(text, x, y);
            return;
        }

        if (gp.gameState == gp.titleState) {
            drawTitleScreen();
            return;
        }
        else if (gp.gameState == gp.inventoryState) {
            drawInventoryAndCharacterScreen();
            return;
        }
        else if (gp.gameState == gp.playState) {
            g2.setFont(new Font("Dialog", Font.ITALIC, 20));
            g2.setColor(Color.white);
            if (gp.keyH.checkDrawTime) {
                g2.drawString(FPS, gp.tileSize / 2, gp.tileSize / 2);
            }
            drawPlayerLife();
            drawPlayerKeys();
            drawPlayerSwords();
            drawDayNightName();
        }
        else if (gp.gameState == gp.pauseState) {
            drawPlayerLife();
            drawPlayerKeys();
            drawPlayerSwords();
            drawPauseScreen();
            drawDayNightName();
        }
        else if (gp.gameState == gp.dialogueState) {
            drawPlayerLife();
            drawPlayerKeys();
            drawPlayerSwords();
            drawDialogueScreen();
            drawDayNightName();
        }
        else if (gp.gameState == gp.deathState) {
            drawDeathScreen();
        }
        else if (gp.gameState == gp.tradeState) {
            drawTradeScreen();
        }

        drawMessages();
        if (isChatOpen) {
            drawChatBar();
        }
        if (gp.showCharacterWindow) {
            drawCharacterStateScreen();
        }
    }

    public void drawDayNightName() {
        String[] dayNames = {"Dawn", "Morning", "Noon", "Afternoon", "Dusk", "Night"};
        String text = dayNames[gp.dayState];

        g2.setFont(gp.handwrittenFont.deriveFont(Font.PLAIN, 32f));

        int textLength = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        int x = gp.screenWidth - textLength - gp.tileSize + 10;
        int y = gp.screenHeight - gp.tileSize - 10;

        g2.setColor(new Color(0, 0, 0, 150));
        g2.drawString(text, x + 2, y + 2);

        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
    }

    public void drawTradeScreen() {
        int x = gp.tileSize * 4;
        int y = gp.tileSize * 2;
        int width = gp.screenWidth - gp.tileSize * 8;
        int height = gp.tileSize * 6;
        Tools drawer = Tools.getTools("drawSubWindow");
        drawer.execute(g2, x, y, width, height);

        g2.setFont(pfst.deriveFont(Font.BOLD, 40f));
        g2.setColor(Color.WHITE);
        String title = "商店 (ESC关闭)";
        int titleX = getXforCenteredText(title);
        int titleY = y + gp.tileSize;
        g2.drawString(title, titleX, titleY);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 28f));
        String coinText = "金币: " + gp.player.coin;
        int coinX = x + gp.tileSize/2 + 10;
        int coinY = y + gp.tileSize * 2;
        g2.drawString(coinText, coinX, coinY);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 28f));
        int itemY = coinY + gp.tileSize;
        String itemText;
        int price = 2;

        itemText = "盾牌 (价格: " + price + ")";
        g2.drawString(itemText, coinX, itemY);
        if (commandNum == 0) {
            g2.drawString(">", coinX - 20, itemY);
        }
        itemY += gp.tileSize;

        itemText = "治疗药水 (价格: " + price + ")";
        g2.drawString(itemText, coinX, itemY);
        if (commandNum == 1) {
            g2.drawString(">", coinX - 20, itemY);
        }
        itemY += gp.tileSize;

        itemText = "剑 (价格: " + price + ")";
        g2.drawString(itemText, coinX, itemY);
        if (commandNum == 2) {
            g2.drawString(">", coinX - 20, itemY);
        }
    }


    public void drawDialogueScreen() {
        int x = gp.tileSize * 2;
        int y = gp.tileSize / 2;
        int width = gp.screenWidth - (x * 2);
        int height = gp.tileSize * 3;
        Tools drawer = Tools.getTools("drawSubWindow");
        drawer.execute(g2, x, y, width, height);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 32F));
        x += gp.tileSize;
        y += gp.tileSize;

        if (currentDialogue != null && !currentDialogue.equals(combinedText)) {
            combinedText = currentDialogue;
            charIndex = 0;
            dialogueFrameCounter = 0;
        }

        if (charIndex < combinedText.length()) {
            dialogueFrameCounter++;
            int typingSpeed = 2;
            if (dialogueFrameCounter > typingSpeed) {
                charIndex++;
                dialogueFrameCounter = 0;
            }
        }

        String textToDisplay = combinedText.substring(0, charIndex);
        Tools txtDrawer = Tools.getTools("drawWrappedText");
        txtDrawer.execute(g2, textToDisplay, x, y, width - (gp.tileSize * 2));

        if (isDialogueAnimationFinished()) {
            int indicatorX = x + width - (gp.tileSize * 2);
            int indicatorY = y + height - gp.tileSize;
            g2.drawString("▼", indicatorX, indicatorY);
        }
    }

    public void finishDialogueAnimation() {
        if (combinedText != null) {
            charIndex = combinedText.length();
        }
    }

    public boolean isDialogueAnimationFinished() {
        return charIndex >= combinedText.length();
    }


    public void setCurrentDialogue(String dialogue) {
        this.currentDialogue = dialogue;
        addMessage(dialogue);
    }


    public void drawPauseScreen() {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setFont(pfst.deriveFont(Font.BOLD, 60f));
        g2.setColor(Color.WHITE);
        String text = "游戏暂停";
        int x = getXforCenteredText(text);
        int y = gp.screenHeight / 2 - gp.tileSize * 2;
        g2.drawString(text, x, y);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 40f));

        text = "继续游戏";
        x = getXforCenteredText(text);
        y += gp.tileSize * 2;
        g2.drawString(text, x, y);
        if (commandNum == 0) {
            g2.drawString(">", x - gp.tileSize, y);
        }

        text = "保存游戏";
        x = getXforCenteredText(text);
        y += gp.tileSize;
        g2.drawString(text, x, y);
        if (commandNum == 1) {
            g2.drawString(">", x - gp.tileSize, y);
        }

        text = "返回主菜单";
        x = getXforCenteredText(text);
        y += gp.tileSize;
        g2.drawString(text, x, y);
        if (commandNum == 2) {
            g2.drawString(">", x - gp.tileSize, y);
        }
    }

    public int getXforCenteredText(String text) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return gp.screenWidth / 2 - length / 2;
    }

    public void drawPlayerLife() {
        int x = 0;
        int y = gp.screenHeight - gp.tileSize;

        for (int i = 0; i < gp.player.maxLife / 2; i++) {
            if (i < gp.player.life / 2) {
                g2.drawImage(heartFull, x + i * gp.tileSize, y, gp.tileSize, gp.tileSize, null);
            } else if (i == gp.player.life / 2 && gp.player.life % 2 == 1) {
                g2.drawImage(heartHalf, x + i * gp.tileSize, y, gp.tileSize, gp.tileSize, null);
            } else {
                g2.drawImage(heartEmpty, x + i * gp.tileSize, y, gp.tileSize, gp.tileSize, null);
            }
        }
    }

    public void drawPlayerKeys() {
        if (gp.player.hasKey > 0) {
            int x = gp.screenWidth - gp.tileSize * 2;
            int y = gp.tileSize;

            g2.drawImage(keyImage, x, y, gp.tileSize, gp.tileSize, null);

            g2.setFont(new Font("Dialog", Font.BOLD, 20));
            g2.setColor(Color.WHITE);
            String keyCount = "x" + gp.player.hasKey;
            g2.drawString(keyCount, x + gp.tileSize + 5, y + gp.tileSize - 5);
        }
    }

    public void drawPlayerSwords() {
        if (gp.player.swordCount > 0) {
            int x = gp.screenWidth - gp.tileSize * 2;
            int y = gp.tileSize * 2;

            g2.drawImage(swordImage, x, y, gp.tileSize, gp.tileSize, null);

            g2.setFont(new Font("Dialog", Font.BOLD, 20));
            g2.setColor(Color.WHITE);
            String swordCount = "x" + gp.player.swordCount;
            g2.drawString(swordCount, x + gp.tileSize + 5, y + gp.tileSize - 5);
        }
    }

    public void drawDeathScreen() {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setFont(pfst.deriveFont(Font.BOLD, 80f));
        g2.setColor(Color.RED);
        String title = "YOU DIED";
        int x = getXforCenteredText(title);
        int y = gp.screenHeight / 2 - gp.tileSize * 2;
        g2.drawString(title, x, y);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 48f));
        g2.setColor(Color.WHITE);

        String text = "NEW GAME";
        x = getXforCenteredText(text);
        y += gp.tileSize * 2;
        g2.drawString(text, x, y);
        if (commandNum == 0) {
            g2.drawString(">", x - gp.tileSize, y);
        }

        text = "LOAD GAME";
        x = getXforCenteredText(text);
        y += gp.tileSize;
        g2.drawString(text, x, y);
        if (commandNum == 1) {
            g2.drawString(">", x - gp.tileSize, y);
        }
    }

    public void drawCharacterStateScreen() {
        int x = gp.tileSize + 4;
        int y = gp.tileSize * 2-40;
        int width = (int)(gp.tileSize * 4.5);
        int lineHeight = gp.tileSize - 14;
        int normalLines = 9;
        int imgLineHeight = gp.tileSize;
        int height = lineHeight * normalLines + imgLineHeight * 2 + 42;
        g2.setColor(Color.BLACK);
        g2.fillRoundRect(x, y, width, height, 30, 30);
        g2.setColor(new Color(255, 200, 200));
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x, y, width, height, 30, 30);

        g2.setFont(new Font("Dialog", Font.BOLD, 18));
        g2.setColor(Color.WHITE);
        int textX = x + 12;
        int valueX = x + width - 12;
        int textY = y + 32;
        g2.drawString("Level", textX, textY);
        drawRightAligned(g2, String.valueOf(gp.player.level), valueX, textY);
        textY += lineHeight;
        g2.drawString("Life", textX, textY);
        drawRightAligned(g2, gp.player.life + "/" + gp.player.maxLife, valueX, textY);
        textY += lineHeight;
        g2.drawString("Strength", textX, textY);
        drawRightAligned(g2, String.valueOf(gp.player.strength), valueX, textY);
        textY += lineHeight;
        g2.drawString("Dexterity", textX, textY);
        drawRightAligned(g2, String.valueOf(gp.player.dexterity), valueX, textY);
        textY += lineHeight;
        g2.drawString("Attack", textX, textY);
        drawRightAligned(g2, String.valueOf(gp.player.attack), valueX, textY);
        textY += lineHeight;
        g2.drawString("Defense", textX, textY);
        drawRightAligned(g2, String.valueOf(gp.player.defense), valueX, textY);
        textY += lineHeight;
        g2.drawString("Exp", textX, textY);
        drawRightAligned(g2, String.valueOf(gp.player.exp), valueX, textY);
        textY += lineHeight;
        g2.drawString("Next Level", textX, textY);
        drawRightAligned(g2, String.valueOf(gp.player.nextLevelExp), valueX, textY);
        textY += lineHeight;
        g2.drawString("Coin", textX, textY);
        drawRightAligned(g2, String.valueOf(gp.player.coin), valueX, textY);
        textY += lineHeight;
        g2.drawString("Weapon", textX, textY + imgLineHeight/2);
        if (gp.player.hasSword && swordImage != null) {
            int imgY = textY;
            int imgX = valueX - imgLineHeight + 8;
            g2.drawImage(swordImage, imgX, imgY, imgLineHeight-4, imgLineHeight-4, null);
        } else {
            drawRightAligned(g2, "无", valueX, textY + imgLineHeight/2);
        }
        textY += imgLineHeight;
        g2.drawString("Shield", textX, textY + imgLineHeight/2);
        if (gp.player.shieldCount > 0 && shieldImage != null) {
            int imgY = textY;
            int imgX = valueX - imgLineHeight + 8;
            g2.drawImage(shieldImage, imgX, imgY, imgLineHeight-4, imgLineHeight-4, null);
            g2.setFont(new Font("Dialog", Font.BOLD, 16));
            g2.setColor(Color.YELLOW);
            g2.drawString("x" + gp.player.shieldCount, imgX - 32, imgY + imgLineHeight/2 + 6);
            g2.setColor(Color.WHITE);
        } else {
            drawRightAligned(g2, "无", valueX, textY + imgLineHeight/2);
        }
    }

    private void drawRightAligned(Graphics2D g2, String text, int rightX, int y) {
        int strWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, rightX - strWidth, y);
    }

    private void drawMessages() {
        if (messageQueue.isEmpty()) return;
        int margin = 16;
        int lineHeight = 20;
        int x = margin;
        int chatBarY = gp.screenHeight - gp.tileSize - 40 - 8;
        int baseY = chatBarY - 12;
        g2.setFont(new Font("Dialog", Font.BOLD, 16));
        int count = Math.min(messageQueue.size(), maxMessages);
        for (int i = 0; i < count; i++) {
            int msgIdx = messageQueue.size() - 1 - i;
            String msg = messageQueue.get(msgIdx);
            int y = baseY - i * lineHeight;
            g2.setColor(new Color(0,0,0,180));
            g2.drawString(msg, x+2, y+2);
            g2.setColor(Color.YELLOW);
            g2.drawString(msg, x, y);
        }
    }

    public void updateMessages() {
        for (int i = 0; i < messageTimers.size(); i++) {
            messageTimers.set(i, messageTimers.get(i) - 1);
        }
        while (!messageTimers.isEmpty() && messageTimers.getFirst() <= 0) {
            messageTimers.removeFirst();
            messageQueue.removeFirst();
        }
    }

    public void drawChatBar() {
        int barWidth = gp.screenWidth - 80;
        int barHeight = 40;
        int x = 40;
        int y = gp.screenHeight - gp.tileSize - barHeight - 8;
        g2.setColor(new Color(0,0,0,200));
        g2.fillRoundRect(x, y, barWidth, barHeight, 18, 18);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Dialog", Font.PLAIN, 22));
        g2.drawString("聊天：" + chatInput.toString() + "_", x + 18, y + 28);
    }

    public void handleChatInput(char c) {
        if (c == 8) {
            if (chatInput.length() > 0) chatInput.deleteCharAt(chatInput.length() - 1);
        } else if (c == 10 || c == 13) {
            if (chatInput.length() > 0) {
                addMessage("[你] " + chatInput.toString());
                chatInput.setLength(0);
            }
        } else if (chatInput.length() < 40 && c >= 32 && c <= 126) {
            chatInput.append(c);
        }
    }

    public void drawInventoryAndCharacterScreen() {
        drawCharacterStateScreen();

        int slotSize = gp.tileSize + 8;
        int cols = 5;
        int rows = 3;
        int invWinWidth = gp.tileSize * 7;
        int invWinX = gp.screenWidth - invWinWidth - gp.tileSize;
        int invWinY = gp.tileSize;
        int startX = invWinX + 24;
        int startY = invWinY + 60;
        int invWinHeight = 60 + rows * slotSize + 24;
        Tools drawer = Tools.getTools("drawSubWindow");
        drawer.execute(g2, invWinX, invWinY, invWinWidth, invWinHeight);

        g2.setFont(new Font("Dialog", Font.BOLD, 28));
        g2.setColor(Color.WHITE);
        g2.drawString("背包 (Q关闭)", invWinX + 24, invWinY + 40);

        int itemIndex = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = startX + col * slotSize;
                int y = startY + row * slotSize;
                g2.setColor(new Color(255,255,255,60));
                g2.fillRoundRect(x, y, slotSize-4, slotSize-4, 10, 10);
                if (row == inventoryRow && col == inventoryCol) {
                    g2.setColor(Color.YELLOW);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawRoundRect(x, y, slotSize-4, slotSize-4, 10, 10);
                }
                if (itemIndex < gp.player.inventory.size()) {
                    entity.Item item = gp.player.inventory.get(itemIndex);
                    if (item.image != null) {
                        g2.drawImage(item.image, x+6, y+6, gp.tileSize-8, gp.tileSize-8, null);
                    }
                }
                itemIndex++;
            }
        }

        int descWinWidth = gp.tileSize * 7;
        int descWinHeight = gp.tileSize * 2;
        int descWinX = gp.screenWidth - descWinWidth - gp.tileSize;
        int descWinY = gp.screenHeight - descWinHeight - gp.tileSize;
        drawer.execute(g2, descWinX, descWinY, descWinWidth, descWinHeight);
        int selectedIndex = inventoryRow * cols + inventoryCol;
        if (selectedIndex < gp.player.inventory.size()) {
            entity.Item item = gp.player.inventory.get(selectedIndex);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Dialog", Font.BOLD, 22));
            g2.drawString("[" + item.name + "]", descWinX + 20, descWinY + 38);
            g2.setFont(new Font("Dialog", Font.PLAIN, 18));
            g2.drawString(item.description, descWinX + 20, descWinY + 68);
        } else {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Dialog", Font.BOLD, 22));
            g2.drawString("空", descWinX + 20, descWinY + 38);
        }
    }
}