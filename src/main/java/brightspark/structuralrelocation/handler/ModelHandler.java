package brightspark.structuralrelocation.handler;

import brightspark.structuralrelocation.init.SRBlocks;
import brightspark.structuralrelocation.init.SRItems;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ModelHandler
{
    @SubscribeEvent
    public static void regModels(ModelRegistryEvent event)
    {
        //Register all item models
        for(Item item : SRItems.ITEMS)
            regModel(item);

        //Register block models
        for(Block block : SRBlocks.BLOCKS)
            regModel(block);
    }
    
    public static void regModel(Item item)
    {
        regModel(item, 0);
    }

    public static void regModel(Block block)
    {
        regModel(Item.getItemFromBlock(block));
    }

    public static void regModel(Item item, int meta)
    {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
