package brightspark.structuralrelocation.item;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.init.SRBlocks;
import brightspark.structuralrelocation.tileentity.TileAreaTeleporter;
import brightspark.structuralrelocation.util.CommonUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        //Set teleporter position
        if(world.getTileEntity(pos) instanceof TileAreaTeleporter)
        {
            setTeleporterLoc(player.getHeldItem(hand), new Location(world, pos));
            if(world.isRemote)
                player.sendMessage(new TextComponentString("Linked to teleporter"));
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        ItemStack stack = playerIn.getHeldItem(hand);
        if(playerIn.isSneaking())
        {
            //Clear teleporter position
            clearTeleporterLoc(stack);
            if(worldIn.isRemote)
                playerIn.sendMessage(new TextComponentString("Cleared linked teleporter"));
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
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
