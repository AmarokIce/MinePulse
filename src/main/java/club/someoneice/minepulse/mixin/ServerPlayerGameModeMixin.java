package club.someoneice.minepulse.mixin;

import club.someoneice.minepulse.core.MinePulse;
import club.someoneice.minepulse.core.ServerConfig;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
	@Shadow protected ServerLevel level;

	@Shadow @Final protected ServerPlayer player;

	@Shadow public abstract GameType getGameModeForPlayer();

	@Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
	private void willDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		final BlockState block = this.level.getBlockState(pos);
		final boolean flag = player instanceof FakePlayer
				|| this.player.isCreative()
				|| this.player.isSpectator()
				|| this.player.blockActionRestricted(this.level, pos, this.getGameModeForPlayer())
				|| !this.player.getMainHandItem().getItem().canAttackBlock(block, level, pos, player);

		if (flag) {
			return;
		}

		boolean flagShouldMine = MinePulse.PLAYER_STATE.getOrDefault(this.player.getDisplayName().getString(), false);
		if (!flagShouldMine) {
			return;
		}
		AtomicBoolean flagOut = new AtomicBoolean(false);
		ServerConfig.ORE_MARKS.stream().filter(it -> it.mark(block)).findFirst().ifPresent(it -> {
			MinePulse.oreHook(player, level, pos, it);
			flagOut.set(true);
		});

		if (flagOut.get()) {
			cir.setReturnValue(true);
			return;
		}

		ServerConfig.TREE_MARKS.stream().filter(it -> it.mark(block)).findFirst().ifPresent(it -> {
			MinePulse.logHook(player, level, pos);
			flagOut.set(true);
		});

		if (flagOut.get()) {
			cir.setReturnValue(true);
		}
	}
}
