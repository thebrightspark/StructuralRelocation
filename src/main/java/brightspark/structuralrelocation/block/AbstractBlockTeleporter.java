package brightspark.structuralrelocation.block;

import brightspark.structuralrelocation.gui.ContainerTeleporter;
import brightspark.structuralrelocation.gui.GuiTeleporter;
import brightspark.structuralrelocation.item.ItemSelector;
import brightspark.structuralrelocation.tileentity.AbstractTileTeleporter;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class AbstractBlockTeleporter extends AbstractBlockContainer
{
    public static final IUnlistedProperty<IBlockState> PROP_CAMO = new IUnlistedProperty<IBlockState>() {
        @Override
        public String getName() {
            return "camo";
        }

        @Override
        public boolean isValid(IBlockState value) {
            return true;
        }

        @Override
        public Class<IBlockState> getType() {
            return IBlockState.class;
        }

        @Override
        public String valueToString(IBlockState value) {
            return value.toString();
        }
    };

    public AbstractBlockTeleporter(String name)
    {
        super(name, Material.IRON);
        setHasGui();
    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(InventoryPlayer invPlayer, TileEntity te)
    {
        return new GuiTeleporter(invPlayer, (AbstractTileTeleporter) te);
    }

    public Container getContainer(InventoryPlayer invPlayer, TileEntity te)
    {
        return new ContainerTeleporter(invPlayer, (AbstractTileTeleporter) te);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if(placer instanceof EntityPlayer && te instanceof AbstractTileTeleporter)
            ((AbstractTileTeleporter) te).setPlacedPlayer(placer.getUniqueID());
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        ItemStack heldItem = playerIn.getHeldItem(hand);
        //StructuralRelocation.LOGGER.info("Item Activated With: " + (heldItem == null ? "Null" : heldItem.toString()));
        if(heldItem.getItem() instanceof ItemSelector)
            return false;
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if(!(te instanceof AbstractTileTeleporter)) return;
        AbstractTileTeleporter teleporter = (AbstractTileTeleporter) te;
        boolean isPowered = worldIn.isBlockPowered(pos);
        if(!worldIn.isRemote && teleporter.hasPowerChanged(isPowered))
        {
            teleporter.setPowered(isPowered);
            //Only start teleportation when powered on
            if(isPowered)
                teleporter.teleport(teleporter.getPlacedPlayerUuid());
        }
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[] { PROP_CAMO });
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        if (!(state instanceof IExtendedBlockState)) return state;
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof AbstractTileTeleporter)) return state;
        IBlockState camo = ((AbstractTileTeleporter) te).camoInv.getBlockState();
        return camo == null ? state : ((IExtendedBlockState) state).withProperty(PROP_CAMO, camo);
    }
}
