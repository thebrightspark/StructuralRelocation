package brightspark.structuralrelocation.item;

import brightspark.structuralrelocation.StructuralRelocation;
import net.minecraft.item.Item;

public class ItemBasic extends Item
{
    public ItemBasic(String name)
    {
        setUnlocalizedName(name);
        setRegistryName(name);
        setCreativeTab(StructuralRelocation.SR_TAB);
    }
}
