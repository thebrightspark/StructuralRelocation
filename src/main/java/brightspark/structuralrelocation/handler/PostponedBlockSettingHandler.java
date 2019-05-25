package brightspark.structuralrelocation.handler;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.StructuralRelocation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Mod.EventBusSubscriber(modid = StructuralRelocation.MOD_ID)
public class PostponedBlockSettingHandler
{
	private static HashMap<Integer, List<PostponedBlock>> blocksToSet = new HashMap<>();

	public static void addBlockToSet(Location location, IBlockState state, TileEntity te)
	{
		List<PostponedBlock> blocks = blocksToSet.computeIfAbsent(location.dimensionId, dimId -> new LinkedList<>());
		blocks.add(new PostponedBlock(location.world.getTotalWorldTime() + 10, location, state, te));
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event)
	{
		World world = event.world;
		List<PostponedBlock> blocks = blocksToSet.get(world.provider.getDimension());
		if(blocks != null && !blocks.isEmpty())
		{
			Iterator<PostponedBlock> iterator = blocks.iterator();
			while(iterator.hasNext())
			{
				PostponedBlock block = iterator.next();
				if(block.isTime(world.getTotalWorldTime()))
				{
					block.set();
					iterator.remove();
				}
			}
		}
	}

	private static class PostponedBlock
	{
		private final Long timeToSet;
		private final Location location;
		private final IBlockState state;
		private final TileEntity te;

		PostponedBlock(Long timeToSet, Location location, IBlockState state, TileEntity te)
		{
			this.timeToSet = timeToSet;
			this.location = location;
			this.state = state;
			this.te = te;
		}

		boolean isTime(Long worldTime)
		{
			return worldTime >= timeToSet;
		}

		void set()
		{
			location.setBlockState(state);
			if(te != null) location.setTE(te);
		}
	}
}
