package brightspark.structuralrelocation.tileentity;

import brightspark.structuralrelocation.Config;
import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.LocationArea;
import brightspark.structuralrelocation.message.MessageUpdateClientTeleporterObstruction;
import brightspark.structuralrelocation.util.CommonUtils;
import brightspark.structuralrelocation.util.LogHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Iterator;

public class TileAreaTeleporter extends AbstractTileTeleporter implements ITickable
{
    private LocationArea toMove;
    private Location target;
    private BlockPos curBlock, targetRelMax, toMoveMin;
    public BlockPos lastBlockInTheWay;

    private boolean checkedEnergy = false;

    public void setAreaToMove(LocationArea area)
    {
        if(area == null) return;
        toMove = area;
        markDirty();
    }

    public LocationArea getAreaToMove()
    {
        return toMove;
    }

    public void setTarget(Location target)
    {
        if(target == null) return;
        this.target = target;
        markDirty();
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
        markDirty();
    }

    public BlockPos getCurBlock()
    {
        return curBlock == null ? null : curBlock.add(toMoveMin);
    }

    public boolean isActive()
    {
        return curBlock != null;
    }

    private boolean doPreActionChecks()
    {
        if(world.isRemote) return false;
        if(toMove == null || target == null || curBlock != null)
        {
            if(Config.debugTeleportMessages) LogHelper.info("Can not teleport. Either no target set or no area set.");
            return false;
        }

        BlockPos destinationStart = target.position;
        BlockPos destinationEnd = destinationStart.add(toMove.getRelativeEndPoint());

        //Check that the target area is completely clear
        //TODO: Check more precisely if the blocks can fit at the destination rather than just making sure the area is completely clear?
        WorldServer targetDim = world.getMinecraftServer().worldServerForDimension(target.dimensionId);
        Iterator<BlockPos> positions = BlockPos.getAllInBox(destinationStart, destinationEnd).iterator();
        while(positions.hasNext())
        {
            BlockPos checkPos = positions.next();
            if(!isDestinationClear(targetDim, checkPos))
            {
                lastBlockInTheWay = checkPos;
                EntityPlayer player = getLastPlayer();
                if(player != null)
                {
                    player.sendMessage(new TextComponentString("Target area is not clear!\n" +
                            "Position 1: " + destinationStart.toString() + "\n" +
                            "Position 2: " + destinationEnd.toString() + "\n" +
                            "Found block ").appendSibling(new TextComponentTranslation(targetDim.getBlockState(checkPos).getBlock().getUnlocalizedName() + ".name"))
                            .appendText(" at " + checkPos.toString()));
                }
                //Update client teleporter so the Debugger item can be used
                CommonUtils.NETWORK.sendToAll(new MessageUpdateClientTeleporterObstruction(pos, checkPos));
                if(Config.debugTeleportMessages) LogHelper.info("Can not teleport. Destination area contains an obstruction at " + checkPos.toString() + " in dimension " + targetDim.provider.getDimension());
                return false;
            }
        }

        if(lastBlockInTheWay != null) CommonUtils.NETWORK.sendToAll(new MessageUpdateClientTeleporterObstruction(pos, null));
        lastBlockInTheWay = null;

        //Start an area teleport
        curBlock = new BlockPos(0, 0, 0);
        targetRelMax = toMove.getRelativeEndPoint();
        toMoveMin = toMove.getStartingPoint();
        return true;
    }

    @Override
    public void teleport(EntityPlayer player)
    {
        super.teleport(player);
        if(doPreActionChecks() && Config.debugTeleportMessages)
            LogHelper.info("Area teleportation successfully started.");
    }

    @Override
    public void copy(EntityPlayer player)
    {
        super.copy(player);
        if(doPreActionChecks() && Config.debugTeleportMessages)
            LogHelper.info("Area copy successfully started.");
    }

    /**
     * If the teleporter is running, then stop it
     */
    public void stop()
    {
        curBlock = null;
        markDirty();
        if(Config.debugTeleportMessages) LogHelper.info("Teleportation stopped.");
    }

    @Override
    public void update()
    {
        if(world.isRemote || curBlock == null) return;
        if(!hasEnoughEnergy())
        {
            if(Config.debugTeleportMessages && !checkedEnergy)
            {
                LogHelper.info("Can not teleport. Not enough power.");
                checkedEnergy = true;
            }
            return;
        }

        checkedEnergy = false;

        //Teleport the block
        WorldServer worldTo = world.getMinecraftServer().worldServerForDimension(target.dimensionId);
        BlockPos toMovePos = toMoveMin.add(curBlock);
        BlockPos targetPos = target.position.add(curBlock);
        if(isCopying)
            copyBlock(toMovePos, worldTo, targetPos);
        else
            teleportBlock(toMovePos, worldTo, targetPos);

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
        while(curBlock != null && !(isCopying ? canCopyBlock(world, toMovePos) : canTeleportBlock(world, toMovePos)));

        if(curBlock == null && Config.debugTeleportMessages) LogHelper.info("Area " + (isCopying ? "copying" : "teleportation") + " complete.");

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
