package club.someoneice.minepulse;

import club.someoneice.minepulse.mixin.DropExperienceBlockMixin;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.util.*;

public final class MinePulse implements ModInitializer {
	@Override
	public void onInitialize() {
        try {
            Config.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PayloadTypeRegistry.playS2C().register(BlockSoundPacket.ID, BlockSoundPacket.CODEC);
		ClientPlayNetworking.registerGlobalReceiver(BlockSoundPacket.ID, (payload, context) -> {
			context.client().execute(() -> {
				final Player player = context.player();
				final Level world = context.client().level;
				final BlockPos pos = payload.get();
				final BlockState state = world.getBlockState(pos);

				world.playSound(player, pos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0f, 1.0f);
				world.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, state));
				world.levelEvent(player, 2001, pos, Block.getId(state));
			});
		});
	}

	// TODO - Oak
	public static void oreHook(final Player player, final ServerLevel world, final BlockPos pos,
								  final BlockState blockState, final OreMark mark) {
		final Set<BlockPos> blockPos = Sets.newHashSet();
		final Stack<BlockPos> blockPosStack = new Stack<>();

		final Block block = blockState.getBlock();

		blockPos.add(pos);
		blockPosStack.push(pos);

		while(!blockPosStack.empty()) {
			BlockPos posIn = blockPosStack.pop();
			for (Direction value : Direction.values()) {
				BlockPos posAt = posIn.relative(value);
				BlockState state = world.getBlockState(posAt);

				if (blockPos.contains(posAt) || !mark.mark(state)) {
					continue;
				}

				blockPos.add(posAt);
				blockPosStack.push(posAt);
			}
		}

		final int[] posYs = blockPos.stream()
				.mapToInt(BlockPos::getY)
				.distinct()
				.filter(it -> it != pos.getY())
				.toArray();

		final int max = Arrays.stream(posYs).max().orElseGet(pos::getY);
		final int min = Arrays.stream(posYs).min().orElseGet(pos::getY);

		final int fin = max < pos.getY() ? Math.min(min, pos.getY()) : max;

		final Map<Integer, BlockPos> posMap = Maps.newHashMap();
		blockPos.stream()
				.filter(it -> it.getY() == fin && it != pos)
				.forEach(it -> posMap.put(dis(pos, it), it));

		final BlockPos finalPos = posMap.isEmpty() ? pos
				: posMap.get(posMap.keySet().stream().max(Comparator.naturalOrder()).get());

		breakBlock(finalPos, world.getBlockState(finalPos), world, player, pos);
	}

	private static int dis(BlockPos A, BlockPos B) {
		return Math.abs(A.getX() - B.getX()) + Math.abs(A.getZ() - B.getZ());
	}

	private static void breakBlock(BlockPos pos, BlockState state, ServerLevel world, Player player, BlockPos oPos) {
		ItemStack item = player.getMainHandItem();
		item.mineBlock(world, state, pos, player);
		Block.getDrops(state, world, pos, null, player, item.copy())
				.forEach(itemStack ->
						Block.popResource(world, player.getOnPos().above(2).relative(player.getDirection()), itemStack));

		if (state.getBlock() instanceof DropExperienceBlock exp) {
			int expDrops = ((DropExperienceBlockMixin) exp).getXpRange().sample(world.random);
			if (world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
				ExperienceOrb.award(world, Vec3.atCenterOf(oPos), expDrops);
			}
		}


		world.players().forEach(it -> ServerPlayNetworking.send(it, new BlockSoundPacket(pos)));
		world.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, state));
		world.removeBlock(pos, false);
	}

	public static class BlockSoundPacket implements CustomPacketPayload {
		public static final Type<BlockSoundPacket> ID = new CustomPacketPayload.Type<>(ResourceLocation.tryParse("minepulse:sendsound"));
		public static final StreamCodec<FriendlyByteBuf, BlockSoundPacket> CODEC = CustomPacketPayload.codec(BlockSoundPacket::write, BlockSoundPacket::new);

		private final BlockPos pos;

		public BlockSoundPacket(FriendlyByteBuf buffer) {
			this.pos = buffer.readBlockPos();
		}

		public BlockSoundPacket(BlockPos pos) {
			this.pos = pos;
		}

		private void write(FriendlyByteBuf buffer) {
			buffer.writeBlockPos(this.pos);
		}

		public BlockPos get() {
			return this.pos;
		}

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}
}