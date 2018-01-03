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
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(SRItems.itemSelector), " g ", "grg", " g ", 'g', "ingotGold", 'r', Blocks.END_ROD));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(SRItems.itemDebugger), " i ", "ibi", " i ", 'i', "ingotIron", 'b', Items.BLAZE_ROD));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(SRBlocks.singleTeleporter), "grg", "oro", "ooo", 'g', "ingotGold", 'r', Blocks.END_ROD, 'o', "obsidian"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(SRBlocks.areaTeleporter), "drd", "oro", "ooo", 'd', "gemDiamond", 'r', Blocks.END_ROD, 'o', "obsidian"));
    }
}
