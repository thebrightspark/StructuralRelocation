package brightspark.structuralrelocation.block;

import brightspark.structuralrelocation.StructuralRelocation;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class AbstractBlockContainer extends BlockBasic implements ITileEntityProvider
{
    protected boolean hasGui = false;

    public AbstractBlockContainer(String name, Material mat)
    {
        super(name, mat);
        isBlockContainer = true;
    }

    public void setHasGui()
    {
        hasGui = true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(InventoryPlayer invPlayer, TileEntity te)
    {
        return null;
    }

    public Container getContainer(InventoryPlayer invPlayer, TileEntity te)
    {
        return null;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if(!hasGui)
            return super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);
        if(!playerIn.isSneaking())
            playerIn.openGui(StructuralRelocation.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }
}
