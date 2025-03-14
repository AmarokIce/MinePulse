package club.someoneice.minepulse.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import net.minecraft.network.chat.Component;

public class ConfigApi implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.literal("Mine Pulse Config"))
                    .setSavingRunnable(() -> ClientConfig.writeIn(ClientConfig.REVERSAL));
            ConfigCategory general = builder.getOrCreateCategory(Component.literal("Common"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            BooleanListEntry shouldAsk = entryBuilder.startBooleanToggle(Component.translatable("option.minepulse.shouldAsk"), ClientConfig.SHOULD_ASK)
                    .setDefaultValue(true)
                    .setSaveConsumer(ClientConfig::setShouldAsk)
                    .build();
            BooleanListEntry enableTree = entryBuilder.startBooleanToggle(Component.translatable("option.minepulse.enableTree"), ClientConfig.ENABLE_MINE_TREE)
                    .setDefaultValue(true)
                    .setSaveConsumer(ClientConfig::setEnableMineTree)
                    .build();
            BooleanListEntry enableOre = entryBuilder.startBooleanToggle(Component.translatable("option.minepulse.enableOre"), ClientConfig.ENABLE_MINE_ORE)
                    .setDefaultValue(true)
                    .setSaveConsumer(ClientConfig::setEnableMineOre)
                    .build();
            BooleanListEntry reversal = entryBuilder.startBooleanToggle(Component.translatable("option.minepulse.reversal"), ClientConfig.REVERSAL)
                    .setDefaultValue(false)
                    .setSaveConsumer(ClientConfig::setReversal)
                    .build();
            general.addEntry(shouldAsk)
                    .addEntry(enableTree)
                    .addEntry(enableOre)
                    .addEntry(reversal);

            return builder.build();
        };
    }
}
