package brightspark.structuralrelocation.init;

import brightspark.structuralrelocation.StructuralRelocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

@GameRegistry.ObjectHolder(StructuralRelocation.MOD_ID)
public class SRItems
{
    public static final Item selector = getNullItem();
    public static final Item debugger = getNullItem();

    @SuppressWarnings("ConstantConditions")
    @Nonnull
    private static Item getNullItem()
    {
        return null;
    }
}
