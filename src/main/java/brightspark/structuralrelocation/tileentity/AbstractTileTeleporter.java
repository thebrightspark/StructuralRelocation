package brightspark.structuralrelocation.tileentity;

import brightspark.structuralrelocation.SREnergyStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

public abstract class AbstractTileTeleporter extends TileEntity
{
    public SREnergyStorage energy = new SREnergyStorage(1000000, 1000, 0);
    protected int energyPerTeleport = 100;

    /**
     * Tries to start the teleporting
     * Player argument is the one who activated the block, and is used to send messages to
     */
    public abstract void teleport(EntityPlayer player);

    protected boolean hasEnoughEnergy()
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
