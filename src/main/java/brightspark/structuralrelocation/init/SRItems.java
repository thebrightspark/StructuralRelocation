package brightspark.structuralrelocation.init;

import brightspark.structuralrelocation.item.ItemAreaFinder;
import brightspark.structuralrelocation.item.ItemTargetFinder;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public class SRItems
{
    public static List<Item> ITEMS = new ArrayList<Item>();

    public static ItemTargetFinder targetFinder;
    public static ItemAreaFinder areaFinder;

    public static void regItem(Item item)
    {
        ITEMS.add(item);
    }

    public static void regItems()
    {
        if(!ITEMS.isEmpty()) return;

        regItem(targetFinder = new ItemTargetFinder());
        regItem(areaFinder = new ItemAreaFinder());
    }
}
