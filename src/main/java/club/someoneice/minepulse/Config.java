package club.someoneice.minepulse;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
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

public final class Config {
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public static final List<TagKey<Block>> TAGS = Lists.newArrayList();
    public static final List<Block> BLOCKS = Lists.newArrayList();

    public static void read() throws IOException {
        final File file = new File(FabricLoader.getInstance().getConfigDir().toFile(), "minepulse.json");
        if (!file.exists() || !file.isFile()) {
            file.createNewFile();
            write(file);
        }

        List<String> list = GSON.fromJson(Files.readString(file.toPath()), new TypeToken<>() {}.getType());
        list.forEach(it -> {
            if (it.startsWith("#")) {
                TAGS.add(TagKey.create(Registries.BLOCK, ResourceLocation.tryParse(it.substring(1))));
            } else {
                BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(it))
                        .ifPresent(b -> BLOCKS.add(b.value()));
            }
        });
    }

    private static void write(File file) throws IOException {
        List<String> datas = Lists.newArrayList(
                "#minecreaft:coal_ores",
                "#minecreaft:copper_ores",
                "#c:raw_copper_blokc",
                "#minecreaft:iron_ores",
                "#c:raw_iron_block",
                "#minecreaft:gold_ores",
                "#c:raw_gold_block",
                "#minecreaft:redstone_ores",
                "#minecreaft:diamond_ores",
                "#minecreaft:emerald_ores",
                "#minecreaft:lapis_ores",
                "minecreaft:nether_quartz_ore",
                "minecreaft:ancient_debris",
                "minecreaft:mushroom_stem",
                "minecreaft:red_mushroom_block",
                "minecreaft:brown_mushroom_block",
                "#minecraft:logs",
                "#minecraft:leaves",
                "#minecreaft:wart_blocks"
        );

        if (FabricLoader.getInstance().isModLoaded("create")) {
            datas.addAll(List.of(
                    "#c:zinc_ores",
                    "#c:raw_zinc_block"
            ));
        }

        if (FabricLoader.getInstance().isModLoaded("techreborn")) {
            datas.addAll(List.of(
                    "#techreborn:ores",
                    "#c:raw_sliver_blocks",
                    "#c:raw_iridium_blocks",
                    "#c:raw_tin_blocks",
                    "#c:raw_lead_blocks",
                    "#c:raw_tungsten_blocks"
            ));
        }

        if (FabricLoader.getInstance().isModLoaded("ad_astra ")) {
            datas.addAll(List.of(
                    "ad_astra:moon_cheese_ore",
                    "#c:desh_ores",
                    "#c:raw_desh_blocks",
                    "#c:ice_shard_ores",
                    "#c:ostrum_ores",
                    "#c:calorite_ores"
            ));
        }

        if (FabricLoader.getInstance().isModLoaded("anvilcraft ")) {
            datas.addAll(List.of(
                    "#c:tin_ores",
                    "#c:titanium_ores",
                    "#c:tungsten_ores",
                    "#c:lead_ores",
                    "#c:silver_ores",
                    "#c:uranium_ores",
                    "anvilcraft:void_stone",
                    "anvilcraft:earth_core_shard_ore"
            ));
        }

        if (FabricLoader.getInstance().isModLoaded("ae2")) {
            datas.add("#c:certus_quartz_ores");
        }

        String out = GSON.toJson(datas);
        Files.write(file.toPath(), out.getBytes());
    }
}


