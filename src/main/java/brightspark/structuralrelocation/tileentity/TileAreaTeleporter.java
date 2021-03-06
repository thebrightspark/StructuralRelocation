package brightspark.structuralrelocation.tileentity;

import brightspark.structuralrelocation.*;
import brightspark.structuralrelocation.message.MessageUpdateClientTeleporterObstruction;
import brightspark.structuralrelocation.util.CommonUtils;
import brightspark.structuralrelocation.util.LocCheckResult;
import brightspark.structuralrelocation.util.TeleporterStatus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

public class TileAreaTeleporter extends AbstractTileTeleporter implements ITickable
{
    private IterableArea areaToMove = new IterableArea();
    private Location target;
    public BlockPos lastBlockInTheWay;

    private boolean checkedEnergy = false;

    public void setAreaToMove(LocationArea area)
    {
        if(area == null) return;
        areaToMove.setArea(area);
        markDirty();
    }

    public LocationArea getAreaToMove()
    {
        return areaToMove.getArea();
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
        areaToMove.setCurPos(pos);
        markDirty();
    }

    public BlockPos getCurBlock()
    {
        return areaToMove.getCurPos();
    }

    @Override
    public Location getFromLoc()
    {
        BlockPos posFrom = getCurBlock() == null ? areaToMove.getArea() == null ? null : areaToMove.getArea().getMin() : getCurBlock();
        return posFrom == null ? null : new Location(world, posFrom);
    }

    @Override
    public Location getToLoc()
    {
        return areaToMove.getCurPos() == null ? target : target == null ? null : new Location(target.dimensionId, target.position.add(areaToMove.getCurPosOffset()));
    }

    public boolean isActive()
    {
        return areaToMove.getCurPos() != null;
    }

    public boolean hasEnoughEnergy()
    {
        return hasEnoughEnergy(getFromLoc(), getToLoc());
    }

    @Override
    public TeleporterStatus getStatus()
    {
        return isActive() ? super.getStatus() : TeleporterStatus.OFF;
    }

    private boolean doPreActionChecks()
    {
        if(world.isRemote) return false;
        if(target == null || areaToMove.getArea() == null)
        {
            if(SRConfig.server.debugTeleportMessages)
                StructuralRelocation.LOGGER.info("Can not teleport. Either no target set or no area set.");
            return false;
        }

        BlockPos destinationStart = target.position;
        BlockPos destinationEnd = destinationStart.add(areaToMove.getArea().getRelativeEndPoint());

        //Check that the target area is completely clear
        EntityPlayer player = getLastPlayer();
        WorldServer targetDim = world.getMinecraftServer().getWorld(target.dimensionId);
        for(BlockPos checkPos : BlockPos.getAllInBox(destinationStart, destinationEnd))
        {
            switch(checkDestination(new Location(targetDim, checkPos)))
            {
                case PASS:
                    lastBlockInTheWay = checkPos;
                    if(player != null)
                    {
                        sendMessageToPlayer(new TextComponentString(String.format("Target area is not clear!\nPosition 1: %s\nPosition 2: %s\nFound block ", destinationStart, destinationEnd))
                            .appendSibling(new TextComponentTranslation(targetDim.getBlockState(checkPos).getBlock().getTranslationKey() + ".name"))
                            .appendText(" at " + checkPos.toString()));
                    }
                    //Update client teleporter so the Debugger item can be used
                    CommonUtils.NETWORK.sendToAll(new MessageUpdateClientTeleporterObstruction(pos, checkPos));
                    if(SRConfig.server.debugTeleportMessages)
                        StructuralRelocation.LOGGER.info("Can not teleport. Destination area contains an obstruction at " + checkPos.toString() + " in dimension " + target.dimensionId);
                    return false;
                case WAIT:
                    if(player != null)
                        sendMessageToPlayer(new TextComponentString(String.format("Target area is not all loaded!\nPosition 1: %s\nPosition 2: %s\nFound block position not loaded: %s", destinationStart, destinationEnd, checkPos)));
                    if(SRConfig.server.debugTeleportMessages)
                        StructuralRelocation.LOGGER.info("Can not teleport. Destination area contains an unloaded chunk at block pos " + checkPos.toString() + " in dimension " + target.dimensionId);
                    return false;
            }
        }

        lastBlockInTheWay = null;

        //Start an area teleport
        areaToMove.resetCurPos();
        return true;
    }

    @Override
    public void teleport(UUID playerUuid)
    {
        super.teleport(playerUuid);
        if(doPreActionChecks() && SRConfig.server.debugTeleportMessages)
            StructuralRelocation.LOGGER.info("Area teleportation successfully started.");
    }

    @Override
    public void copy(UUID playerUuid)
    {
        super.copy(playerUuid);
        if(doPreActionChecks() && SRConfig.server.debugTeleportMessages)
            StructuralRelocation.LOGGER.info("Area copy successfully started.");
    }

    /**
     * If the teleporter is running, then stop it
     */
    public void stop()
    {
        areaToMove.setCurPos(null);
        markDirty();
        if(SRConfig.server.debugTeleportMessages) StructuralRelocation.LOGGER.info("Teleportation stopped.");
    }

    @Override
    public void update()
    {
        if(world.isRemote || areaToMove.getCurPos() == null || target == null) return;

        //If waiting for a chunk to load, then do nothing
        if(waitTicks > 0)
        {
            waitTicks--;
            return;
        }

        for(int i = 0; i < SRConfig.server.numBlockPerTick; i++) {
            if (isFinished())
                return;

            Location from = new Location(world, areaToMove.getCurPos());

            //Skip air and unbreakable blocks
            while (areaToMove.getCurPos() != null && checkSource(from, isCopying) == LocCheckResult.PASS) {
                areaToMove.next();
                from.position = areaToMove.getCurPos();
            }

            markDirty();

            if (isFinished())
                return;

            Location to = new Location(target.world, target.position.add(areaToMove.getCurPosOffset()));
            if (!hasEnoughEnergy(from, to)) {
                if (SRConfig.server.debugTeleportMessages && !checkedEnergy) {
                    StructuralRelocation.LOGGER.info("Can not teleport. Not enough power.");
                    checkedEnergy = true;
                }
                return;
            }

            checkedEnergy = false;

            //Teleport the block
            if (isCopying)
                copyBlock(from, to);
            else
                teleportBlock(from, to);
            //If not waiting for an unloaded chunk, then go to next pos
            if (waitTicks <= 0)
                areaToMove.next();
            else
                return;
        }
    }

    private boolean isFinished()
    {
        boolean finished = areaToMove.getCurPos() == null;
        if (finished && SRConfig.server.debugTeleportMessages)
            StructuralRelocation.LOGGER.info("Area " + (isCopying ? "copying" : "teleportation") + " complete.");
        return finished;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        //Read target
        if(nbt.hasKey("target")) target = new Location(nbt.getCompoundTag("target"));
        //Read area
        areaToMove = new IterableArea(nbt.getCompoundTag("area"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        //Write target
        if(target != null) nbt.setTag("target", target.serializeNBT());
        //Write area
        nbt.setTag("area", areaToMove.serializeNBT());

        return super.writeToNBT(nbt);
    }
}
