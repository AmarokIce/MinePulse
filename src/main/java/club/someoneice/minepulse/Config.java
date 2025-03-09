package club.someoneice.minepulse;

import club.someoneice.json.JSON;
import club.someoneice.json.node.*;
import club.someoneice.json.processor.JsonBuilder;
import com.google.common.collect.Sets;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

public final class Config {
    public static final Set<OreMark> ORE_MARKS = Sets.newHashSet();
    public static final Set<OreMark> TREE_MARKS = Sets.newHashSet();

    public static boolean enableOre = true;
    public static boolean enableTree = true;
    public static boolean reversalShiftEnable = false;
    public static int maxSizeCache = 1024;

    public static void read() throws IOException {
        final File file = new File(FabricLoader.getInstance().getConfigDir().toFile(), "minepulse.json");
        checkFile(file);

        MapNode node = JSON.json.parse(file).asMapNodeOrEmpty();
        ArrayNode ores = node.get("ores").asArrayNodeOrEmpty();
        ArrayNode trees = node.get("trees").asArrayNodeOrEmpty();

        foreach(ores, ORE_MARKS);
        foreach(trees, TREE_MARKS);

        enableOre = node.has("enableOreMine")
                ? Boolean.parseBoolean(node.get("enableOreMine").toString())
                : enableOre;
        enableTree = node.has("enableTreeMine")
                ? Boolean.parseBoolean(node.get("enableTreeMine").toString())
                : enableTree;
        reversalShiftEnable = node.has("reversalShiftEnable")
                ? Boolean.parseBoolean(node.get("reversalShiftEnable").toString())
                : enableTree;
        maxSizeCache = node.has("maxSizeCache")
                ? (int) node.get("maxSizeCache").getObj()
                : maxSizeCache;
    }

    private static void write(File file) throws IOException {
        // JsonArray array = new JsonArray();

        ArrayNode node = new ArrayNode();
        node.add(to("#minecraft:coal_ores"));
        node.add(new ArrayNode(List.of(to("#minecraft:copper_ores"), to("#c:storage_blocks/raw_copper"))));
        node.add(new ArrayNode(List.of(to("#minecraft:iron_ores"), to("#c:storage_blocks/raw_iron"))));
        node.add(new ArrayNode(List.of(to("#minecraft:gold_ores"), to("#c:storage_blocks/raw_gold"))));

        node.add(to("#minecraft:redstone_ores"));
        node.add(to("#minecraft:diamond_ores"));
        node.add(to("#minecraft:emerald_ores"));
        node.add(to("#minecraft:lapis_ores"));
        node.add(to("#minecraft:nether_quartz_ore"));
        node.add(to("#minecraft:ancient_debris"));

        node.add(to("#c:ores/netherite_scrap"));

        node.add(new ArrayNode(List.of(
                to("minecraft:mushroom_stem"),
                to("minecraft:red_mushroom_block"),
                to("minecraft:brown_mushroom_block")
        )));

        ArrayNode treeNode = new ArrayNode(List.of(
                to("#minecraft:logs")
        ));

        if (FabricLoader.getInstance().isModLoaded("create")) {
            node.addAll(List.of(
                    to("#c:zinc_ores"),
                    to("#c:storage_blocks/raw_zinc")
            ));
        }

        if (FabricLoader.getInstance().isModLoaded("techreborn")) {
            node.addAll(List.of(
                    to("#c:ores/bauxite"),
                    to("#c:ores/cinnabar"),
                    to("#c:ores/galena"),
                    to("#c:ores/iridium"),
                    to("#c:ores/lead"),
                    to("#c:ores/peridot"),
                    to("#c:ores/pyrite"),
                    to("#c:ores/ruby"),
                    to("#c:ores/sapphire"),
                    to("#c:ores/sheldonite"),
                    to("#c:ores/silver"),
                    to("#c:ores/sodalite"),
                    to("#c:ores/sphalerite"),
                    to("#c:ores/tin"),
                    to("#c:ores/tungsten")
            ));
        }

        if (FabricLoader.getInstance().isModLoaded("ad_astra")) {
            node.addAll(List.of(
                    to("ad_astra:moon_cheese_ore"),
                    to("#c:desh_ores"),
                    to("#c:storage_blocks/raw_desh"),
                    to("#c:ice_shard_ores"),
                    to("#c:ostrum_ores"),
                    to("#c:calorite_ores")
            ));
        }

        if (FabricLoader.getInstance().isModLoaded("anvilcraft")) {
            node.addAll(List.of(
                    to("#c:tin_ores"),
                    to("#c:titanium_ores"),
                    to("#c:tungsten_ores"),
                    to("#c:lead_ores"),
                    to("#c:silver_ores"),
                    to("#c:uranium_ores"),
                    to("anvilcraft:void_stone"),
                    to("anvilcraft:earth_core_shard_ore")
            ));
        }

        if (FabricLoader.getInstance().isModLoaded("ae2")) {
            node.add(to("#c:certus_quartz_ores"));
        }

        MapNode out = new MapNode();
        out.put("ores", node);
        out.put("trees", treeNode);
        out.put("enableOreMine", new BooleanNode(true));
        out.put("enableTreeMine", new BooleanNode(true));
        out.put("reversalShiftEnable", new BooleanNode(false));
        out.put("maxSizeCache", new IntegerNode(1024));

        String data = JsonBuilder.prettyPrint(out);
        Files.write(file.toPath(), data.getBytes());
    }

    private static void checkFile(File file) throws IOException {
        if (file.exists() && file.isFile()) {
            return;
        }

        if (!file.createNewFile()) {
            throw new RuntimeException("Cannot create the config file!");
        }

        write(file);
    }

    private static void foreach(ArrayNode nodeArray, Set<OreMark> mark) {
        for (JsonNode<?> node: nodeArray) {
            mark.add(node.getType() == JsonNode.NodeType.String
                    ? make((StringNode) node.asTypeNode())
                    : make(node.asArrayNodeOrEmpty())
            );
        }
    }

    private static OreMark make(ArrayNode node) {
        Set<TagKey<Block>> tags = Sets.newHashSet();
        Set<Block> blocks = Sets.newHashSet();
        node.forEach(it -> {
            String dat = it.toString();
            if (dat.startsWith("#")) {
                tags.add(createTag(dat));
            } else {
                blocks.add(createBlock(dat));
            }
        });

        return new OreMark(tags, blocks);
    }

    private static OreMark make(StringNode ore) {
        String dat = ore.toString();
        return dat.startsWith("#")
                ? new OreMark(Set.of(createTag(dat)), Set.of())
                : new OreMark(Set.of(), Set.of(createBlock(dat)));
    }

    private static TagKey<Block> createTag(String str) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.tryParse(str.substring(1)));
    }

    private static Block createBlock(String str) {
        return BuiltInRegistries.BLOCK.getValue(ResourceLocation.tryParse(str));
    }

    private static StringNode to(String str) {
        return new StringNode(str);
    }
}
