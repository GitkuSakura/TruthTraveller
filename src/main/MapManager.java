package main;

import java.util.*;
import java.io.*;

/**
 * 地图管理器 - 提供灵活的地图配置和切换功能
 */

public class MapManager {
    private GamePanel gp;
    Map<String, MapData> maps;
    private MapData currentMap;
    private EntityFactory entityFactory;

    // 事件处理相关
    private Map<EventData, Boolean> eventStates; // 记录每个事件的状态
    private Map<EventData, Boolean> eventHappened; // 记录事件是否已发生

    public MapManager(GamePanel gp) {
        this.gp = gp;
        this.maps = new HashMap<>();
        this.entityFactory = new EntityFactory(gp);
        this.eventStates = new HashMap<>();
        this.eventHappened = new HashMap<>();
        initializeMaps();
    }

    /**
     * 地图数据类 - 包含地图的所有配置信息
     */
    public static class MapData {
        public String mapFile;
        public int playerStartX;
        public int playerStartY;
        public List<EntityData> npcs;
        public List<EntityData> monsters;
        public List<EntityData> objects;
        public List<EventData> events;
        public String mapName;
        public String description;
        public boolean dynamicSpawningEnabled; // <--- 新增此行

        public MapData(String mapName, String mapFile, int playerStartX, int playerStartY) {
            this.mapName = mapName;
            this.mapFile = mapFile;
            this.playerStartX = playerStartX;
            this.playerStartY = playerStartY;
            this.npcs = new ArrayList<>();
            this.monsters = new ArrayList<>();
            this.objects = new ArrayList<>();
            this.events = new ArrayList<>();
            this.dynamicSpawningEnabled = false; // <--- 新增此行，默认为 false
        }

        // 新增一个方法来开启此功能
        public MapData setDynamicSpawning(boolean enabled) {
            this.dynamicSpawningEnabled = enabled;
            return this;
        }
        public MapData addNpc(String type, int x, int y, String... dialogues) {
            npcs.add(new EntityData(type, x, y, dialogues));
            return this;
        }

        public MapData addMonster(int x, int y) {
            return addMonster(x, y, 4, 2); // 默认4点血量，2点攻击力
        }

        public MapData addMonster(int x, int y, int maxHealth) {
            return addMonster(x, y, maxHealth, 2); // 默认2点攻击力
        }

        public MapData addMonster(int x, int y, int maxHealth, int attackPower) {
            monsters.add(new EntityData("monster", x, y, maxHealth, attackPower));
            return this;
        }

        public MapData addObject(String type, int x, int y) {
            return addObject(type, x, y, 0); // 默认参数
        }

        public MapData addObject(String type, int x, int y, int parameter) {
            objects.add(new EntityData(type, x, y, 0, 0, parameter)); // 对象不需要血量和攻击力
            return this;
        }

        public MapData addEvent(String type, int x, int y) {
            events.add(new EventData(type, x, y));
            return this;
        }

        public MapData addEvent(EventData event) {
            events.add(event);
            return this;
        }
    }

    /**
     * 实体数据类 - 存储实体的位置和类型信息
     */
    public static class EntityData {
        public String type;
        public int worldX;
        public int worldY;
        public int maxHealth; // 血量参数
        public int attackPower; // 攻击力参数
        public int parameter; // 通用参数（用于药物回复量等）
        public String[] dialogues; // 新增字段

        public EntityData(String type, int x, int y) {
            this(type, x, y, 4, 2, 0); // 默认4点血量，2点攻击力，0参数
        }

        public EntityData(String type, int x, int y, int maxHealth) {
            this(type, x, y, maxHealth, 2, 0); // 默认2点攻击力，0参数
        }

        public EntityData(String type, int x, int y, int maxHealth, int attackPower) {
            this(type, x, y, maxHealth, attackPower, 0); // 默认0参数
        }

        public EntityData(String type, int x, int y, int maxHealth, int attackPower, int parameter) {
            this.type = type;
            this.worldX = x;
            this.worldY = y;
            this.maxHealth = maxHealth;
            this.attackPower = attackPower;
            this.parameter = parameter;
        }

        public EntityData(String type, int x, int y, String... dialogues) {
            this(type, x, y, 4, 2, 0); // 默认血量、攻击力、参数
            this.dialogues = dialogues;
        }
    }

    /**
     * 事件数据类 - 存储事件的位置、类型和配置信息
     */
    public static class EventData {
        public String type;
        public int worldX;
        public int worldY;
        public int width;
        public int height;
        public String requiredDirection;
        public boolean canReenter;
        public boolean oneTimeOnly;
        public Map<String, Object> parameters;

        public EventData(String type, int x, int y) {
            this.type = type;
            this.worldX = x;
            this.worldY = y;
            this.width = 48; // 默认事件区域大小
            this.height = 48;
            this.requiredDirection = "any";
            this.canReenter = true;
            this.oneTimeOnly = false;
            this.parameters = new HashMap<>();
        }

        public EventData setSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public EventData setDirection(String direction) {
            this.requiredDirection = direction;
            return this;
        }

        public EventData setReenter(boolean canReenter) {
            this.canReenter = canReenter;
            return this;
        }

        public EventData setOneTime(boolean oneTimeOnly) {
            this.oneTimeOnly = oneTimeOnly;
            return this;
        }

        public EventData addParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }
    }

    /**
     * 初始化所有地图配置
     */
    private void initializeMaps() {
        // 地图1 - 新手村
        MapData map01 = new MapData("新手村", "/res/maps/map01.txt",
                gp.tileSize * 3, gp.tileSize * 4)
                // --- 修改点 4: 在创建NPC时直接提供对话 ---
                .addNpc("girl", gp.tileSize * 10, gp.tileSize * 21,
                        "你好,MikuSakura",
                        "你果然还是来了啊。",
                        "和约好的一样，完成目的后就给我离开这。",
                        "祝你好运。",
                        "如果你被森林里的陷阱伤害，试着贴着边缘走。")
                .addMonster(gp.tileSize * 35, gp.tileSize * 6, 4, 2)
                .addObject("instructionBoard", -gp.tileSize * 4, gp.tileSize * 1)
                .addObject("healingPotion", gp.tileSize * 37, gp.tileSize * 25, 2)
                .addObject("key", gp.tileSize * 40, gp.tileSize * 47)
                .addObject("door", gp.tileSize * 37, gp.tileSize * 27)
                .addObject("sword", gp.tileSize * 24, gp.tileSize * 7)
                .addObject("chest", gp.tileSize * 37, gp.tileSize * 23)
                .addEvent(new EventData("playSound", gp.tileSize * 32, gp.tileSize * 16)
                        .setSize(48, 48).addParameter("soundIndex", 3))
                .addEvent(new EventData("itemSpawn", gp.tileSize * 37, gp.tileSize * 23)
                        .setSize(48, 48).setOneTime(true)
                        .addParameter("itemType", "shield")
                        .addParameter("message", "你获得了盾牌！防御力+1"))
                .addEvent(new EventData("damagePit", gp.tileSize * 34, gp.tileSize * 46)
                        .setSize(16, 16).addParameter("message", "你被吸进去了！"))
                .addEvent(new EventData("dialogue", gp.tileSize * 32, gp.tileSize * 6)
                        .setOneTime(true).addParameter("message", "这里有一把神秘的剑..."))
                .addEvent(new EventData("teleport", gp.tileSize * 32, gp.tileSize * 16)
                        .addParameter("targetMap", "map02")
                        .addParameter("targetX", gp.tileSize * 25)
                        .addParameter("targetY", gp.tileSize * 25))
                .addEvent(new EventData("monsterSpawn", gp.tileSize * 25, gp.tileSize * 7)
                        .setOneTime(true).addParameter("monsterType", "monster")
                        .addParameter("x", gp.tileSize * 32).addParameter("y", gp.tileSize * 6)
                        .addParameter("maxHealth", 6).addParameter("attackPower", 3))
                // --- 修改点 5: 更改事件参数以传递一个列表，而不是单个字符串 ---
                .addEvent(new EventData("changeDialogue", gp.tileSize * 25, gp.tileSize * 7)
                        .setOneTime(true).addParameter("targetNpc", "girl")
                        .addParameter("newDialogues", Arrays.asList(
                                "我真的没想到你能解决那些怪物...",
                                "我还以为你已经死了...",
                                "密道在一个隐蔽的角落")));

        MapData map02 = new MapData("神秘森林", "/res/maps/map02.txt",
                gp.tileSize * 25, gp.tileSize * 25)
                .addMonster(gp.tileSize * 13, gp.tileSize * 24, 6, 3) // 精英怪物，6点血量，3点攻击力
                .addMonster(gp.tileSize * 14, gp.tileSize * 38, 8, 3) // Boss怪物，8点血量，4点攻击力
                .addObject("key", gp.tileSize * 39, gp.tileSize * 34)
                .addObject("door", gp.tileSize * 14, gp.tileSize * 36)
                .addObject("sword", gp.tileSize * 14, gp.tileSize * 34)
                // 对话事件 - 古老石碑
                .addEvent(new EventData("dialogue", gp.tileSize * 36, gp.tileSize * 19)
                        .setSize(48, 48)
                        .setDirection("any")
                        .setReenter(true)
                        .setOneTime(true)
                        .addParameter("message", "这里有一块古老的石碑...")
                        .addParameter("spawnHouseAfter", true))
                // 石碑触发事件 - 生成NPC和怪物
                .addEvent(new EventData("stoneTablet", gp.tileSize * 36, gp.tileSize * 19)
                        .setSize(48, 48)
                        .setDirection("any")
                        .setReenter(false)
                        .setOneTime(true)
                        .addParameter("message", "Someone: Help!"))
                // 伤害陷阱事件 - 森林陷阱
                .addEvent(new EventData("damagePit", gp.tileSize * 40, gp.tileSize * 15)
                        .setSize(16, 16)
                        .setDirection("any")
                        .setReenter(true)
                        .setOneTime(false)
                        .addParameter("message", "你踩到了森林陷阱！"))
                // 物品生成事件 - 生成治疗药水
                .addEvent(new EventData("itemSpawn", gp.tileSize * 18, gp.tileSize * 17)
                        .setSize(48, 48)
                        .setDirection("any")
                        .setReenter(false)
                        .setOneTime(true)
                        .addParameter("itemType", "healingPotion")
                        .addParameter("x", gp.tileSize * 10)
                        .addParameter("y", gp.tileSize * 9)
                        .addParameter("parameter", 5)) // 回复3点生命值
                // 怪物生成事件 - 在特定位置生成怪物
                .addEvent(new EventData("monsterSpawn", gp.tileSize * 36, gp.tileSize * 19)
                        .setSize(48, 48)
                        .setDirection("any")
                        .setReenter(false)
                        .setOneTime(true)
                        .addParameter("monsterType", "monster")
                        .addParameter("x", gp.tileSize * 22)
                        .addParameter("y", gp.tileSize * 25)
                        .addParameter("maxHealth", 100)
                        .addParameter("attackPower", 6)) // 生成8点血量的Boss怪物

                .addMonster(gp.tileSize * 11, gp.tileSize * 18, 30) // 普通怪物，6点血量
                .addMonster(gp.tileSize * 18, gp.tileSize * 13, 30) // 普通怪物，6点血量
                .addMonster(gp.tileSize * 16, gp.tileSize * 15, 30) // 精英怪物，8点血量
                .addMonster(gp.tileSize * 16, gp.tileSize * 19, 30) // 精英怪物，8点血量
                .addEvent(new EventData("monsterSpawn", gp.tileSize * 36, gp.tileSize * 19)
                        .setSize(48, 48)
                        .setDirection("any")
                        .setReenter(false)
                        .setOneTime(true)
                        .addParameter("monsterType", "monster")
                        .addParameter("x", gp.tileSize * 25)
                        .addParameter("y", gp.tileSize * 28)
                        .addParameter("maxHealth", 100)
                        .addParameter("attackPower", 6));
        MapData map03 = new MapData("宝藏地图", "/res/maps/map03.txt",
                gp.tileSize * 25, gp.tileSize * 36)
                // --- MODIFIED: Changed "girl" to "trader" ---
                .addNpc("trader", gp.tileSize * 24, gp.tileSize * 35, "说实话，我不讨厌你。看看有什么需要的吧？")
                .addObject("chest", gp.tileSize * 30, gp.tileSize * 30) // 添加宝箱
                .addObject("sword", gp.tileSize * 35, gp.tileSize * 35) // 添加剑
                .addObject("healingPotion", gp.tileSize * 20, gp.tileSize * 20, 10)
                .setDynamicSpawning(true); // 超级药水，回复5点


        maps.put("map01", map01);
        maps.put("map02", map02);
        maps.put("map03", map03);
        currentMap = map01;
    }

    private void createNpc(EntityData data) {
        for (int i = 0; i < gp.npc.length; i++) {
            if (gp.npc[i] == null) {
                // --- 修改点 6: 将包含对话的data对象传递给工厂 ---
                gp.npc[i] = entityFactory.createNpc(data);
                break;
            }
        }
    }

    /**
     * 切换到指定地图
     */
    public void switchToMap(String mapKey) {
        MapData targetMap = maps.get(mapKey);
        if (targetMap == null) {
            System.err.println("地图不存在: " + mapKey);
            return;
        }
        loadMap(targetMap);
    }
    private void loadMap(MapData mapData) {
        clearAllEntities();
        gp.TileM.loadMap(mapData.mapFile);
        gp.player.worldX = mapData.playerStartX;
        gp.player.worldY = mapData.playerStartY;
        gp.player.direction = "down";
        for (EntityData npcData : mapData.npcs) { createNpc(npcData); }
        for (EntityData monsterData : mapData.monsters) { createMonster(monsterData); }
        for (EntityData objectData : mapData.objects) { createObject(objectData); }
        for (EventData event : mapData.events) {
            if ("teleport".equals(event.type)) {
                createObject(new EntityData("house", event.worldX, event.worldY, 0, 0));
            }
        }
        currentMap = mapData;
        initializeEventStates();
        gp.spawner.resetTimers();
    }
    private void initializeEventStates() {
        eventStates.clear(); eventHappened.clear();
        if (currentMap != null) {
            for (EventData event : currentMap.events) {
                eventStates.put(event, false);
                eventHappened.put(event, false);
            }
        }
    }
    public void checkEvents() {
        if (currentMap == null || gp.gameState != gp.playState) return;
        for (EventData event : currentMap.events) {
            if (isEventTriggered(event)) {
                if (shouldTriggerEvent(event)) triggerEvent(event);
            } else {
                if (eventStates.containsKey(event) && eventStates.get(event)) eventStates.put(event, false);
            }
        }
    }
    private void handleChangeDialogueEvent(EventData event) {
        String targetNpcType = (String) event.parameters.getOrDefault("targetNpc", "girl");
        // --- 修改点 7: 获取新的对话列表 ---
        List<String> newDialogues = (List<String>) event.parameters.get("newDialogues");

        if (newDialogues == null || newDialogues.isEmpty()) {
            System.err.println("ChangeDialogue event is missing 'newDialogues' parameter.");
            return;
        }

        // 查找指定类型的NPC并改变其对话
        for (int i = 0; i < gp.npc.length; i++) {
            if (gp.npc[i] != null && gp.npc[i].name.equals(targetNpcType)) {
                // --- 核心修复点 8: 创建一个全新的、干净的数组来替换旧的 ---
                // 1. 创建一个与Entity类中定义的大小相同的空数组
                String[] newDialogueArray = new String[gp.npc[i].dialogues.length];

                // 2. 将新的对话内容从List复制到新数组中
                for (int j = 0; j < newDialogues.size() && j < newDialogueArray.length; j++) {
                    newDialogueArray[j] = newDialogues.get(j);
                }

                // 3. 将NPC的对话数组引用指向这个全新的数组
                gp.npc[i].dialogues = newDialogueArray;

                // (可选) 显示一条消息确认变化
                gp.ui.addMessage("你感觉周围的气氛似乎改变了...");
                break; // 找到并修改后即可退出循环
            }
        }
    }
    private boolean isEventTriggered(EventData event) {
        int eventLeft = event.worldX - event.width / 2;
        int eventTop = event.worldY - event.height / 2;
        int eventRight = event.worldX + event.width / 2;
        int eventBottom = event.worldY + event.height / 2;
        boolean inArea = gp.player.worldX >= eventLeft && gp.player.worldX <= eventRight && gp.player.worldY >= eventTop && gp.player.worldY <= eventBottom;
        boolean directionMatch = event.requiredDirection.equals("any") || gp.player.direction.equals(event.requiredDirection);
        return inArea && directionMatch;
    }
    private boolean shouldTriggerEvent(EventData event) {
        if (!eventStates.containsKey(event)) eventStates.put(event, false);
        if (!eventHappened.containsKey(event)) eventHappened.put(event, false);
        boolean canTrigger = !eventStates.get(event);
        if (event.oneTimeOnly && eventHappened.get(event)) canTrigger = false;
        if (!event.canReenter && eventHappened.get(event)) canTrigger = false;
        return canTrigger;
    }
    private void triggerEvent(EventData event) {
        eventStates.put(event, true);
        eventHappened.put(event, true);
        switch (event.type) {
            case "damagePit": handleDamagePitEvent(event); break;
            case "teleport": handleTeleportEvent(event); break;
            case "dialogue": handleDialogueEvent(event); break;
            case "changeDialogue": handleChangeDialogueEvent(event); break;
            case "itemSpawn": handleItemSpawnEvent(event); break;
            case "monsterSpawn": handleMonsterSpawnEvent(event); break;
            case "stoneTablet": handleStoneTabletEvent(event); break;
            case "playSound": handlePlaySoundEvent(event); break;
            default: System.err.println("未知事件类型: " + event.type); break;
        }
    }
    private void handleDamagePitEvent(EventData event) {
        gp.gameState = gp.dialogueState;
        gp.ui.setCurrentDialogue((String) event.parameters.getOrDefault("message", "你掉进了陷阱！"));
        gp.player.life--;
        gp.currentNpc = -1;
    }
    private void handleTeleportEvent(EventData event) {
        int targetX = (Integer) event.parameters.getOrDefault("targetX", 0);
        int targetY = (Integer) event.parameters.getOrDefault("targetY", 0);
        String targetMap = (String) event.parameters.getOrDefault("targetMap", null);
        if (targetMap != null) switchToMap(targetMap);
        gp.player.worldX = targetX;
        gp.player.worldY = targetY;
        gp.playSE(3);
    }
    private void handleDialogueEvent(EventData event) {
        gp.gameState = gp.dialogueState;
        gp.ui.currentDialogue = (String) event.parameters.getOrDefault("message", "这里有什么东西...");
        gp.currentNpc = -1;
    }
    private void handleItemSpawnEvent(EventData event) {
        String itemType = (String) event.parameters.get("itemType");
        int x = (Integer) event.parameters.getOrDefault("x", event.worldX);
        int y = (Integer) event.parameters.getOrDefault("y", event.worldY);
        
        // 创建物品
        for (int i = 0; i < gp.obj.length; i++) {
            if (gp.obj[i] == null) {
                gp.obj[i] = entityFactory.createObject(itemType, x, y);
                break;
            }
        }
    }
    private void handleMonsterSpawnEvent(EventData event) {
        // 只生成怪物，不做任何消息提示
        String monsterType = (String) event.parameters.getOrDefault("monsterType", "monster");
        int x = (Integer) event.parameters.getOrDefault("x", 0);
        int y = (Integer) event.parameters.getOrDefault("y", 0);
        int maxHealth = (Integer) event.parameters.getOrDefault("maxHealth", 4);
        int attackPower = (Integer) event.parameters.getOrDefault("attackPower", 2);
        createMonster(new EntityData(monsterType, x, y, maxHealth, attackPower));
    }
    private void handleStoneTabletEvent(EventData event) {
        // 1. 显示剧情文本
        gp.gameState = gp.dialogueState;
        gp.ui.currentDialogue = (String) event.parameters.getOrDefault("message", "石碑上刻着奇怪的文字...");
        gp.currentNpc = -1;
        // 2. 生成girl NPC在(26,26)
        createNpc(new EntityData("girl", gp.tileSize * 26, gp.tileSize * 26, "谢谢你救了我！", "我以为你和他们一样...","抱歉，我给你看看真正的宝藏吧。"));
        // 3. 生成house传送门在(25,25)
        createObject(new EntityData("house", gp.tileSize * 25, gp.tileSize * 25, 0, 0, 0));
        // 4. 标记剧情触发，后续girl对话时检测怪物数量
    }
    private void handlePlaySoundEvent(EventData event) { /* ... */ }
    private void createMonster(EntityData data) {
        for (int i = 0; i < gp.monster.length; i++) {
            if (gp.monster[i] == null) {
                gp.monster[i] = entityFactory.createMonster(data.type, data.worldX, data.worldY, data.maxHealth, data.attackPower);
                break;
            }
        }
    }
    private void createObject(EntityData data) {
        for (int i = 0; i < gp.obj.length; i++) {
            if (gp.obj[i] == null) {
                gp.obj[i] = entityFactory.createObject(data.type, data.worldX, data.worldY, data.parameter);
                break;
            }
        }
    }
    private void clearAllEntities() {
        Arrays.fill(gp.npc, null);
        Arrays.fill(gp.monster, null);
        Arrays.fill(gp.obj, null);
    }
    public MapData getCurrentMap() { return currentMap; }

    /**
     * 获取所有可用地图
     */
    public Set<String> getAvailableMaps() {
        return maps.keySet();
    }

    /**
     * 添加新地图
     */
    public void addMap(String key, MapData mapData) {
        maps.put(key, mapData);
    }

    /**
     * 从配置文件加载地图
     */
    public void loadMapFromFile(String configFile) {
        // 这里可以实现从JSON或XML文件加载地图配置
        // 为了简化，暂时使用硬编码配置
    }

    /**
     * 保存地图配置到文件
     */
    public void saveMapToFile(String configFile) {
        // 这里可以实现将地图配置保存到JSON或XML文件
    }

    // 动态生成house传送门对象
    // public void spawnHousePortal(int worldX, int worldY, String targetMap, int targetX, int targetY) {
    //     for (int i = 0; i < gp.obj.length; i++) {
    //         if (gp.obj[i] == null) {
    //             object.OBJ_House house = new object.OBJ_House(gp);
    //             house.worldX = worldX;
    //             house.worldY = worldY;
    //             house.targetMap = targetMap;
    //             house.targetX = targetX;
    //             house.targetY = targetY;
    //             gp.obj[i] = house;
    //             break;
    //         }
    //     }
    // }

    public String getCurrentMapKey() {
        for (Map.Entry<String, MapData> entry : maps.entrySet()) {
            if (entry.getValue() == currentMap) {
                return entry.getKey();
            }
        }
        return null;
    }
}