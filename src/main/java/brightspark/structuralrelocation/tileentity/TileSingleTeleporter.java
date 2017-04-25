package brightspark.structuralrelocation.tileentity;

import brightspark.structuralrelocation.Location;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.IFluidBlock;

public class TileSingleTeleporter extends TileEntity implements ITeleporter
{
    private Location target;

    public TileSingleTeleporter() {}

    public void setTarget(Location location)
    {
        target = location;
    }

    private boolean canTeleport()
    {
        BlockPos blockPos = pos.up();
        IBlockState state = worldObj.getBlockState(blockPos);
        return target != null && state.getMaterial() != Material.AIR && !(state.getBlock() instanceof IFluidBlock) && state.getBlockHardness(worldObj, blockPos) >= 0;
    }

    @Override
    public void teleport(EntityPlayer player)
    {
        //Called from the block when right clicked
        if(worldObj.isRemote || !canTeleport()) return;
        IBlockState state = worldObj.getBlockState(pos.up());
        WorldServer server = worldObj.getMinecraftServer().worldServerForDimension(target.dimensionId);
        server.setBlockState(target.position, state);
        worldObj.setBlockToAir(pos.up());
        player.addChatMessage(new TextComponentString("Block Teleported"));
    }
}
