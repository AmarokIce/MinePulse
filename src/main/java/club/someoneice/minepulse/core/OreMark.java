package club.someoneice.minepulse.core;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public record OreMark(Set<TagKey<Block>> tags, Set<Block> blocks) {
    public boolean mark(BlockState state) {
        return this.tags.stream().anyMatch(state::is) || this.blocks.stream().anyMatch(state::is);
    }
}
