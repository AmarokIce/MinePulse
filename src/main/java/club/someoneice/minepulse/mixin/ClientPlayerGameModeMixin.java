package club.someoneice.minepulse.mixin;

import club.someoneice.minepulse.client.ClientConfig;
import club.someoneice.minepulse.client.ClientInit;
import club.someoneice.minepulse.client.ScreenConfig;
import club.someoneice.minepulse.core.MinePulse;
import club.someoneice.minepulse.core.ServerConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class ClientPlayerGameModeMixin {
    @Shadow @Final private Minecraft minecraft;

    @Shadow public abstract GameType getPlayerMode();

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void willDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        final Player player = this.minecraft.player;
        final Level world = this.minecraft.level;

        assert player != null;
        assert world != null;

        final BlockState block = world.getBlockState(pos);

        final boolean flag = player.isCreative()
                || player.isSpectator()
                || !player.getMainHandItem().getItem().canAttackBlock(block, world, pos, player)
                || player.blockActionRestricted(world, pos, this.getPlayerMode());

        if (flag) {
            return;
        }

        if (!ClientInit.check) {
            return;
        }

        final Boolean flagOre = ServerConfig.ORE_MARKS.stream().anyMatch(it -> it.mark(block));
        final Boolean flagTree = ServerConfig.TREE_MARKS.stream().anyMatch(it -> it.mark(block));

        if (flagOre || flagTree) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    public void onStartDestoryBlock(BlockPos loc, Direction face, CallbackInfoReturnable<Boolean> cir) {
        if (ClientConfig.SHOULD_ASK) {
            this.minecraft.setScreen(new ScreenConfig());
            cir.setReturnValue(false);
        }

        final Player player = this.minecraft.player;
        boolean checking = ClientConfig.REVERSAL == player.isShiftKeyDown();
        if (ClientInit.check != checking) {
            ClientPlayNetworking.send(new MinePulse.PlayerChangedStatePacket((ClientInit.check = checking)));
        }
    }
}
