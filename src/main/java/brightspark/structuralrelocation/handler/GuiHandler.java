package brightspark.structuralrelocation.handler;

import brightspark.structuralrelocation.block.AbstractBlockContainer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        BlockPos pos = new BlockPos(x, y, z);
        Block block = world.getBlockState(pos).getBlock();
        if(block instanceof AbstractBlockContainer)
            return ((AbstractBlockContainer) block).getContainer(player.inventory, world.getTileEntity(pos));
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        BlockPos pos = new BlockPos(x, y, z);
        Block block = world.getBlockState(pos).getBlock();
        if(block instanceof AbstractBlockContainer)
            return ((AbstractBlockContainer) block).getGui(player.inventory, world.getTileEntity(pos));
        return null;
    }
}
