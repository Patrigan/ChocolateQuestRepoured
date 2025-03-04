package team.cqr.cqrepoured.world.structure.generation.dungeons;

import java.io.File;
import java.util.Properties;
import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;
import team.cqr.cqrepoured.CQRMain;
import team.cqr.cqrepoured.util.PropertyFileHelper;
import team.cqr.cqrepoured.world.structure.generation.generators.GeneratorTemplateSurface;

/**
 * Copyright (c) 29.04.2019 Developed by DerToaster98 GitHub: https://github.com/DerToaster98
 */
public class DungeonTemplateSurface extends DungeonBase {

	protected File structureFolderPath = new File(CQRMain.CQ_STRUCTURE_FILES_FOLDER, "test");
	protected boolean rotateDungeon = true;

	public DungeonTemplateSurface(String name, Properties prop) {
		super(name, prop);

		this.structureFolderPath = PropertyFileHelper.getStructureFolderProperty(prop, "structureFolder", "test");
		this.rotateDungeon = PropertyFileHelper.getBooleanProperty(prop, "rotateDungeon", this.rotateDungeon);
	}

	@Override
	public StructurePiece runGenerator(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos pos, Random random) {
		return new GeneratorTemplateSurface().prepare(dynamicRegistries, chunkGenerator, templateManager, pos, random, this);
	}

	public File getStructureFolderPath() {
		return this.structureFolderPath;
	}

	public boolean rotateDungeon() {
		return this.rotateDungeon;
	}

}
