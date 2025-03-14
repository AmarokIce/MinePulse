package club.someoneice.minepulse.core;

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

public final class ServerConfig {
    public static final Set<OreMark> ORE_MARKS = Sets.newHashSet();
    public static final Set<OreMark> TREE_MARKS = Sets.newHashSet();

    public static int maxSizeCache = 1024;

    public static void read() throws IOException {
        final File file = new File(FabricLoader.getInstance().getConfigDir().toFile(), "minepulse_common.json");
        checkFile(file);

        MapNode node = JSON.json.parse(file).asMapNodeOrEmpty();
        ArrayNode ores = node.get("ores").asArrayNodeOrEmpty();
        ArrayNode trees = node.get("trees").asArrayNodeOrEmpty();

        foreach(ores, ORE_MARKS);
        foreach(trees, TREE_MARKS);
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

        /* Mods */

        node.add(to("#c:ores/bauxite"));
        node.add(to("#c:ores/cinnabar"));
        node.add(to("#c:ores/galena"));
        node.add(to("#c:ores/iridium"));
        node.add(to("#c:ores/lead"));
        node.add(to("#c:ores/peridot"));
        node.add(to("#c:ores/pyrite"));
        node.add(to("#c:ores/ruby"));
        node.add(to("#c:ores/sapphire"));
        node.add(to("#c:ores/sheldonite"));
        node.add(to("#c:ores/silver"));
        node.add(to("#c:ores/sodalite"));
        node.add(to("#c:ores/sphalerite"));
        node.add(to("#c:ores/tin"));
        node.add(to("#c:ores/tungsten"));

        node.add(to("#c:desh_ores"));
        node.add(to("#c:storage_blocks/raw_desh"));
        node.add(to("#c:ice_shard_ores"));
        node.add(to("#c:ostrum_ores"));
        node.add(to("#c:calorite_ores"));

        if (FabricLoader.getInstance().isModLoaded("create")) {
            node.addAll(List.of(
                    to("#c:zinc_ores"),
                    to("#c:storage_blocks/raw_zinc")
            ));
        }

        if (FabricLoader.getInstance().isModLoaded("ad_astra")) {
            node.add(to("ad_astra:moon_cheese_ore"));
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
