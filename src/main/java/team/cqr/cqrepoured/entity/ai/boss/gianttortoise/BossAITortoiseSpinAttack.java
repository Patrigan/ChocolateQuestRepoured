package team.cqr.cqrepoured.entity.ai.boss.gianttortoise;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.World;
import team.cqr.cqrepoured.entity.ai.AbstractCQREntityAI;
import team.cqr.cqrepoured.entity.boss.gianttortoise.EntityCQRGiantTortoise;
import team.cqr.cqrepoured.entity.projectiles.ProjectileBubble;
import team.cqr.cqrepoured.init.CQREntityTypes;
import team.cqr.cqrepoured.init.CQRSounds;

public class BossAITortoiseSpinAttack extends AbstractCQREntityAI<EntityCQRGiantTortoise> {

	private Vector3d movementVector;

	private static final int COOLDOWN = 2;
	private int cooldown = COOLDOWN / 2;
	private int previousBlocks = 0;
	private static final int MAX_BLOCKED_SPINS = 1;

	private final int AFTER_IDLE_TIME = 5;
	private final int BUBBLE_SHOOT_DURATION = 40;

	static final float MAX_DISTANCE_TO_BEGIN_SPIN = 16;
	static final float MAX_DISTANCE_TO_TARGET = 32;

	private int explosionCooldown = 0;
	private static final int MAX_EXPLOSION_COOLDOWN = 20;
	private int ignoreWallTicks = 10;

	public BossAITortoiseSpinAttack(EntityCQRGiantTortoise entity) {
		super(entity);
		//this.setMutexBits(8);
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	private EntityCQRGiantTortoise getBoss() {
		return this.entity;
	}

	@Override
	public boolean canUse() {
		// System.out.println("cooldown: " + this.cooldown);
		if (this.cooldown > 0) {
			this.cooldown--;
		}
		if (!this.getBoss().isStunned() && this.getBoss().getTarget() != null && this.getBoss().getTarget().isAlive()) {
			// System.out.println("The entity is not stunned and has a living attack target");
			/*
			 * if (this.getBoss().getDistance(this.getBoss().getAttackTarget()) > MAX_DISTANCE_TO_BEGIN_SPIN) {
			 * System.out.println("Target too far away");
			 * return false;
			 * }
			 */

			if (this.cooldown <= 0 && !this.getBoss().isHealing() && this.getBoss().isReadyToSpin()) {
				// System.out.println("Cooldown reached, not healing and ready to spin!");
				this.cooldown = 0;
				this.previousBlocks = 0;
				if (this.getBoss().isInShell() || this.getBoss().getCurrentAnimationId() == EntityCQRGiantTortoise.ANIMATION_ID_IN_SHELL) {
					// System.out.println("Ready to spin!");
					this.getBoss().setCanBeStunned(false);
					this.getBoss().setSpinning(true);
					this.getBoss().setInShell(true);
					this.ignoreWallTicks = 10;
					return true;
				} else if (this.getBoss().getCurrentAnimationId() != EntityCQRGiantTortoise.ANIMATION_ID_ENTER_SHELL) {
					// System.out.println("Not yet in in-shell animation, play enter shell animatio");
					this.getBoss().setNextAnimation(EntityCQRGiantTortoise.ANIMATION_ID_ENTER_SHELL);
				}
				// System.out.println("Internal state is not in shell");
			}
		}
		return false;
	}

	@Override
	public boolean canContinueToUse() {
		return this.getBoss() != null && this.getBoss().getCurrentAnimationId() == EntityCQRGiantTortoise.ANIMATION_ID_SPINNING && !this.getBoss().isStunned() && this.getBoss().getSpinsBlocked() <= MAX_BLOCKED_SPINS && !this.getBoss().isDeadOrDying() && this.getBoss().getTarget() != null
				&& !this.getBoss().getTarget().isDeadOrDying() && !this.getBoss().isHealing() && this.getBoss().shouldCurrentAnimationBePlaying();
	}

	private void calculateVelocity() {
		this.movementVector = this.getBoss().getTarget().position().subtract(this.getBoss().position());
		if (this.movementVector.y >= 2) {
			this.movementVector = this.movementVector.subtract(0, this.movementVector.y, 0);
		}
		this.movementVector = this.movementVector.normalize();
		this.movementVector = this.movementVector.scale(1.125D);
	}

	@Override
	public boolean isInterruptable() {
		return false;
	}

	@Override
	public void start() {
		super.start();
		this.getBoss().setSpinning(true);
		this.getBoss().setCanBeStunned(false);
		this.getBoss().setInShell(true);
		this.getBoss().setReadyToSpin(false);
		this.getBoss().setNextAnimation(EntityCQRGiantTortoise.ANIMATION_ID_SPINNING);
		this.ignoreWallTicks = 5;
	}

	@Override
	public void tick() {
		super.tick();
		// this.getBoss().setSpinning(false);
		if (this.getBoss().getSpinsBlocked() >= MAX_BLOCKED_SPINS) {
			this.getBoss().setSpinning(false);
			this.getBoss().setStunned(true);
		} else if (this.getBoss().getCurrentAnimationTick() > this.BUBBLE_SHOOT_DURATION && EntityCQRGiantTortoise.ANIMATIONS[EntityCQRGiantTortoise.ANIMATION_ID_SPINNING].getAnimationDuration() - this.getBoss().getCurrentAnimationTick() > this.AFTER_IDLE_TIME) {
			// Spinning phase
			this.ignoreWallTicks--;
			if (this.explosionCooldown > 0) {
				this.explosionCooldown--;
			}
			if (this.movementVector == null) {
				this.calculateVelocity();
			}
			float targetDist = this.getBoss().distanceTo(this.getBoss().getTarget());
			if (targetDist >= MAX_DISTANCE_TO_TARGET) {
				// Return to bubbling
				this.getBoss().resetCurrentAnimationTickTime();
				this.getBoss().setSpinning(false);
				this.getBoss().resetSpinsBlocked();
				//Reset the velocity
				this.movementVector = null;
			}
			if ((this.ignoreWallTicks <= 0 && this.getBoss().horizontalCollision) || this.previousBlocks != this.getBoss().getSpinsBlocked()) {
				if (this.getBoss().horizontalCollision && !this.getBoss().getWorld().isClientSide && this.explosionCooldown <= 0) {
					this.explosionCooldown = MAX_EXPLOSION_COOLDOWN;
					this.getBoss().getWorld().explode(this.getBoss(), this.entity.position().x, this.entity.position().y, this.entity.position().z, 2, false, Mode.NONE);
				}

				if (this.movementVector != null && this.ignoreWallTicks <= 0 && this.getBoss().horizontalCollision && this.hitHardBlock(this.movementVector.x, this.movementVector.y, this.movementVector.z)) {
					this.getBoss().setSpinning(false);
					this.getBoss().setStunned(true);
				}

				this.calculateVelocity();
				float damage = 1F;
				if (this.previousBlocks != this.getBoss().getSpinsBlocked()) {
					this.previousBlocks = this.getBoss().getSpinsBlocked();
					damage *= 1.5F;
					damage /= Math.max(1, this.getBoss().getWorld().getDifficulty().getId());
					this.getBoss().hurt(DamageSource.IN_WALL, damage, true);
				}

				/*
				 * damage /= Math.max(1, getBoss().getWorld().getDifficulty().getDifficultyId()); if(getBoss().collidedHorizontally) {
				 * getBoss().attackEntityFrom(DamageSource.IN_WALL, damage, true); }
				 */
			}
			this.getBoss().setSpinning(true);
			this.getBoss().setCanBeStunned(false);
			this.getBoss().setInShell(true);
			if(this.movementVector != null) {
				/*this.getBoss().motionX = this.movementVector.x;
				this.getBoss().motionZ = this.movementVector.z;
				this.getBoss().motionY = this.entity.horizontalCollision ? this.movementVector.y : 0.5 * this.movementVector.y;
				this.getBoss().velocityChanged = true;*/
				this.getBoss().setDeltaMovement(this.movementVector.x, this.entity.horizontalCollision ? this.movementVector.y : 0.5 * this.movementVector.y, this.movementVector.z);
				this.getBoss().hasImpulse = true;
			}
		} else if (this.getBoss().getCurrentAnimationTick() <= this.BUBBLE_SHOOT_DURATION) {
			// Shooting bubbles
			this.getBoss().setSpinning(false);
			if (this.getBoss().getCurrentAnimationTick() % 5 == 0) {
				this.getBoss().playSound(CQRSounds.BUBBLE_BUBBLE, 1, 0.75F + (0.5F * this.getBoss().getRandom().nextFloat()));
			}
			Vector3d v = new Vector3d(this.entity.getRandom().nextDouble() - 0.5D, 0.125D * (this.entity.getRandom().nextDouble() - 0.5D), this.entity.getRandom().nextDouble() - 0.5D);
			v = v.normalize();
			v = v.scale(1.4);
			this.entity.getLookControl().setLookAt(this.entity.getTarget(), 30, 30);
			ProjectileBubble bubble = new ProjectileBubble(this.entity, this.entity.getWorld());
			/*bubble.motionX = v.x;
			bubble.motionY = v.y;
			bubble.motionZ = v.z;
			bubble.velocityChanged = true;*/
			bubble.setDeltaMovement(v);
			bubble.hasImpulse = true;
			this.entity.level.addFreshEntity(bubble);

		} else {
			this.getBoss().setSpinning(false);
			this.getBoss().resetSpinsBlocked();
		}
	}

	private boolean hitHardBlock(double vx, double vy, double vz) {
		Vector3d velocity = new Vector3d(vx, vy, vz);
		AxisAlignedBB aabb = this.getBoss().getBoundingBox();
		if (aabb == null) {
			return false;
		}
		aabb = aabb.inflate(0.5).move(velocity.normalize().scale(this.getBoss().getBbWidth() / 2));
		World world = this.getBoss().getWorld();

		int x1 = MathHelper.floor(aabb.minX);
		int y1 = MathHelper.floor(aabb.minY);
		int z1 = MathHelper.floor(aabb.minZ);
		int x2 = MathHelper.floor(aabb.maxX);
		int y2 = MathHelper.floor(aabb.maxY);
		int z2 = MathHelper.floor(aabb.maxZ);

		for (int k1 = x1; k1 <= x2; ++k1) {
			for (int l1 = y1; l1 <= y2; ++l1) {
				for (int i2 = z1; i2 <= z2; ++i2) {
					BlockPos blockpos = new BlockPos(k1, l1, i2);
					BlockState iblockstate = world.getBlockState(blockpos);
					Block block = iblockstate.getBlock();

					if (EntityCQRGiantTortoise.isHardBlock(block.getRegistryName())) {
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public void stop() {
		super.stop();
		this.getBoss().setSpinning(false);
		this.getBoss().setReadyToSpin(true);
		this.getBoss().setCanBeStunned(true);
		this.getBoss().setNextAnimation(EntityCQRGiantTortoise.ANIMATION_ID_IN_SHELL);
		this.cooldown = COOLDOWN;
		if (((this.getBoss().getTarget() == null) || this.getBoss().getTarget().isDeadOrDying())) {
			this.cooldown /= 3;
		}
		// this.getBoss().setAnimationTick(0);
		if (this.getBoss().getSpinsBlocked() >= MAX_BLOCKED_SPINS) {
			this.cooldown *= 1.5;
			this.getBoss().setStunned(true);
		}
		this.getBoss().resetSpinsBlocked();
	}

}
