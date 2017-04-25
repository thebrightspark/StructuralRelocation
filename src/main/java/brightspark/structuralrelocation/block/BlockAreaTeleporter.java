package brightspark.structuralrelocation.block;

import brightspark.structuralrelocation.tileentity.TileAreaTeleporter;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockAreaTeleporter extends AbstractBlockContainer
{
    public BlockAreaTeleporter()
    {
        super("areaTeleporter", Material.ROCK);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileAreaTeleporter();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileAreaTeleporter && !player.isSneaking() && ((TileAreaTeleporter) te).canTeleport())
        {
            //Start teleportation
            if(world.isRemote) player.addChatMessage(new TextComponentString("Teleporting blocks..."));
            ((TileAreaTeleporter) te).teleport(player);
        }
        return true;
    }
}
