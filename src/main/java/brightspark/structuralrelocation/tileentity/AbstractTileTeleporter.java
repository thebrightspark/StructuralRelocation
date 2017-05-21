package brightspark.structuralrelocation.tileentity;

import brightspark.structuralrelocation.Config;
import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.SREnergyStorage;
import brightspark.structuralrelocation.util.LogHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.IFluidBlock;

public abstract class AbstractTileTeleporter extends TileEntity
{
    protected EntityPlayer lastPlayer;
    public SREnergyStorage energy;


    public AbstractTileTeleporter()
    {
        initEnergy();
    }

    public void initEnergy()
    {
        energy = new SREnergyStorage(1000000, 1000, 0);
    }

    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return world.getTileEntity(pos) == this && player.getDistanceSq((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D) <= 64.0D;
    }

    /**
     * Tries to start the teleporting
     * Player argument is the one who activated the block, and is used to send messages to
     */
    public void teleport(EntityPlayer player)
    {
        lastPlayer = player;
    }

    /**
     * Checks that the block can be teleported by the player who started the teleport
     */
    protected boolean canTeleportBlock(World world, BlockPos pos)
    {
        if(!world.isBlockModifiable(lastPlayer, pos) || world.isAirBlock(pos)) return false;
        IBlockState state = world.getBlockState(pos);
        return (lastPlayer.capabilities.isCreativeMode || state.getBlock().getBlockHardness(state, world, pos) >= 0) &&
                (!isFluid(world, pos) ||(isFluidSourceBlock(world, pos) && Config.canTeleportFluids));
    }

    /**
     * Checks if a block can be teleported to the location
     */
    protected boolean isDestinationClear(World world, BlockPos pos)
    {
        return world.isBlockModifiable(lastPlayer, pos) && (world.isAirBlock(pos) || world.getBlockState(pos).getBlock().isReplaceable(world, pos)) && !isFluidSourceBlock(world, pos);
    }

    /**
     * Checks if the block is a fluid
     */
    public static boolean isFluid(World world, BlockPos pos)
    {
        Block block = world.getBlockState(pos).getBlock();
        return block instanceof IFluidBlock || block instanceof BlockLiquid;
    }

    /**
     * Checks if the block is a source block of a fluid
     */
    public static boolean isFluidSourceBlock(World world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        return block instanceof IFluidBlock && ((IFluidBlock) block).canDrain(world, pos) ||
                block instanceof BlockLiquid && state.getValue(BlockLiquid.LEVEL) == 0;
    }

    /**
     * Teleports the block to the given location
     */
    protected void teleportBlock(BlockPos from, Location to)
    {
        teleportBlock(from, world.getMinecraftServer().worldServerForDimension(to.dimensionId), to.position);
    }

    /**
     * Teleports the block to the given location
     */
    protected void teleportBlock(BlockPos from, World worldTo, BlockPos to)
    {
        teleportBlock(from, worldTo, to, true);
    }

    /**
     * Teleports the block to the given location
     */
    protected void teleportBlock(BlockPos from, World worldTo, BlockPos to, boolean moveTileEntities)
    {
        if(!canTeleportBlock(world, from))
        {
            if(Config.debugTeleportMessages) LogHelper.info("Not able to teleport block at " + from.toString() + ". Either no permission, is air, is a fluid and config disallows it, or player is not creative and block is unbreakable.");
            return;
        }
        IBlockState state = world.getBlockState(from);
        TileEntity te = world.getTileEntity(from);
        //If not moving tile entities, and this block has one, then don't move it
        if(!moveTileEntities && te != null) return;
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
        worldTo.setBlockState(to, state);
        worldTo.setTileEntity(to, newTe);
        //Remove the old block and tile entity
        world.removeTileEntity(from);
        world.setBlockToAir(from);
        useEnergy();
        if(Config.debugTeleportMessages) LogHelper.info("Successfully teleported block from " + from.toString() + " to " + to.toString() + " in dimension " + worldTo.provider.getDimension());
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
        return energy.getEnergyStored() >= Config.energyPerBlockTeleport;
    }

    protected void useEnergy()
    {
        energy.modifyEnergy(-Config.energyPerBlockTeleport);
        markDirty();
    }

    public void setEnergy(int amount)
    {
        if(energy.getEnergyStored() == 0 && energy.getMaxEnergyStored() == 0)
            initEnergy();
        energy.setEnergyStored(amount);
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

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        //Read energy
        energy.deserializeNBT(nbt.getCompoundTag("energy"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        //Write energy
        nbt.setTag("energy", energy.serializeNBT());

        return super.writeToNBT(nbt);
    }
}
