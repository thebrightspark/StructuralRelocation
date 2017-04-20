package brightspark.structuralrelocation.tileentity;

import brightspark.structuralrelocation.Location;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.IFluidBlock;

public class TileSingleTeleporter extends TileEntity
{
    private Location target;

    public TileSingleTeleporter() {}

    public boolean canTeleport()
    {
        IBlockState state = worldObj.getBlockState(pos.up());
        return target != null && state.getMaterial() != Material.AIR && !(state.getBlock() instanceof IFluidBlock);
    }

    public void teleport()
    {
        //Called from the block when right clicked
        if(worldObj.isRemote || !canTeleport()) return;
        IBlockState state = worldObj.getBlockState(pos.up());
        WorldServer server = worldObj.getMinecraftServer().worldServerForDimension(target.dimensionId);
        server.setBlockState(target.position, state);
        worldObj.setBlockToAir(pos.up());
    }

    public void setTarget(Location location)
    {
        target = location;
    }
}
