package team.cqr.cqrepoured.world.structure.generation.structurefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import team.cqr.cqrepoured.CQRMain;
import team.cqr.cqrepoured.block.BlockExporterChest;
import team.cqr.cqrepoured.config.CQRConfig;
import team.cqr.cqrepoured.init.CQRBlocks;
import team.cqr.cqrepoured.util.DungeonGenUtils;
import team.cqr.cqrepoured.util.NBTCollectors;
import team.cqr.cqrepoured.world.structure.generation.generation.DungeonPlacement;
import team.cqr.cqrepoured.world.structure.generation.generation.GeneratableDungeon;
import team.cqr.cqrepoured.world.structure.generation.generation.ICQRLevel;
import team.cqr.cqrepoured.world.structure.generation.generation.preparable.PreparableEntityInfo;
import team.cqr.cqrepoured.world.structure.generation.generation.preparable.PreparablePosInfo;
import team.cqr.cqrepoured.world.structure.generation.inhabitants.DungeonInhabitant;

public class CQStructure {

	private static final Map<File, CQStructure> CACHED_STRUCTURES = new HashMap<>();
	public static final String CQR_FILE_VERSION = "2.0.0";
	private static final Set<ResourceLocation> SPECIAL_ENTITIES = new HashSet<>();
	private final List<PreparablePosInfo> blockInfoList = new ArrayList<>();
	private final List<PreparableEntityInfo> entityInfoList = new ArrayList<>();
	private final List<BlockPos> unprotectedBlockList = new ArrayList<>();
	private BlockPos size = BlockPos.ZERO;
	private String author = "";

	public CQStructure() {

	}

	public static void cacheFiles() {
		CACHED_STRUCTURES.clear();

		if (!CQRConfig.SERVER_CONFIG.advanced.cacheStructureFiles.get()) {
			return;
		}

		List<File> fileList = new ArrayList<>(FileUtils.listFiles(CQRMain.CQ_STRUCTURE_FILES_FOLDER, new String[] { "nbt" }, true));
		fileList.sort((file1, file2) -> {
			if (file1.length() > file2.length()) {
				return -1;
			}
			if (file1.length() < file2.length()) {
				return 1;
			}
			return 0;
		});

		long fileSizeSum = 0;
		for (int i = 0; i < fileList.size() && i < CQRConfig.SERVER_CONFIG.advanced.cachedStructureFilesMaxAmount.get(); i++) {
			File file = fileList.get(i);
			long fileSize = file.length();
			if (fileSizeSum + fileSize < CQRConfig.SERVER_CONFIG.advanced.cachedStructureFilesMaxSize.get() * 1000) {
				CACHED_STRUCTURES.put(file, createFromFile(file));
				fileSizeSum += fileSize;
			}
		}
	}

	public static void clearCache() {
		CACHED_STRUCTURES.clear();
	}

	public static CQStructure createFromFile(File file) {
		if (CACHED_STRUCTURES.containsKey(file)) {
			return CACHED_STRUCTURES.get(file);
		}
		CQStructure structure = new CQStructure();
		structure.readFromFile(file);
		return structure;
	}

	public static CQStructure createFromWorld(World world, BlockPos startPos, BlockPos endPos, boolean ignoreBasicEntities, Collection<BlockPos> unprotectedBlocks, String iTextComponent) {
		CQStructure structure = new CQStructure();
		structure.author = iTextComponent;
		structure.takeBlocksAndEntitiesFromWorld(world, startPos, endPos, ignoreBasicEntities, unprotectedBlocks);
		return structure;
	}

	public boolean isEmpty() {
		return this.blockInfoList.isEmpty() && this.entityInfoList.isEmpty();
	}

	public boolean writeToFile(File file) {
		try {
			if (file.isDirectory()) {
				throw new FileNotFoundException();
			}
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			try (OutputStream outputStream = new FileOutputStream(file)) {
				CompressedStreamTools.writeCompressed(this.writeToNBT(), outputStream);
			}
			return true;
		} catch (Exception e) {
			CQRMain.logger.error(String.format("Failed to write structure to file %s", file.getName()), e);
		}
		return false;
	}

	private boolean readFromFile(File file) {
		try {
			try (InputStream inputStream = new FileInputStream(file)) {
				this.readFromNBT(CompressedStreamTools.readCompressed(inputStream));
			}
			return true;
		} catch (Exception e) {
			CQRMain.logger.error(String.format("Failed to read structure from file %s", file.getName()), e);
		}
		return false;
	}

	private CompoundNBT writeToNBT() {
		CompoundNBT compound = new CompoundNBT();

		compound.putString("cqr_file_version", CQStructure.CQR_FILE_VERSION);
		compound.putString("author", this.author);
		compound.put("size", NBTUtil.writeBlockPos(this.size));

		BlockStatePalette palette = new BlockStatePalette();
		ListNBT compoundList = new ListNBT();

		// Save normal blocks
		ByteBuf buf = Unpooled.buffer(this.blockInfoList.size() * 2);
		this.blockInfoList.forEach(preparable -> PreparablePosInfo.Registry.write(preparable, buf, palette, compoundList));
		compound.putByteArray("blockInfoList", Arrays.copyOf(buf.array(), buf.writerIndex()));

		// Save entities
		compound.put("entityInfoList", this.entityInfoList.stream().map(PreparableEntityInfo::getEntityData).collect(NBTCollectors.toList()));

		// Save block states
		compound.put("palette", palette.writeToNBT());

		// Save compound tags
		compound.put("compoundTagList", compoundList);

		compound.putIntArray("unprotectedBlockList", this.unprotectedBlockList.stream().flatMapToInt(pos -> IntStream.of(pos.getX(), pos.getY(), pos.getZ())).toArray());

		return compound;
	}

	private void readFromNBT(CompoundNBT compound) {
		String cqrFileVersion = compound.getString("cqr_file_version");
		if (!cqrFileVersion.equals(CQR_FILE_VERSION)) {
			throw new IllegalArgumentException(String.format("Structure nbt is deprecated! Expected %s but got %s.", CQR_FILE_VERSION, cqrFileVersion));
		}

		this.author = compound.getString("author");
		this.size = NBTUtil.readBlockPos(compound.getCompound("size"));

		this.blockInfoList.clear();
		this.entityInfoList.clear();

		BlockStatePalette blockStatePalette = new BlockStatePalette();

		// Load compound tags
		ListNBT compoundTagList = compound.getList("compoundTagList", Constants.NBT.TAG_COMPOUND);

		// Load block states
		int blockStateIndex = 0;
		for (INBT nbt : compound.getList("palette", Constants.NBT.TAG_COMPOUND)) {
			blockStatePalette.addMapping(NBTUtil.readBlockState((CompoundNBT) nbt), blockStateIndex++);
		}

		// Load normal blocks
		ByteBuf buf = Unpooled.wrappedBuffer(compound.getByteArray("blockInfoList"));
		for (int i = 0; i < this.size.getX() * this.size.getY() * this.size.getZ(); i++) {
			this.blockInfoList.add(PreparablePosInfo.Registry.read(buf, blockStatePalette, compoundTagList));
		}

		// Load entities
		for (INBT nbt : compound.getList("entityInfoList", Constants.NBT.TAG_COMPOUND)) {
			this.entityInfoList.add(new PreparableEntityInfo((CompoundNBT) nbt));
		}

		this.unprotectedBlockList.clear();
		int[] intArray = compound.getIntArray("unprotectedBlockList");
		IntStream.range(0, intArray.length / 3).mapToObj(i -> new BlockPos(intArray[i * 3], intArray[i * 3 + 1], intArray[i * 3 + 2])).forEach(this.unprotectedBlockList::add);
	}

	private void takeBlocksAndEntitiesFromWorld(World world, BlockPos startPos, BlockPos endPos, boolean ignoreBasicEntities, Collection<BlockPos> unprotectedBlocks) {
		BlockPos pos1 = DungeonGenUtils.getValidMinPos(startPos, endPos);
		BlockPos pos2 = DungeonGenUtils.getValidMaxPos(startPos, endPos);

		this.size = pos2.subtract(pos1).offset(1, 1, 1);

		this.takeBlocksFromWorld(world, pos1, pos2);
		this.takeEntitiesFromWorld(world, pos1, pos2, ignoreBasicEntities);

		this.unprotectedBlockList.clear();
		for (BlockPos pos : unprotectedBlocks) {
			if (pos.getX() < pos1.getX() && pos.getY() < pos1.getY() && pos.getZ() < pos1.getZ()) {
				continue;
			}
			if (pos.getX() > pos2.getX() && pos.getY() > pos2.getY() && pos.getZ() > pos2.getZ()) {
				continue;
			}
			this.unprotectedBlockList.add(pos.subtract(pos1));
		}
	}

	private void takeBlocksFromWorld(World world, BlockPos minPos, BlockPos maxPos) {
		this.blockInfoList.clear();

		for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
			BlockState state = world.getBlockState(pos);
			Block block = state.getBlock();

			if (block != CQRBlocks.NULL_BLOCK.get()
					&& block != Blocks.STRUCTURE_VOID
					&& block != CQRBlocks.BOSS_BLOCK.get()
					&& !(block instanceof BlockExporterChest)
					&& block != CQRBlocks.SPAWNER.get()
					&& block != CQRBlocks.MAP_PLACEHOLDER.get()
					&& state.getDestroySpeed(world, pos) < 0.0F) {
				CQRMain.logger.warn("Exporting unbreakable block: {} from {}", state, pos);
			}

			this.blockInfoList.add(PreparablePosInfo.Registry.create(world, pos, state));
		}
	}

	private void takeEntitiesFromWorld(World world, BlockPos minPos, BlockPos maxPos, boolean ignoreBasicEntities) {
		this.entityInfoList.clear();
		AxisAlignedBB aabb = new AxisAlignedBB(minPos, maxPos.offset(1, 1, 1));

		for (Entity entity : world.getEntitiesOfClass(Entity.class, aabb, input -> !(input instanceof PlayerEntity))) {
			if (ignoreBasicEntities && !SPECIAL_ENTITIES.contains(EntityList.getKey(entity))) {
				CQRMain.logger.info("Skipping entity: {}", entity);
				continue;
			}

			this.entityInfoList.add(new PreparableEntityInfo(minPos, entity));
		}
	}

	public List<PreparablePosInfo> getBlockInfoList() {
		return Collections.unmodifiableList(this.blockInfoList);
	}

	public List<PreparableEntityInfo> getEntityInfoList() {
		return Collections.unmodifiableList(this.entityInfoList);
	}

	public List<BlockPos> getUnprotectedBlockList() {
		return Collections.unmodifiableList(this.unprotectedBlockList);
	}

	public BlockPos getSize() {
		return this.size;
	}

	public String getAuthor() {
		return this.author;
	}

	public void addAll(GeneratableDungeon.Builder builder, BlockPos pos, Offset offset) {
		this.addAll(builder.getLevel(), builder.getPlacement(offset.apply(pos, this, Mirror.NONE, Rotation.NONE)));
	}

	public void addAll(GeneratableDungeon.Builder builder, BlockPos pos, Offset offset, Mirror mirror, Rotation rotation) {
		this.addAll(builder.getLevel(), builder.getPlacement(offset.apply(pos, this, mirror, rotation), mirror, rotation));
	}

	public void addAll(GeneratableDungeon.Builder builder, BlockPos pos, Offset offset, DungeonInhabitant inhabitant) {
		this.addAll(builder.getLevel(), builder.getPlacement(offset.apply(pos, this, Mirror.NONE, Rotation.NONE), inhabitant));
	}

	public void addAll(GeneratableDungeon.Builder builder, BlockPos pos, Offset offset, Mirror mirror, Rotation rotation, DungeonInhabitant inhabitant) {
		this.addAll(builder.getLevel(), builder.getPlacement(offset.apply(pos, this, mirror, rotation), mirror, rotation, inhabitant));
	}

	public void addAll(ICQRLevel level, DungeonPlacement placement) {
		int i = 0;
		for (BlockPos pos : BlockPos.betweenClosed(BlockPos.ZERO, this.size.offset(-1, -1, -1))) {
			this.blockInfoList.get(i++).prepare(level, pos, placement);
		}
	}

	public static void updateSpecialEntities() {
		CQStructure.SPECIAL_ENTITIES.clear();
		for (String s : CQRConfig.SERVER_CONFIG.advanced.specialEntities.get()) {
			CQStructure.SPECIAL_ENTITIES.add(new ResourceLocation(s));
		}
	}

}
