package brightspark.structuralrelocation.tileentity;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.LocationArea;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Iterator;

public class TileAreaTeleporter extends AbstractTileTeleporter implements ITickable
{
    private LocationArea toMove;
    private Location target;
    private BlockPos curBlock, targetRelMax, toMoveMin;

    public void setAreaToMove(LocationArea area)
    {
        if(area != null) toMove = area;
    }

    public LocationArea getAreaToMove()
    {
        return toMove;
    }

    public void setTarget(Location target)
    {
        if(target != null) this.target = target;
    }

    public Location getTarget()
    {
        return target;
    }

    /**
     * This should only be used to sync the data from the server in the Gui's Container class
     */
    @SideOnly(Side.CLIENT)
    public void setCurBlock(BlockPos pos)
    {
        curBlock = pos;
    }

    public BlockPos getCurBlock()
    {
        return curBlock == null ? null : curBlock.add(toMoveMin);
    }

    public boolean isActive()
    {
        return curBlock != null;
    }

    private boolean isAreaClear(BlockPos pos1, BlockPos pos2)
    {
        Iterator<BlockPos> positions = BlockPos.getAllInBox(pos1, pos2).iterator();
        while(positions.hasNext())
            if(!world.isAirBlock(positions.next()))
                return false;
        return true;
    }

    @Override
    public void teleport(EntityPlayer player)
    {
        //Called from the block when right clicked
        if(world.isRemote || toMove == null || target == null || curBlock != null)
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
            player.sendMessage(new TextComponentString("Area to be moved must not intersect the destination area!"));
            return;
        }
        */

        //Check that the target area is completely clear
        //TODO: Check more precisely if the blocks can fit at the destination rather than just making sure the area is completely clear?
        if(!isAreaClear(destinationStart, destinationEnd))
        {
            player.sendMessage(new TextComponentString("Target area is not clear!\n" +
                    "Position 1: " + destinationStart.toString() + "\n" +
                    "Position 2: " + destinationEnd.toString()));
            return;
        }

        //Start an area teleport
        curBlock = new BlockPos(0, 0, 0);
        targetRelMax = toMove.getRelativeEndPoint();
        toMoveMin = toMove.getStartingPoint();
    }

    /**
     * If the teleporter is running, then stop it
     */
    public void stop()
    {
        curBlock = null;
    }

    @Override
    public void update()
    {
        if(world.isRemote || curBlock == null || !hasEnoughEnergy()) return;

        //Teleport the block
        WorldServer server = world.getMinecraftServer().worldServerForDimension(target.dimensionId);
        BlockPos toMovePos = toMoveMin.add(curBlock);
        BlockPos targetPos = target.position.add(curBlock);
        teleportBlock(toMovePos, new Location(target.dimensionId, targetPos));

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
                nextPos = null;

            curBlock = nextPos == null ? null : new BlockPos(nextPos);
            toMovePos = curBlock == null ? null : toMoveMin.add(curBlock);
        }
        //Skip air and unbreakable blocks
        while(curBlock != null && (server.isAirBlock(toMovePos) || server.getBlockState(toMovePos).getBlockHardness(server, toMovePos) < 0));

        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        //Read target
        if(nbt.hasKey("target")) target = new Location(nbt.getCompoundTag("target"));
        //Read area
        if(nbt.hasKey("area")) toMove = new LocationArea(nbt.getCompoundTag("area"));
        //Read other data
        if(nbt.hasKey("curBlock")) curBlock = BlockPos.fromLong(nbt.getLong("curBlock"));
        if(nbt.hasKey("targetRelMax")) targetRelMax = BlockPos.fromLong(nbt.getLong("targetRelMax"));
        if(nbt.hasKey("toMoveMin")) toMoveMin = BlockPos.fromLong(nbt.getLong("toMoveMin"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        //Write target
        if(target != null) nbt.setTag("target", target.serializeNBT());
        //Write area
        if(toMove != null) nbt.setTag("area", toMove.serializeNBT());
        //Write other data
        if(curBlock != null) nbt.setLong("curBlock", curBlock.toLong());
        if(targetRelMax != null) nbt.setLong("targetRelMax", targetRelMax.toLong());
        if(toMoveMin != null) nbt.setLong("toMoveMin", toMoveMin.toLong());

        return super.writeToNBT(nbt);
    }
}
