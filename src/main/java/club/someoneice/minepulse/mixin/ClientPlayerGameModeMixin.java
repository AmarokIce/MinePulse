package club.someoneice.minepulse.mixin;

import club.someoneice.minepulse.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public final class ClientPlayerGameModeMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void willDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        final Player player = this.minecraft.player;
        final Level world = this.minecraft.level;

        if (player.isShiftKeyDown() != Config.reversalShiftEnable) {
            return;
        }

        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        BlockState block = world.getBlockState(pos);
        if (!player.getMainHandItem().getItem().canAttackBlock(block, world, pos, player)) {
            return;
        }

        final Boolean flagOre = Config.enableOre && Config.ORE_MARKS.stream().anyMatch(it -> it.mark(block));
        final Boolean flagTree = Config.enableTree && Config.TREE_MARKS.stream().anyMatch(it -> it.mark(block));

        if (flagOre || flagTree) {
            cir.setReturnValue(false);
        }
    }
}
