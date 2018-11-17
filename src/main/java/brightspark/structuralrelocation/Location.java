package brightspark.structuralrelocation;

import brightspark.structuralrelocation.util.CommonUtils;
import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

public class Location implements INBTSerializable<NBTTagCompound>
{
    public int dimensionId;
    public World world;
    public BlockPos position;

    public Location(int dimensionId, BlockPos position)
    {
        this.dimensionId = dimensionId;
        this.world = CommonUtils.getWorldByDimId(dimensionId);
        this.position = position;
    }

    public Location(World world, BlockPos position)
    {
        this.dimensionId = world.provider.getDimension();
        this.world = world;
        this.position = new BlockPos(position);
    }

    public Location(NBTTagCompound nbt)
    {
        deserializeNBT(nbt);
    }

    public boolean isEqual(Location location)
    {
        return location != null && location.dimensionId == dimensionId && location.position.equals(position);
    }

    public double distanceTo(Location location)
    {
        BlockPos pos = location.position;
        return position.getDistance(pos.getX(), pos.getY(), pos.getZ());
    }

    public boolean setBlock(Block block)
    {
        return setBlockState(block.getDefaultState());
    }

    public boolean setBlockState(IBlockState state)
    {
        return world.setBlockState(position, state);
    }

    public boolean setBlockToAir()
    {
        return world.setBlockToAir(position);
    }

    public IBlockState getBlockState()
    {
        return world.getBlockState(position);
    }

    public void setTE(TileEntity te)
    {
        world.setTileEntity(position, te);
    }

    public TileEntity getTE()
    {
        return world.getTileEntity(position);
    }

    public void removeTE()
    {
        world.removeTileEntity(position);
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("dimension", dimensionId);
        tag.setLong("position", position.toLong());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        dimensionId = tag.getInteger("dimension");
        world = CommonUtils.getWorldByDimId(dimensionId);
        position = BlockPos.fromLong(tag.getLong("position"));
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("dim", dimensionId)
                .add("pos", position).toString();
    }
}
