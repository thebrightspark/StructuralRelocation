package brightspark.structuralrelocation.init;

import brightspark.structuralrelocation.block.BlockSingleTeleporter;
import brightspark.structuralrelocation.tileentity.TileSingleTeleporter;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;

public class SRBlocks
{
    public static List<Block> BLOCKS = new ArrayList<Block>();
    public static List<ItemBlock> ITEM_BLOCKS = new ArrayList<ItemBlock>();

    public static BlockSingleTeleporter singleTeleporter;

    public static void regBlock(Block block)
    {
        BLOCKS.add(block);
        ITEM_BLOCKS.add((ItemBlock) new ItemBlock(block).setRegistryName(block.getRegistryName()));
    }

    public static void regTE(Class<? extends TileEntity> teClass, Block block)
    {
        GameRegistry.registerTileEntity(teClass, block.getRegistryName().getResourcePath());
    }

    public static void regBlocks()
    {
        if(!BLOCKS.isEmpty()) return;

        regBlock(singleTeleporter = new BlockSingleTeleporter());
    }

    public static void regTileEntities()
    {
        regTE(TileSingleTeleporter.class, singleTeleporter);
    }
}
