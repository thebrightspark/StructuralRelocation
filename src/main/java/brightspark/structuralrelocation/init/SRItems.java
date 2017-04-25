package brightspark.structuralrelocation.init;

import brightspark.structuralrelocation.item.ItemSelector;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public class SRItems
{
    public static List<Item> ITEMS = new ArrayList<Item>();

    public static ItemSelector itemSelector;

    public static void regItem(Item item)
    {
        ITEMS.add(item);
    }

    public static void regItems()
    {
        if(!ITEMS.isEmpty()) return;

        regItem(itemSelector = new ItemSelector());
    }
}
