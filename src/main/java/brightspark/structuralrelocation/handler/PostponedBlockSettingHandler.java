package brightspark.structuralrelocation.handler;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.StructuralRelocation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkEvent;
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
		//Set blocks once their time has been reached
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

	@SubscribeEvent
	public static void onChunkUnload(ChunkEvent.Unload event)
	{
		//When a chunk is being unloaded, set any blocks waiting
		World world = event.getWorld();
		List<PostponedBlock> blocks = blocksToSet.get(world.provider.getDimension());
		if(blocks != null && !blocks.isEmpty())
		{
			ChunkPos chunkPos = event.getChunk().getPos();
			Iterator<PostponedBlock> iterator = blocks.iterator();
			while(iterator.hasNext())
			{
				PostponedBlock block = iterator.next();
				if(block.isInChunk(chunkPos))
				{
					StructuralRelocation.LOGGER.info("Setting block {} at {} early as chunk {} is unloading!", block.state, block.location, chunkPos);
					block.set();
					world.markTileEntityForRemoval(block.te);
					iterator.remove();
				}
			}
		}
	}

	private static class PostponedBlock
	{
		final Long timeToSet;
		final Location location;
		final IBlockState state;
		final TileEntity te;

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

		boolean isInChunk(ChunkPos chunkPos)
		{
			BlockPos pos = location.position;
			return pos.getX() >= chunkPos.getXStart() && pos.getX() <= chunkPos.getXEnd() &&
				pos.getZ() >= chunkPos.getZStart() && pos.getZ() <= chunkPos.getZEnd();
		}

		void set()
		{
			location.setBlockState(state);
			if(te != null) location.setTE(te);
		}
	}
}
