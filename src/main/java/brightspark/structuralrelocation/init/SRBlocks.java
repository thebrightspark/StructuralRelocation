package brightspark.structuralrelocation.init;

import brightspark.structuralrelocation.StructuralRelocation;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

@GameRegistry.ObjectHolder(StructuralRelocation.MOD_ID)
public class SRBlocks
{
    public static final Block single_teleporter = getNullBlock();
    public static final Block area_teleporter = getNullBlock();
    public static final Block creative_generator = getNullBlock();

    @SuppressWarnings("ConstantConditions")
    @Nonnull
    private static Block getNullBlock()
    {
        return null;
    }
}
