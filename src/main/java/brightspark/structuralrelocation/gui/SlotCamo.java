package brightspark.structuralrelocation.gui;

import brightspark.structuralrelocation.tileentity.InventoryCamo;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotCamo extends Slot {
    public SlotCamo(InventoryCamo inv, int xPos, int yPos) {
        super(inv, 0, xPos, yPos);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return inventory.isItemValidForSlot(slotNumber, stack);
    }
}
