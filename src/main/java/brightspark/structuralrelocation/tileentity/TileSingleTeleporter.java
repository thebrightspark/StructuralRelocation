package brightspark.structuralrelocation.tileentity;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.SRConfig;
import brightspark.structuralrelocation.StructuralRelocation;
import brightspark.structuralrelocation.util.LocCheckResult;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public class TileSingleTeleporter extends AbstractTileTeleporter
{
    private Location toTeleport;
    private Location target;

    @Override
    public void onLoad()
    {
        super.onLoad();
        toTeleport = new Location(world, pos.up());
    }

    public void setTarget(Location location)
    {
        target = location;
        markDirty();
    }

    public Location getTarget()
    {
        return target;
    }

    public boolean hasEnoughEnergy()
    {
        return hasEnoughEnergy(toTeleport, target);
    }

    private boolean doPreActionChecks()
    {
        if(world.isRemote) return false;
        if(target == null)
        {
            if(SRConfig.server.debugTeleportMessages)
                StructuralRelocation.LOGGER.info("Can not teleport - no target set");
            return false;
        }
        if(!hasEnoughEnergy(toTeleport, target))
        {
            if(SRConfig.server.debugTeleportMessages)
                StructuralRelocation.LOGGER.info("Can not teleport - not enough power.");
            return false;
        }
        return handleCheckResult(checkSource(toTeleport, isCopying)) && handleCheckResult(checkDestination(target));
    }

    @Override
    protected boolean handleCheckResult(LocCheckResult result)
    {
        return result == LocCheckResult.SUCCESS;
    }

    @Override
    public Location getFromLoc()
    {
        return toTeleport;
    }

    @Override
    public Location getToLoc()
    {
        return target;
    }

    @Override
    public void teleport(UUID playerUuid)
    {
        super.teleport(playerUuid);
        if(doPreActionChecks())
        {
            teleportBlock(toTeleport, target);
            if(SRConfig.server.debugTeleportMessages) StructuralRelocation.LOGGER.info("Block Teleported");
        }
    }

    @Override
    public void copy(UUID playerUuid)
    {
        super.copy(playerUuid);
        if(doPreActionChecks())
        {
            copyBlock(toTeleport, target);
            if(SRConfig.server.debugTeleportMessages) StructuralRelocation.LOGGER.info("Block Copied");
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        //Read target
        if(nbt.hasKey("target")) target = new Location(nbt.getCompoundTag("target"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        //Write target
        if(target != null) nbt.setTag("target", target.serializeNBT());

        return super.writeToNBT(nbt);
    }
}
