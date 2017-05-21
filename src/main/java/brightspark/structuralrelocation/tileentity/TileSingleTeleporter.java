package brightspark.structuralrelocation.tileentity;

import brightspark.structuralrelocation.Config;
import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.util.LogHelper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.IFluidBlock;

public class TileSingleTeleporter extends AbstractTileTeleporter
{
    private Location target;

    public void setTarget(Location location)
    {
        target = location;
        markDirty();
    }

    public Location getTarget()
    {
        return target;
    }

    private boolean canTeleport()
    {
        WorldServer worldTo = world.getMinecraftServer().worldServerForDimension(target.dimensionId);
        return target != null && hasEnoughEnergy() && isDestinationClear(worldTo, target.position);
    }

    private boolean doPreActionChecks()
    {
        if(world.isRemote) return false;
        if(!canTeleport())
        {
            if(Config.debugTeleportMessages) LogHelper.info("Can not teleport. Either no target set or not enough power.");
            return false;
        }
        return true;
    }

    @Override
    public void teleport(EntityPlayer player)
    {
        super.teleport(player);
        if(doPreActionChecks())
        {
            teleportBlock(pos.up(), target);
            if(Config.debugTeleportMessages) LogHelper.info("Block Teleported");
        }
    }

    @Override
    public void copy(EntityPlayer player)
    {
        super.copy(player);
        if(doPreActionChecks())
        {
            copyBlock(pos.up(), target);
            if(Config.debugTeleportMessages) LogHelper.info("Block Copied");
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
