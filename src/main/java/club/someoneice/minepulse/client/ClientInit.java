package club.someoneice.minepulse.client;

import club.someoneice.minepulse.core.MinePulse;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ClientInit implements ClientModInitializer {
    public static boolean check = false;

    @Override
    public void onInitializeClient() {
        ClientConfig.init();
        ClientPlayNetworking.registerGlobalReceiver(MinePulse.BlockSoundPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                final Player player = context.player();
                final Level world = context.client().level;
                final BlockPos pos = payload.get();
                final BlockState state = world.getBlockState(pos);

                world.levelEvent(player, 2001, pos, Block.getId(state));
                world.playSound(player, pos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0f, 1.0f);
            });
        });
    }
}
