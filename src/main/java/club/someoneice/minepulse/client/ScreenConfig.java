package club.someoneice.minepulse.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public class ScreenConfig extends Screen {
    private static final ResourceLocation RL = ResourceLocation.tryParse("minepulse:ui.png");
    public ScreenConfig() {
        super(Component.literal("Mine Pulse Config"));
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg, mouseX, mouseY, partialTick);

        final int baseX = this.width / 2;
        final int baseY = this.height /2;

        final int height = baseY - 176 / 2;

        final int baseAX = baseX - 5;
        final int rangeA = baseAX - 176;

        final int baseBX = baseX + 5;
        final int rangeB = baseAX + 176;

        final boolean flag = mouseY > height && mouseY < height + 176;
        final boolean flagA = flag && mouseX > rangeA && mouseX < baseAX;
        final boolean flagB = flag && mouseX > baseBX && mouseX < rangeB;

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        gg.drawCenteredString(this.minecraft.font, Component.translatable("screen.minepulse.choose"), baseX, height - 20, Color.WHITE.getRGB());
        gg.drawCenteredString(this.minecraft.font, Component.translatable("screen.minepulse.config"), baseX, height - 10, Color.WHITE.getRGB());
        gg.blit(RenderType::guiTextured, RL, rangeA, height, flagA ? 175 : 0, 0, 175, 175, 512, 512);
        gg.blit(RenderType::guiTextured, RL, baseBX, height, flagB ? 175 : 0, 0, 175, 175, 512, 512);

        gg.blit(RenderType::guiTextured, RL, rangeA + 48, height + 48, 0, 176, 80, 96, 512, 512);
        gg.blit(RenderType::guiTextured, RL, baseBX + 48, height + 48, 81, 177, 80, 96, 512, 512);

        if (flagA) {
            gg.renderTooltip(this.minecraft.font, Component.translatable("screen.minepulse.pass"), mouseX, mouseY);
        } else if (flagB) {
            gg.renderTooltip(this.minecraft.font, Component.translatable("screen.minepulse.unpass"), mouseX, mouseY);
        }

        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        final int baseX = this.width / 2;
        final int baseY = this.height /2;

        final int height = baseY - 176 / 2;

        final int baseAX = baseX - 5;
        final int rangeA = baseAX - 176;

        final int baseBX = baseX + 5;
        final int rangeB = baseAX + 176;

        final boolean flag = mouseY > height && mouseY < height + 176;
        final boolean flagA = flag && mouseX > rangeA && mouseX < baseAX;
        final boolean flagB = flag && mouseX > baseBX && mouseX < rangeB;

        if (flagA || flagB) {
            ClientConfig.SHOULD_ASK = false;
            ClientConfig.writeIn(flagA);
            this.onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
