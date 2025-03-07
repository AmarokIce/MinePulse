package club.someoneice.minepulse.mixin;

import club.someoneice.minepulse.Config;
import club.someoneice.minepulse.MinePulse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ServerPlayerGameMode.class)
public final class ServerPlayerGameModeMixin {
	@Shadow protected ServerLevel level;

	@Shadow @Final protected ServerPlayer player;

	@Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
	private void willDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (player.isShiftKeyDown()) {
			return;
		}

		if (this.player.isCreative()) {
			return;
		}

		final BlockState block = this.level.getBlockState(pos);
		if (!this.player.getMainHandItem().getItem().canAttackBlock(block, level, pos, player)) {
			return;
		}

		AtomicBoolean flag = new AtomicBoolean(false);

		if (Config.enableOre) {
			Config.ORE_MARKS.stream().filter(it -> it.mark(block)).findFirst().ifPresent(it -> {
				MinePulse.oreHook(player, level, pos, block, it);
				flag.set(true);
			});

			if (flag.get()) {
				cir.setReturnValue(true);
				return;
			}
		}

		if (Config.enableTree) {
			Config.TREE_MARKS.stream().filter(it -> it.mark(block)).findFirst().ifPresent(it -> {
				// TODO - tree hook
				MinePulse.oreHook(player, level, pos, block, it);
				flag.set(true);
			});

			if (flag.get()) {
				cir.setReturnValue(true);
			}
		}
	}
}
