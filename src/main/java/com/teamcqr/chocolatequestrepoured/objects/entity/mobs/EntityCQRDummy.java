package com.teamcqr.chocolatequestrepoured.objects.entity.mobs;

import com.teamcqr.chocolatequestrepoured.factions.EFaction;
import com.teamcqr.chocolatequestrepoured.objects.entity.EBaseHealths;
import com.teamcqr.chocolatequestrepoured.objects.entity.bases.AbstractEntityCQR;

import net.minecraft.entity.Entity;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityCQRDummy extends AbstractEntityCQR {

	public EntityCQRDummy(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {

	}

	@Override
	public float getBaseHealth() {
		return EBaseHealths.DUMMY.getValue();
	}

	@Override
	public EFaction getFaction() {
		return null;
	}

	@Override
	protected void initEntityAI() {

	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	protected void collideWithEntity(Entity entityIn) {

	}
	
	@Override
	public double getSizeVariation() {
		return 0F;
	}

}
