package brightspark.structuralrelocation.item;

import brightspark.structuralrelocation.StructuralRelocation;
import net.minecraft.item.Item;

public class ItemBasic extends Item
{
    public ItemBasic(String name)
    {
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(StructuralRelocation.SR_TAB);
    }
}
