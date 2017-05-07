package brightspark.structuralrelocation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public class Location implements INBTSerializable<NBTTagCompound>
{
    public int dimensionId;
    public BlockPos position;

    public Location(int dimensionId, BlockPos position)
    {
        this.dimensionId = dimensionId;
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
        position = BlockPos.fromLong(tag.getLong("position"));
    }
}
