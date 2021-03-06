package brightspark.structuralrelocation.gui;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.LocationArea;
import brightspark.structuralrelocation.StructuralRelocation;
import brightspark.structuralrelocation.message.MessageUpdateClientContainer;
import brightspark.structuralrelocation.message.MessageUpdateTeleporterCurBlock;
import brightspark.structuralrelocation.message.MessageUpdateTeleporterLocation;
import brightspark.structuralrelocation.tileentity.AbstractTileTeleporter;
import brightspark.structuralrelocation.tileentity.TileAreaTeleporter;
import brightspark.structuralrelocation.tileentity.TileSingleTeleporter;
import brightspark.structuralrelocation.util.CommonUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerTeleporter extends Container
{
    private AbstractTileTeleporter teleporter;
    private int cachedEnergy = -1;
    private Location cachedLocation;
    private LocationArea cachedArea;
    private BlockPos cachedCurBlock;
    private boolean cachedChatWarnings;
    private int invStartX = 8;
    private int invStartY = 93;

    public ContainerTeleporter(InventoryPlayer invPlayer, AbstractTileTeleporter teleporter)
    {
        this.teleporter = teleporter;
        init();
        addSlots();
        bindPlayerInventory(invPlayer);
    }

    /**
     * Called first in the constructor for anything which cannot be done in the constructor.
     */
    protected void init() {}

    /**
     * Called after init() to add slots to the container.
     */
    protected void addSlots()
    {
        addSlotToContainer(new SlotCamo(teleporter.camoInv, 155, 5));
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return teleporter.isUsableByPlayer(playerIn);
    }

    /**
     * Looks for changes made in the container, sends them to every listener.
     */
    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        for(IContainerListener listener : listeners)
        {
            if(!(listener instanceof EntityPlayerMP))
                return;

            EntityPlayerMP player = (EntityPlayerMP) listener;

            //Energy
            int energy = teleporter.getEnergyStored();
            if(energy != cachedEnergy)
            {
                cachedEnergy = energy;
                CommonUtils.NETWORK.sendTo(new MessageUpdateClientContainer(0, energy), player);
            }

            //Chat Warnings
            if(teleporter.chatWarnings != cachedChatWarnings)
            {
                cachedChatWarnings = teleporter.chatWarnings;
                listener.sendWindowProperty(this, 1, cachedChatWarnings ? 1 : 0);
            }

            //Locations
            if(teleporter instanceof TileAreaTeleporter)
            {
                TileAreaTeleporter areaTP = (TileAreaTeleporter) teleporter;
                Location tpLocation = areaTP.getTarget();
                if((cachedLocation == null || !cachedLocation.isEqual(tpLocation)) && tpLocation != null)
                {
                    cachedLocation = tpLocation;
                    CommonUtils.NETWORK.sendTo(new MessageUpdateTeleporterLocation(cachedLocation), player);
                }
                LocationArea tpArea = areaTP.getAreaToMove();
                if((cachedArea == null || !cachedArea.isEqual(tpArea)) && tpArea != null)
                {
                    cachedArea = tpArea;
                    CommonUtils.NETWORK.sendTo(new MessageUpdateTeleporterLocation(cachedArea), player);
                }
                BlockPos curBlock = areaTP.getCurBlock();
                if(cachedCurBlock == null || !cachedCurBlock.equals(curBlock))
                {
                    cachedCurBlock = curBlock;
                    CommonUtils.NETWORK.sendTo(new MessageUpdateTeleporterCurBlock(cachedCurBlock), player);
                }
            }
            else
            {
                Location location = ((TileSingleTeleporter) teleporter).getTarget();
                if((cachedLocation == null || !cachedLocation.isEqual(location)) && location != null)
                {
                    cachedLocation = location;
                    CommonUtils.NETWORK.sendTo(new MessageUpdateTeleporterLocation(cachedLocation), player);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data)
    {
        switch (id)
        {
            case 0:
                teleporter.setEnergy(data);
                break;
            case 1:
                teleporter.chatWarnings = data == 1;
                break;
            default:
                StructuralRelocation.LOGGER.warn("Unhandled client container data for ID {}!", id);
        }
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (slotId == 0 && (clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE)) {
            Slot slot = inventorySlots.get(slotId);
            if (slot instanceof SlotCamo) {
                ItemStack heldStack = player.inventory.getItemStack();
                if (heldStack.isEmpty())
                    slot.putStack(ItemStack.EMPTY);
                else if (slot.isItemValid(heldStack)) {
                    switch (clickTypeIn) {
                        case PICKUP:
                            slot.putStack(heldStack.isEmpty() ? ItemStack.EMPTY : heldStack.copy());
                            break;
                        case QUICK_MOVE:
                            slot.putStack(ItemStack.EMPTY);
                    }
                }
                return ItemStack.EMPTY;
            }
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        if (index != 0) {
            Slot slot = inventorySlots.get(0);
            if (slot instanceof SlotCamo) {
                ItemStack stack = inventorySlots.get(index).getStack();
                if (slot.isItemValid(stack))
                    slot.putStack(stack);
            }
        }
        return ItemStack.EMPTY;
    }

    @SideOnly(Side.CLIENT)
    public void updateTeleporter(Location location)
    {
        if(teleporter instanceof TileAreaTeleporter)
            ((TileAreaTeleporter) teleporter).setTarget(location);
        else
            ((TileSingleTeleporter) teleporter).setTarget(location);
    }

    @SideOnly(Side.CLIENT)
    public void updateTeleporter(LocationArea locationArea)
    {
        if(teleporter instanceof TileAreaTeleporter)
            ((TileAreaTeleporter) teleporter).setAreaToMove(locationArea);
    }

    @SideOnly(Side.CLIENT)
    public void updateTeleporter(BlockPos curBlock)
    {
        if(teleporter instanceof TileAreaTeleporter)
            ((TileAreaTeleporter) teleporter).setCurBlock(curBlock);
    }

    /**
     * Adds the player's inventory slots to the container. Called after addSlots().
     */
    protected void bindPlayerInventory(InventoryPlayer inventoryPlayer)
    {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, invStartX + j * 18, invStartY + i * 18));

        for (int i = 0; i < 9; i++)
            addSlotToContainer(new Slot(inventoryPlayer, i, invStartX + i * 18, invStartY + 18 * 3 + 4));
    }
}
