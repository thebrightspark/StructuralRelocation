package brightspark.structuralrelocation.tileentity;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.LocationArea;
import brightspark.structuralrelocation.util.LogHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;

import java.util.Iterator;

public class TileAreaTeleporter extends AbstractTileTeleporter implements ITickable
{
    private LocationArea toMove;
    private Location target;
    private BlockPos curBlock, targetRelMax, toMoveMin;

    public TileAreaTeleporter() {}

    public void setAreaToMove(LocationArea area)
    {
        if(area != null) toMove = area;
    }

    public void setTarget(Location target)
    {
        if(target != null) this.target = target;
    }

    private boolean isAreaClear(BlockPos pos1, BlockPos pos2)
    {
        Iterator<BlockPos> positions = BlockPos.getAllInBox(pos1, pos2).iterator();
        while(positions.hasNext())
            if(!worldObj.isAirBlock(positions.next()))
                return false;
        return true;
    }

    @Override
    public void teleport(EntityPlayer player)
    {
        //Called from the block when right clicked
        if(worldObj.isRemote || toMove == null || target == null || curBlock != null)
            return;

        BlockPos destinationStart = target.position;
        BlockPos destinationEnd = destinationStart.add(toMove.getRelativeEndPoint());

        //COMMENTED OUT: I don't think I actually need to check intersection? Just so long as the destination area is clear.
        //Check that the target area will not intersect with the area to move
        /*
        AxisAlignedBB selection = new AxisAlignedBB(toMove.pos1, toMove.pos2);
        AxisAlignedBB destination = new AxisAlignedBB(destinationStart, destinationEnd);
        if(selection.intersectsWith(destination))
        {
            player.addChatMessage(new TextComponentString("Area to be moved must not intersect the destination area!"));
            return;
        }
        */

        //Check that the target area is completely clear
        //TODO: Check more precisely if the blocks can fit at the destination rather than just making sure the area is completely clear?
        if(!isAreaClear(destinationStart, destinationEnd))
        {
            player.addChatMessage(new TextComponentString("Target area is not clear!\n" +
                    "Position 1: " + destinationStart.toString() + "\n" +
                    "Position 2: " + destinationEnd.toString()));
            return;
        }

        //Start an area teleport
        curBlock = new BlockPos(0, 0, 0);
        targetRelMax = toMove.getRelativeEndPoint();
        toMoveMin = toMove.getStartingPoint();

        player.addChatMessage(new TextComponentString("Teleporting blocks..."));
    }

    @Override
    public void update()
    {
        if(worldObj.isRemote || curBlock == null || !hasEnoughEnergy()) return;

        //Teleport the block
        WorldServer server = worldObj.getMinecraftServer().worldServerForDimension(target.dimensionId);
        BlockPos toMovePos = toMoveMin.add(curBlock);
        BlockPos targetPos = target.position.add(curBlock);
        server.setBlockState(targetPos, worldObj.getBlockState(toMovePos));
        worldObj.setBlockToAir(toMovePos);

        do
        {
            //Get the next block to teleport
            BlockPos nextPos = curBlock.east();

            //If reached the max for X, then go back to min X and add 1 to Z
            if(nextPos.getX() > targetRelMax.getX())
                nextPos = new BlockPos(0, nextPos.getY(), nextPos.south().getZ());

            //If reached the max for Z, then go back to min Z and add 1 to Y
            if(nextPos.getZ() > targetRelMax.getZ())
                nextPos = new BlockPos(nextPos.getX(), nextPos.up().getY(), 0);

            //If reached the max for Y, then finished!
            if(nextPos.getY() > targetRelMax.getY())
            {
                nextPos = null;
                LogHelper.info("Teleporting Finished!");
            }

            curBlock = nextPos == null ? null : new BlockPos(nextPos);
            toMovePos = curBlock == null ? null : toMoveMin.add(curBlock);
        }
        //Skip air and unbreakable blocks
        while(curBlock != null && (server.isAirBlock(toMovePos) || server.getBlockState(toMovePos).getBlockHardness(server, toMovePos) < 0));
    }
}
