package brightspark.structuralrelocation.block;

import brightspark.structuralrelocation.StructuralRelocation;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockBasic extends Block
{
    public BlockBasic(String name, Material mat)
    {
        super(mat);
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(StructuralRelocation.SR_TAB);
        setHardness(2f);
        setResistance(10f);
    }
}
