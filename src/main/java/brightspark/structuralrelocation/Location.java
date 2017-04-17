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

    @Override
    public NBTTagCompound serializeNBT()
    {
        return saveToNBT(new NBTTagCompound());
    }

    public NBTTagCompound saveToNBT(NBTTagCompound tag)
    {
        tag.setInteger("dimension", dimensionId);
        tag.setLong("position", position.toLong());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        dimensionId = nbt.getInteger("dimension");
        position = BlockPos.fromLong(nbt.getLong("position"));
    }
}
