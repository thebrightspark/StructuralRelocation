package brightspark.structuralrelocation.block;

import brightspark.structuralrelocation.tileentity.TileAreaTeleporter;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockAreaTeleporter extends AbstractBlockTeleporter
{
    public BlockAreaTeleporter()
    {
        super("areaTeleporter", Material.ROCK);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileAreaTeleporter();
    }
}
