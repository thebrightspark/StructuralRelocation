package brightspark.structuralrelocation.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class CommonUtils
{
    /**
     * Clears all of the given keys from the stack's NBT.
     */
    public static void clearNBTKeys(ItemStack stack, String... keys)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null) return;
        for(String k : keys)
            tag.removeTag(k);
    }
}
