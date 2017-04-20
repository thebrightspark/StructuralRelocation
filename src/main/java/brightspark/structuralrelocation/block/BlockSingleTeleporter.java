package brightspark.structuralrelocation.block;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.item.ItemTargetFinder;
import brightspark.structuralrelocation.tileentity.TileSingleTeleporter;
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

public class BlockSingleTeleporter extends AbstractBlockContainer
{
    public BlockSingleTeleporter()
    {
        super("singleTeleporter", Material.ROCK);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileSingleTeleporter();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileSingleTeleporter)
        {
            TileSingleTeleporter teleporter = (TileSingleTeleporter) te;
            if(heldItem != null && heldItem.getItem() instanceof ItemTargetFinder)
            {
                Location target = ItemTargetFinder.getTarget(heldItem);
                if(target != null)
                {
                    teleporter.setTarget(target);
                    if(world.isRemote)
                        player.addChatMessage(new TextComponentString("Target set!\n" +
                            "Dimension ID: " + target.dimensionId + "   Position: " + target.position.toString()));
                }
            }
            else if(!player.isSneaking() && teleporter.canTeleport())
            {
                //Start teleportation
                if(world.isRemote) player.addChatMessage(new TextComponentString("Teleporting block..."));
                teleporter.teleport();
            }
        }
        return true;
    }
}
