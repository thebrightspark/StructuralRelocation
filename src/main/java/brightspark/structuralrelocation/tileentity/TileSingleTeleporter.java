package brightspark.structuralrelocation.tileentity;

import brightspark.structuralrelocation.Location;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;

public class TileSingleTeleporter extends TileEntity
{
    private Location target;

    public TileSingleTeleporter() {}

    public void teleport()
    {
        //Called from the block when right clicked
        if(worldObj.isRemote) return;
        IBlockState state = worldObj.getBlockState(pos.up());
        if(state.getMaterial() == Material.AIR || target == null) return;
        WorldServer server = worldObj.getMinecraftServer().worldServerForDimension(target.dimensionId);
        server.setBlockState(target.position, state);
        worldObj.setBlockToAir(pos.up());
    }

    public void setTarget(Location location)
    {
        target = location;
    }
}
