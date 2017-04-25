package brightspark.structuralrelocation.block;

import brightspark.structuralrelocation.item.ItemSelector;
import brightspark.structuralrelocation.tileentity.ITeleporter;
import brightspark.structuralrelocation.util.LogHelper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class AbstractBlockTeleporter extends AbstractBlockContainer
{
    public AbstractBlockTeleporter(String name, Material mat)
    {
        super(name, mat);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        LogHelper.info("Item Activated With: " + (heldItem == null ? "Null" : heldItem.toString()));
        if(heldItem != null && heldItem.getItem() instanceof ItemSelector)
            return false;
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof ITeleporter && !player.isSneaking())
        {
            //Try to start teleporting
            ((ITeleporter) te).teleport(player);
        }
        return true;
    }
}
