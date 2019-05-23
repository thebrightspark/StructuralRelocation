package brightspark.structuralrelocation.handler;

import brightspark.structuralrelocation.StructuralRelocation;
import brightspark.structuralrelocation.init.SRBlocks;
import brightspark.structuralrelocation.init.SRItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = StructuralRelocation.MOD_ID)
public class RegistrationHandler
{
    @SubscribeEvent
    public static void regItems(RegistryEvent.Register<Item> event)
    {
        //Register all items
        SRItems.regItems();
        IForgeRegistry<Item> registry = event.getRegistry();
        for(Item item : SRItems.ITEMS)
            registry.register(item);

        SRBlocks.regBlocks();
        for(Item item : SRBlocks.ITEM_BLOCKS)
            registry.register(item);
    }

    @SubscribeEvent
    public static void regBlocks(RegistryEvent.Register<Block> event)
    {
        //Register all blocks
        SRBlocks.regBlocks();
        IForgeRegistry<Block> registry = event.getRegistry();
        for(Block block : SRBlocks.BLOCKS)
            registry.register(block);
    }
}
