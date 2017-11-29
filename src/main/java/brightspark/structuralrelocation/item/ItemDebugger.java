package brightspark.structuralrelocation.item;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.init.SRBlocks;
import brightspark.structuralrelocation.tileentity.TileAreaTeleporter;
import brightspark.structuralrelocation.util.CommonUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemDebugger extends ItemBasic
{
    public ItemDebugger()
    {
        super("debugger");
        setMaxStackSize(1);
    }

    public static void setTeleporterLoc(ItemStack stack, Location location)
    {
        stack.setTagInfo("location", location.serializeNBT());
    }

    public static Location getTeleporterLoc(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null) return null;
        NBTTagCompound locTag = tag.getCompoundTag("location");
        return locTag.getSize() == 0 ? null : new Location(locTag);
    }

    public static void clearTeleporterLoc(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null) return;
        tag.removeTag("location");
    }

    /**
     * This is called when the item is used, before the block is activated.
     * @return Return PASS to allow vanilla handling, any other to skip normal code.
     */
    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        if(world.isRemote)
        {
            if(player instanceof EntityPlayerSP)
                //Send a packet to process this on the server, because MC won't do it if I return anything other than PASS
                ((EntityPlayerSP) player).connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, side, hand, hitX, hitY, hitZ));
            return EnumActionResult.SUCCESS;
        }

        //Set teleporter position
        if(world.getTileEntity(pos) instanceof TileAreaTeleporter)
        {
            setTeleporterLoc(stack, new Location(world, pos));
            player.sendMessage(new TextComponentString("Linked to teleporter"));
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if(playerIn.isSneaking())
        {
            //Clear teleporter position
            clearTeleporterLoc(itemStackIn);
            if(worldIn.isRemote)
                playerIn.sendMessage(new TextComponentString("Cleared linked teleporter"));
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
        }
        return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        Location location = getTeleporterLoc(stack);
        if(location == null)
            tooltip.add("Not linked to an " + SRBlocks.areaTeleporter.getLocalizedName() + "!");
        else
        {
            tooltip.add(TextFormatting.GOLD + "Dimension ID: " + TextFormatting.GRAY + location.dimensionId);
            tooltip.add(TextFormatting.GOLD + "Position: " + CommonUtils.posToString(location.position));
        }
    }
}
