package club.someoneice.minepulse.mixin;

import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.DropExperienceBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DropExperienceBlock.class)
public interface DropExperienceBlockMixin {
    @Accessor
    IntProvider getXpRange();
}
