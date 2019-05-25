package brightspark.structuralrelocation.tileentity;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.SRConfig;
import brightspark.structuralrelocation.SREnergyStorage;
import brightspark.structuralrelocation.StructuralRelocation;
import brightspark.structuralrelocation.message.MessageSpawnParticleBlock;
import brightspark.structuralrelocation.util.CommonUtils;
import brightspark.structuralrelocation.util.LocCheckResult;
import brightspark.structuralrelocation.util.TeleporterStatus;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.UUID;

public abstract class AbstractTileTeleporter extends TileEntity
{
    private UUID lastPlayerUUID, placedPlayerUUID;
    private EntityPlayer lastPlayer, placedPlayer;
    /** True = copying, False = teleporting **/
    protected boolean isCopying, isPowered;
    protected int waitTicks = 0;
    public SREnergyStorage energy;

    public AbstractTileTeleporter()
    {
        initEnergy();
    }

    public void initEnergy()
    {
        energy = new SREnergyStorage(1000000, 5000, 0);
    }

    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return world.getTileEntity(pos) == this && player.getDistanceSq((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D) <= 64.0D;
    }

    /**
     * Gets the last player to start a teleport/copy
     */
    public EntityPlayer getLastPlayer()
    {
        if(lastPlayer != null) return lastPlayer;
        if(lastPlayerUUID != null) return lastPlayer = world.getPlayerEntityByUUID(lastPlayerUUID);
        if(world instanceof WorldServer) return lastPlayer = FakePlayerFactory.getMinecraft((WorldServer) world);
        return null;
    }

    public void setLastPlayer(EntityPlayer player)
    {
        lastPlayer = player;
        lastPlayerUUID = player.getUniqueID();
    }

    public EntityPlayer getPlacedPlayer()
    {
        if(placedPlayer != null) return placedPlayer;
        if(placedPlayerUUID != null) return placedPlayer = world.getPlayerEntityByUUID(placedPlayerUUID);
        if(world instanceof WorldServer) return placedPlayer = FakePlayerFactory.getMinecraft((WorldServer) world);
        return null;
    }

    public void setPlacedPlayer(EntityPlayer player)
    {
        placedPlayer = player;
        placedPlayerUUID = player.getUniqueID();
    }

    public void setPowered(boolean powered)
    {
        isPowered = powered;
    }

    public boolean hasPowerChanged(boolean powered)
    {
        return powered != isPowered;
    }

    public boolean isWaiting()
    {
        return waitTicks > 0;
    }

    public abstract Location getFromLoc();

    public abstract Location getToLoc();

    public TeleporterStatus getStatus()
    {
        if(getFromLoc() == null || getToLoc() == null)
            return TeleporterStatus.TARGETS;
        if(!hasEnoughEnergy())
            return TeleporterStatus.ENERGY;
        if(isWaiting())
            return TeleporterStatus.WAITING;
        return TeleporterStatus.ON;
    }

    /**
     * Tries to start the teleporting
     * Player argument is the one who activated the block, and is used to send messages to
     */
    public void teleport(EntityPlayer player)
    {
        setLastPlayer(player);
        isCopying = false;
    }

    /**
     * Tries to start the copying
     * Player argument is the one who activated the block, and is used to send messages to
     */
    public void copy(EntityPlayer player)
    {
        setLastPlayer(player);
        isCopying = true;
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

    protected LocCheckResult checkSource(Location location, boolean copy)
    {
        World worldIn = location.world;
        IBlockState state = location.getBlockState();
        BlockPos posIn = location.position;
        String blockName = state.getBlock().getRegistryName().toString();
        if(!worldIn.isBlockLoaded(posIn))
        {
            if(SRConfig.debugTeleportMessages)
                StructuralRelocation.LOGGER.info("Can not teleport/copy block {} at {} -> Chunk not loaded.", blockName, posIn);
            return LocCheckResult.WAIT;
        }
        if(worldIn.isAirBlock(posIn))
        {
            if(SRConfig.debugTeleportMessages)
                StructuralRelocation.LOGGER.info("Can not teleport/copy block {} at {} -> Is an air block.", blockName, posIn);
            return LocCheckResult.PASS;
        }
        if((getLastPlayer() != null && !getLastPlayer().capabilities.isCreativeMode) && state.getBlock().getBlockHardness(state, worldIn, posIn) < 0)
        {
            if(SRConfig.debugTeleportMessages)
                StructuralRelocation.LOGGER.info("Can not teleport/copy block {} at {} -> Block is unbreakable.", blockName, posIn);
            return LocCheckResult.PASS;
        }
        if(isFluidSourceBlock(worldIn, posIn) && !SRConfig.canTeleportFluids)
        {
            if(SRConfig.debugTeleportMessages)
                StructuralRelocation.LOGGER.info("Can not teleport/copy block {} at {} -> Block is a fluid source.", blockName, posIn);
            return LocCheckResult.PASS;
        }

        //Fire BreakEvent if teleporting
        if(!copy && MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(worldIn, posIn, state, getLastPlayer())))
        {
            if(SRConfig.debugTeleportMessages)
                StructuralRelocation.LOGGER.info("Can not teleport block {} at {} -> BreakEvent cancelled.", blockName, posIn);
            return LocCheckResult.PASS;
        }
        return LocCheckResult.SUCCESS;
    }

    protected LocCheckResult checkDestination(Location location)
    {
        World worldIn = location.world;
        IBlockState state = location.getBlockState();
        BlockPos posIn = location.position;
        int dimensionId = location.dimensionId;
        String blockName = state.getBlock().getRegistryName().toString();

        if(!worldIn.isBlockLoaded(posIn))
        {
            if(SRConfig.debugTeleportMessages)
                StructuralRelocation.LOGGER.info("Can not teleport to {} in dimension {} -> Chunk not loaded.", posIn, dimensionId);
            return LocCheckResult.WAIT;
        }
        if(!worldIn.isBlockModifiable(getLastPlayer(), posIn))
        {
            if(SRConfig.debugTeleportMessages)
                StructuralRelocation.LOGGER.info("Can not teleport to {} at {} in dimension {} -> No permission to modify destination.", blockName, posIn, dimensionId);
            return LocCheckResult.PASS;
        }
        if((!worldIn.isAirBlock(posIn) && !worldIn.getBlockState(posIn).getBlock().isReplaceable(worldIn, posIn)) || isFluidSourceBlock(worldIn, posIn))
        {
            if(SRConfig.debugTeleportMessages)
                StructuralRelocation.LOGGER.info("Can not teleport to {} at {} in dimension {} -> Destination block is not air, not replaceable or is a fluid source block.", blockName, posIn, dimensionId);
            return LocCheckResult.PASS;
        }

        BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(worldIn, posIn);
        if(ForgeEventFactory.onPlayerBlockPlace(getLastPlayer(), snapshot, EnumFacing.UP, EnumHand.MAIN_HAND).isCanceled())
        {
            snapshot.restore(true, false);
            if(SRConfig.debugTeleportMessages)
                StructuralRelocation.LOGGER.info("Can not teleport to {} in dimension {} -> PlaceEvent cancelled.", blockName, posIn);
            return LocCheckResult.PASS;
        }
        return LocCheckResult.SUCCESS;
    }

    /**
     * Teleports the block to the given location
     */
    protected void teleportBlock(Location from, Location to)
    {
        if(doTeleporterAction(from, to, true, true) && SRConfig.debugTeleportMessages)
            StructuralRelocation.LOGGER.info("Successfully teleported block from {} to {}", from, to);
    }

    /**
     * Copy the block to the given location
     */
    protected void copyBlock(Location from, Location to)
    {
        if(doTeleporterAction(from, to, true, false) && SRConfig.debugTeleportMessages)
            StructuralRelocation.LOGGER.info("Successfully copied block from {} to {}", from, to);
    }

    protected boolean handleCheckResult(LocCheckResult result)
    {
        switch(result)
        {
            case WAIT:
                waitTicks = SRConfig.teleportWaitTicks;
            case PASS:
                return false;
            default:
                return true;
        }
    }

    private boolean doTeleporterAction(Location from, Location to, boolean handleTileEntities, boolean removeBlocks)
    {
        if(!handleCheckResult(checkSource(from, removeBlocks))) return false;
        IBlockState state = from.getBlockState();
        TileEntity te = from.getTE();
        //If not handling tile entities, and this block has one, then don't do anything to it
        if(!handleTileEntities && te != null) return false;
        if(!handleCheckResult(checkDestination(to))) return false;
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
                StructuralRelocation.LOGGER.error("Couldn't create a new instance of the TileEntity at " + from.toString());
                e.printStackTrace();
                return false;
            }
        }

        //Set the new block and tile entity
        to.setBlockState(state);
        if(newTe != null) to.setTE(newTe);
        if(removeBlocks)
        {
            //Remove the old block and tile entity
            from.removeTE();
            from.setBlockToAir();

            //Spawn particle for animation
            BlockPos fromPos = from.position;
            CommonUtils.NETWORK.sendToAllAround(new MessageSpawnParticleBlock(fromPos, false),
                new NetworkRegistry.TargetPoint(world.provider.getDimension(), fromPos.getX(), fromPos.getY(), fromPos.getZ(), 30D));
        }
        useEnergy(from, to);
        return true;
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

    public abstract boolean hasEnoughEnergy();

    public boolean hasEnoughEnergy(Location from, Location to)
    {
        return from != null && to != null && energy.getEnergyStored() >= calcEnergyCost(from, to);
    }

    protected void useEnergy(Location from, Location to)
    {
        energy.modifyEnergy(-calcEnergyCost(from, to));
        markDirty();
    }

    protected int calcEnergyCost(Location from, Location to)
    {
        int cost = SRConfig.energyPerBlockBase + (int) Math.ceil(SRConfig.energyPerDistanceMultiplier * 20F * Math.ceil(Math.log(from.distanceTo(to))));
        if(from.dimensionId != to.dimensionId)
            cost *= SRConfig.energyAcrossDimensionsMultiplier;
        return cost;
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
        //Read last player
        if(nbt.hasUniqueId("player")) lastPlayerUUID = nbt.getUniqueId("player");
        //Read placed player
        if(nbt.hasUniqueId("placed")) placedPlayerUUID = nbt.getUniqueId("placed");
        //Read if copying
        isCopying = nbt.getBoolean("isCopying");
        //Read if powered
        isPowered = nbt.getBoolean("isPowered");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        //Write energy
        nbt.setTag("energy", energy.serializeNBT());
        //Write last player
        if(lastPlayerUUID != null) nbt.setUniqueId("player", lastPlayerUUID);
        //Write placed player
        if(placedPlayerUUID != null) nbt.setUniqueId("placed", placedPlayerUUID);
        //Write if copying
        nbt.setBoolean("isCopying", isCopying);
        //Write if powered
        nbt.setBoolean("isPowered", isPowered);

        return super.writeToNBT(nbt);
    }
}
