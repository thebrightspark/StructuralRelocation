package brightspark.structuralrelocation.gui;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.LocationArea;
import brightspark.structuralrelocation.message.MessageUpdateClientContainer;
import brightspark.structuralrelocation.message.MessageUpdateTeleporterCurBlock;
import brightspark.structuralrelocation.message.MessageUpdateTeleporterLocation;
import brightspark.structuralrelocation.tileentity.AbstractTileTeleporter;
import brightspark.structuralrelocation.tileentity.TileAreaTeleporter;
import brightspark.structuralrelocation.tileentity.TileSingleTeleporter;
import brightspark.structuralrelocation.util.CommonUtils;
import brightspark.structuralrelocation.util.LogHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;

public class ContainerTeleporter extends Container
{
    protected AbstractTileTeleporter teleporter;
    protected int[] cachedFields;
    protected Location cachedLocation;
    protected LocationArea cachedArea;
    protected BlockPos cachedCurBlock;
    protected int slotI = 0;
    protected int invStartX = 8;
    protected int invStartY = 93;

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
    protected void addSlots() {}

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return teleporter.isUseableByPlayer(playerIn);
    }

    private boolean handleCachedField(int index)
    {
        int teValue;
        switch(index)
        {
            case 0:
                teValue = teleporter.energy.getEnergyStored();
                break;
            default:
                LogHelper.warn("Unhandled server container data for ID " + index + "!");
                return false;
        }

        boolean result;
        if(result = cachedFields[index] != teValue)
            cachedFields[index] = teValue;
        return result;
    }

    /**
     * Looks for changes made in the container, sends them to every listener.
     */
    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        if(cachedFields == null)
        {
            cachedFields = new int[1];
            //Fill the array with -1s rather than 0s so that a field value of 0 can be detected when the container is opened
            Arrays.fill(cachedFields, -1);
        }

        for(IContainerListener listener : listeners)
        {
            //Cached fields
            for(int i = 0; i < cachedFields.length; i++)
                if(handleCachedField(i))
                {
                    //If the data is bigger than a short, then send over a custom, larger packet.
                    if(cachedFields[i] > Short.MAX_VALUE || cachedFields[i] < Short.MIN_VALUE)
                        CommonUtils.NETWORK.sendTo(new MessageUpdateClientContainer(i, cachedFields[i]), (EntityPlayerMP) listener);
                    else
                        listener.sendProgressBarUpdate(this, i, cachedFields[i]);
                }

            //Locations
            if(teleporter instanceof TileAreaTeleporter)
            {
                TileAreaTeleporter areaTP = (TileAreaTeleporter) teleporter;
                Location tpLocation = areaTP.getTarget();
                if((cachedLocation == null || !cachedLocation.isEqual(tpLocation)) && tpLocation != null)
                {
                    cachedLocation = tpLocation;
                    CommonUtils.NETWORK.sendTo(new MessageUpdateTeleporterLocation(cachedLocation), (EntityPlayerMP) listener);
                }
                LocationArea tpArea = areaTP.getAreaToMove();
                if((cachedArea == null || !cachedArea.isEqual(tpArea)) && tpArea != null)
                {
                    cachedArea = tpArea;
                    CommonUtils.NETWORK.sendTo(new MessageUpdateTeleporterLocation(cachedArea), (EntityPlayerMP) listener);
                }
                BlockPos curBlock = areaTP.getCurBlock();
                if(cachedCurBlock == null || !cachedCurBlock.equals(curBlock))
                {
                    cachedCurBlock = curBlock;
                    CommonUtils.NETWORK.sendTo(new MessageUpdateTeleporterCurBlock(cachedCurBlock), (EntityPlayerMP) listener);
                }
            }
            else
            {
                Location location = ((TileSingleTeleporter) teleporter).getTarget();
                if((cachedLocation == null || !cachedLocation.isEqual(location)) && location != null)
                {
                    cachedLocation = location;
                    CommonUtils.NETWORK.sendTo(new MessageUpdateTeleporterLocation(cachedLocation), (EntityPlayerMP) listener);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data)
    {
        switch(id)
        {
            case 0:
                teleporter.energy.setEnergyStored(data);
                break;
            default:
                LogHelper.warn("Unhandled client container data for ID " + id + "!");
        }
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
