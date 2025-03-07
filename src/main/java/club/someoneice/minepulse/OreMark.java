package club.someoneice.minepulse;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public record OreMark(Set<TagKey<Block>> tags, Set<Block> blocks) {
    public boolean mark(BlockState block) {
        return this.tags.stream().anyMatch(block::is) && this.blocks.stream().anyMatch(block::is);
    }
}
