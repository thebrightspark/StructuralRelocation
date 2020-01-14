package brightspark.structuralrelocation.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class InventoryCamo implements IInventory {
    private AbstractTileTeleporter teleporter;
    private ItemStack stack = ItemStack.EMPTY;
    private IBlockState blockState = null;

    public InventoryCamo(AbstractTileTeleporter teleporter) {
        this.teleporter = teleporter;
    }

    private void updateBlockState() {
        blockState = null;
        Item item = stack.getItem();
        if (!(item instanceof ItemBlock)) return;
        Block block = ((ItemBlock) item).getBlock();
        blockState = block.getStateFromMeta(stack.getMetadata());
        teleporter.markAndNotifyBlock();
    }

    public IBlockState getBlockState() {
        return blockState;
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return stack;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (count < 1) return ItemStack.EMPTY;
        return removeStackFromSlot(index);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = this.stack.copy();
        this.stack = ItemStack.EMPTY;
        updateBlockState();
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.stack = stack;
        updateBlockState();
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void markDirty() {}

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof ItemBlock)) return false;
        Block block = ((ItemBlock) stack.getItem()).getBlock();
        return block.isFullCube(block.getDefaultState());
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        stack = ItemStack.EMPTY;
        updateBlockState();
    }

    @Override
    public String getName() {
        return "Camo";
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(this.getName());
    }
}
