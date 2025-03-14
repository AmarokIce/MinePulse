package club.someoneice.minepulse.client;

import club.someoneice.json.JSON;
import club.someoneice.json.node.MapNode;
import club.someoneice.json.processor.JsonBuilder;
import com.google.common.io.Files;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;

public class ClientConfig {
    public static boolean SHOULD_ASK = true;
    public static boolean ENABLE_MINE_ORE = true;
    public static boolean ENABLE_MINE_TREE = true;
    public static boolean REVERSAL = false;

    static final File file = new File(FabricLoader.getInstance().getConfigDir().toFile(), "minepulse_client.json");

    public static void init() {
        boolean flag = !file.exists() || !file.isFile();
        if (flag) {
            writeIn(true);
            return;
        }

        MapNode node = JSON.json.parse(file).asMapNodeOrEmpty();
        if (node.isEmpty()) {
            SHOULD_ASK = true;
            return;
        }

        SHOULD_ASK = node.has("should_ask")
                ? (boolean) node.get("should_ask").getObj()
                : SHOULD_ASK;
        ENABLE_MINE_ORE = node.has("enable_mine_ore")
                ? (boolean) node.get("enable_mine_ore").getObj()
                : ENABLE_MINE_TREE;
        ENABLE_MINE_TREE = node.has("enable_mine_tree")
                ? (boolean) node.get("enable_mine_tree").getObj()
                : ENABLE_MINE_TREE;
        REVERSAL = node.has("reversal")
                ? (boolean) node.get("reversal").getObj()
                : REVERSAL;
    }

    public static void writeIn(boolean flag) {
        REVERSAL = flag;
        try {
            file.createNewFile();
            MapNode node = new MapNode();
            node.put("should_ask", SHOULD_ASK);
            node.put("enable_mine_ore", ENABLE_MINE_ORE);
            node.put("enable_mine_tree", ENABLE_MINE_TREE);
            node.put("reversal", REVERSAL);
            String out = JsonBuilder.prettyPrint(node);
            Files.write(out.getBytes(), file);
        } catch(IOException e) {
            throw new RuntimeException("Cannot creat the new config for client!");
        }
    }

    public static void setShouldAsk(boolean shouldAsk) {
        SHOULD_ASK = shouldAsk;
    }

    public static void setEnableMineOre(boolean enableMineOre) {
        ENABLE_MINE_ORE = enableMineOre;
    }

    public static void setEnableMineTree(boolean enableMineTree) {
        ENABLE_MINE_TREE = enableMineTree;
    }

    public static void setReversal(boolean reversal) {
        REVERSAL = reversal;
    }
}
