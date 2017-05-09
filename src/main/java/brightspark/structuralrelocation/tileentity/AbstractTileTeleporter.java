package brightspark.structuralrelocation.tileentity;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.SREnergyStorage;
import brightspark.structuralrelocation.util.LogHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

public abstract class AbstractTileTeleporter extends TileEntity
{
    public SREnergyStorage energy = new SREnergyStorage(1000000, 1000, 0);
    protected int energyPerTeleport = 100;

    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return world.getTileEntity(pos) == this && player.getDistanceSq((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D) <= 64.0D;
    }

    /**
     * Tries to start the teleporting
     * Player argument is the one who activated the block, and is used to send messages to
     */
    public abstract void teleport(EntityPlayer player);

    /**
     * Teleports the block in the given world and position to the given location
     */
    protected void teleportBlock(BlockPos from, Location to)
    {
        teleportBlock(from, to, true);
    }

    protected void teleportBlock(BlockPos from, Location to, boolean moveTileEntities)
    {
        //TODO: Handle moveTileEntities
        useEnergy();
        IBlockState state = world.getBlockState(from);
        TileEntity te = world.getTileEntity(from);
        TileEntity newTe = null;
        if(te != null)
        {
            try
            {
                //Try and copy the tile entity
                newTe = te.getClass().newInstance();
                NBTTagCompound teNbt = te.serializeNBT();
                newTe.deserializeNBT(teNbt);
                newTe.markDirty();
            }
            catch(Exception e)
            {
                LogHelper.error("Couldn't create a new instance of the TileEntity at " + from.toString());
                e.printStackTrace();
            }
        }

        //Set the new block and tile entity
        WorldServer worldTo = world.getMinecraftServer().worldServerForDimension(to.dimensionId);
        worldTo.setBlockState(to.position, state);
        worldTo.setTileEntity(to.position, newTe);
        //Remove the old block and tile entity
        world.removeTileEntity(from);
        world.setBlockToAir(from);
    }

    protected void copyBlock(BlockPos from, Location to)
    {
        //TODO: Copy block method
    }

    public int getEnergyStored()
    {
        return energy.getEnergyStored();
    }

    public int getMaxEnergyStored()
    {
        return energy.getMaxEnergyStored();
    }

    /**
     * Returns a value between 0 and 1 representing how full the energy storage is
     */
    public float getEnergyPercent()
    {
        return (float) energy.getEnergyStored() / (float) energy.getMaxEnergyStored();
    }

    public boolean hasEnoughEnergy()
    {
        return energy.getEnergyStored() >= energyPerTeleport;
    }

    protected void useEnergy()
    {
        energy.modifyEnergy(-energyPerTeleport);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if(capability == CapabilityEnergy.ENERGY)
            return (T) energy;
        return super.getCapability(capability, facing);
    }
}
