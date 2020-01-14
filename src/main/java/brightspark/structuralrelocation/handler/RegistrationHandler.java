package brightspark.structuralrelocation.handler;

import brightspark.structuralrelocation.StructuralRelocation;
import brightspark.structuralrelocation.block.BlockAreaTeleporter;
import brightspark.structuralrelocation.block.BlockCreativeGenerator;
import brightspark.structuralrelocation.block.BlockSingleTeleporter;
import brightspark.structuralrelocation.init.SRBlocks;
import brightspark.structuralrelocation.item.ItemDebugger;
import brightspark.structuralrelocation.item.ItemSelector;
import brightspark.structuralrelocation.tileentity.TileAreaTeleporter;
import brightspark.structuralrelocation.tileentity.TileCreativeGenerator;
import brightspark.structuralrelocation.tileentity.TileSingleTeleporter;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = StructuralRelocation.MOD_ID)
public class RegistrationHandler
{
    @SubscribeEvent
    public static void regItems(RegistryEvent.Register<Item> event)
    {
        //Register all items
        event.getRegistry().registerAll(
                new ItemSelector(),
                new ItemDebugger(),
                createItemBlock(SRBlocks.single_teleporter),
                createItemBlock(SRBlocks.area_teleporter),
                createItemBlock(SRBlocks.creative_generator)
        );
    }

    @SubscribeEvent
    public static void regBlocks(RegistryEvent.Register<Block> event)
    {
        //Register all blocks
        event.getRegistry().registerAll(
                new BlockSingleTeleporter(),
                new BlockAreaTeleporter(),
                new BlockCreativeGenerator()
        );
    }

    public static void regTileEntities()
    {
        regTE(TileSingleTeleporter.class, SRBlocks.single_teleporter);
        regTE(TileAreaTeleporter.class, SRBlocks.area_teleporter);
        regTE(TileCreativeGenerator.class, SRBlocks.creative_generator);
    }

    private static Item createItemBlock(Block block)
    {
        //noinspection ConstantConditions
        return new ItemBlock(block).setRegistryName(block.getRegistryName());
    }

    private static void regTE(Class<? extends TileEntity> teClass, Block block)
    {
        //noinspection ConstantConditions
        GameRegistry.registerTileEntity(teClass, block.getRegistryName());
    }
}
