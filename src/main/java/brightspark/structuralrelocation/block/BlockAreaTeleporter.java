package brightspark.structuralrelocation.block;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.LocationArea;
import brightspark.structuralrelocation.item.ItemAreaFinder;
import brightspark.structuralrelocation.item.ItemTargetFinder;
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
        if(te instanceof TileAreaTeleporter)
        {
            TileAreaTeleporter teleporter = (TileAreaTeleporter) te;
            if(heldItem != null)
            {
                if(heldItem.getItem() instanceof ItemAreaFinder)
                {
                    //Set Area
                    LocationArea area = ItemAreaFinder.getArea(heldItem);
                    if(area != null)
                    {
                        teleporter.setAreaToMove(area);
                        if(world.isRemote)
                            player.addChatMessage(new TextComponentString("Area set!\n" +
                                    "Position 1: " + area.pos1.toString() + "\n" +
                                    "Position 2: " + area.pos2.toString()));
                    }
                }
                else if(heldItem.getItem() instanceof ItemTargetFinder)
                {
                    //Set Target
                    Location target = ItemTargetFinder.getTarget(heldItem);
                    if(target != null)
                    {
                        teleporter.setTarget(target);
                        if(world.isRemote)
                            player.addChatMessage(new TextComponentString("Target set!\n" +
                                    "Dimension ID: " + target.dimensionId + "   Position: " + target.position.toString()));
                    }
                }
            }
            else if(!player.isSneaking() && teleporter.canTeleport())
            {
                //Start teleportation
                if(world.isRemote) player.addChatMessage(new TextComponentString("Teleporting blocks..."));
                teleporter.teleport();
            }
        }
        return true;
    }
}
