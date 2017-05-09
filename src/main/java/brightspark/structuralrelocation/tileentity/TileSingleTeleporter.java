package brightspark.structuralrelocation.tileentity;

import brightspark.structuralrelocation.Location;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fluids.IFluidBlock;

public class TileSingleTeleporter extends AbstractTileTeleporter
{
    private Location target;

    public TileSingleTeleporter() {}

    public void setTarget(Location location)
    {
        target = location;
    }

    public Location getTarget()
    {
        return target;
    }

    private boolean canTeleport()
    {
        BlockPos blockPos = pos.up();
        IBlockState state = world.getBlockState(blockPos);
        return target != null && state.getMaterial() != Material.AIR && !(state.getBlock() instanceof IFluidBlock) && state.getBlockHardness(world, blockPos) >= 0 && hasEnoughEnergy();
    }

    @Override
    public void teleport(EntityPlayer player)
    {
        //Called from the block when right clicked
        if(world.isRemote || !canTeleport()) return;
        teleportBlock(pos.up(), target);
        player.sendMessage(new TextComponentString("Block Teleported"));
    }
}
