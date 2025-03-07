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

    public static boolean enableOre;
    public static boolean enableTree;


    public static void read() throws IOException {
        final File file = new File(FabricLoader.getInstance().getConfigDir().toFile(), "minepulse.json");
        if (!file.exists() || !file.isFile()) {
            file.createNewFile();
            write(file);
        }

        MapNode node = JSON.json.parse(file).asMapNodeOrEmpty();
        ArrayNode ores = node.get("ores").asArrayNodeOrEmpty();
        ArrayNode trees = node.get("trees").asArrayNodeOrEmpty();

        enableOre = (boolean) node.get("enableOreMine").asTypeNode().getObj();
        enableTree = (boolean) node.get("enableTreeMine").asTypeNode().getObj();

        for (JsonNode<?> ore : ores) {
            if (ore.getType() == JsonNode.NodeType.String) {
                ORE_MARKS.add(make(ore));
                continue;
            }

            if (ore.getType() == JsonNode.NodeType.Array) {
                Set<TagKey<Block>> tags = Sets.newHashSet();
                Set<Block> blocks = Sets.newHashSet();
                ore.asArrayNodeOrEmpty().forEach(it -> {
                    String dat = it.toString();
                    if (dat.startsWith("#")) {
                        tags.add(TagKey.create(Registries.BLOCK, ResourceLocation.tryParse(dat.substring(1))));
                    } else {
                        blocks.add(BuiltInRegistries.BLOCK.getValue(ResourceLocation.tryParse(dat)));
                    }
                });

                ORE_MARKS.add(new OreMark(tags, blocks));
            }
        }

        for (JsonNode<?> tree: trees) {
            if (tree.getType() == JsonNode.NodeType.String) {
                TREE_MARKS.add(make(tree));
                continue;
            }

            if (tree.getType() == JsonNode.NodeType.Array) {
                Set<TagKey<Block>> tags = Sets.newHashSet();
                Set<Block> blocks = Sets.newHashSet();
                tree.asArrayNodeOrEmpty().forEach(it -> {
                    String dat = it.toString();
                    if (dat.startsWith("#")) {
                        tags.add(TagKey.create(Registries.BLOCK, ResourceLocation.tryParse(dat.substring(1))));
                    } else {
                        blocks.add(BuiltInRegistries.BLOCK.getValue(ResourceLocation.tryParse(dat)));
                    }
                });

                TREE_MARKS.add(new OreMark(tags, blocks));
            }
        }
    }

    private static OreMark make(JsonNode<?> ore) {
        String dat = ore.toString();
        return dat.startsWith("#")
                ? new OreMark(Set.of(TagKey.create(Registries.BLOCK,
                ResourceLocation.tryParse(dat.substring(1)))), Set.of())
                : new OreMark(Set.of(), Set.of(BuiltInRegistries.BLOCK.getValue(ResourceLocation.tryParse(dat))));
    }

    private static StringNode to(String str) {
        return new StringNode(str);
    }

    private static void write(File file) throws IOException {
        // JsonArray array = new JsonArray();

        ArrayNode node = new ArrayNode();
        node.add(to("#minecreaft:coal_ores"));
        node.add(new ArrayNode(List.of(to("#minecreaft:copper_ores"), to("#c:raw_copper_block"))));
        node.add(new ArrayNode(List.of(to("#minecreaft:iron_ores"), to("#c:raw_iron_block"))));
        node.add(new ArrayNode(List.of(to("#minecreaft:gold_ores"), to("#c:raw_gold_block"))));

        node.add(to("#minecreaft:redstone_ores"));
        node.add(to("#minecreaft:diamond_ores"));
        node.add(to("#minecreaft:emerald_ores"));
        node.add(to("#minecreaft:lapis_ores"));
        node.add(to("#minecreaft:nether_quartz_ore"));
        node.add(to("#minecreaft:ancient_debris"));

        node.add(new ArrayNode(List.of(to("#minecreaft:mushroom_stem"),
                to("#minecreaft:red_mushroom_block"),
                to("#minecreaft:brown_mushroom_block")
        )));

        ArrayNode treeNode = new ArrayNode(List.of(new ArrayNode(List.of(
                to("#minecraft:logs"),
                to("#minecraft:leaves"),
                to("#minecreaft:wart_blocks")
        ))));

        if (FabricLoader.getInstance().isModLoaded("create")) {
            node.addAll(List.of(
                    to("#c:zinc_ores"),
                    to("#c:raw_zinc_block")
            ));
        }

        if (FabricLoader.getInstance().isModLoaded("techreborn")) {
            node.addAll(List.of(
                    to("#techreborn:ores"),
                    to("#c:raw_sliver_blocks"),
                    to("#c:raw_iridium_blocks"),
                    to("#c:raw_tin_blocks"),
                    to("#c:raw_lead_blocks"),
                    to("#c:raw_tungsten_blocks")
            ));
        }

        if (FabricLoader.getInstance().isModLoaded("ad_astra")) {
            node.addAll(List.of(
                    to("ad_astra:moon_cheese_ore"),
                    to("#c:desh_ores"),
                    to("#c:raw_desh_blocks"),
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

        String data = JsonBuilder.prettyPrint(out);
        Files.write(file.toPath(), data.getBytes());
    }
}


