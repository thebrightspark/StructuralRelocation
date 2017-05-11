package brightspark.structuralrelocation.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class SRRecipes
{
    public static void init()
    {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(SRItems.itemSelector), " g ", "grg", " g ", 'g', Items.GOLD_INGOT, 'r', Blocks.END_ROD));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(SRBlocks.singleTeleporter), "grg", "oro", "ooo", 'g', Items.GOLD_INGOT, 'r', Blocks.END_ROD, 'o', Blocks.OBSIDIAN));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(SRBlocks.areaTeleporter), "drd", "oro", "ooo", 'd', Items.DIAMOND, 'r', Blocks.END_ROD, 'o', Blocks.OBSIDIAN));
    }
}
