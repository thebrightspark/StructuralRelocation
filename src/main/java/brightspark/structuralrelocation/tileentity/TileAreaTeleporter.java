package brightspark.structuralrelocation.tileentity;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.LocationArea;
import brightspark.structuralrelocation.util.LogHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

/**
 * Created by Mark on 17/04/2017.
 */
public class TileAreaTeleporter extends TileEntity implements ITickable
{
    private LocationArea toMove;
    private Location target;
    private BlockPos curBlock, targetRelMax, toMoveMin;

    public TileAreaTeleporter() {}

    public void setAreaToMove(LocationArea area)
    {
        toMove = area;
    }

    public void setTarget(Location target)
    {
        this.target = target;
    }

    public boolean canTeleport()
    {
        return toMove != null && target != null;
    }

    public void teleport()
    {
        //Called from the block when right clicked
        if(worldObj.isRemote || toMove == null || target == null || curBlock != null)
            return;

        //TODO: Check that the target area will not intersect with the area to move
        //TODO: Check that the target area is completely clear

        //Start an area teleport
        curBlock = new BlockPos(0, 0, 0);
        targetRelMax = toMove.getRelativeEndPoint();
        toMoveMin = toMove.getStartingPoint();
    }

    @Override
    public void update()
    {
        //TODO: Finish teleporting an area
        if(worldObj.isRemote || curBlock == null) return;

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
        }
        //Skip air blocks
        while(curBlock != null && server.isAirBlock(toMoveMin.add(curBlock)));
    }
}
