package team.cqr.cqrepoured.objects.entity.projectiles;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ProjectileCannonBall extends ProjectileBase {

	private boolean isFast = false;

	public ProjectileCannonBall(World worldIn) {
		super(worldIn);
	}

	public ProjectileCannonBall(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	public ProjectileCannonBall(World worldIn, EntityLivingBase shooter, boolean fast) {
		super(worldIn, shooter);
		this.isFast = fast;
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (!this.world.isRemote) {
			if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
				if (result.entityHit == this.thrower || !(result.entityHit instanceof EntityLivingBase)) {
					return;
				}

				if (result.entityHit instanceof EntityLivingBase) {
					EntityLivingBase entity = (EntityLivingBase) result.entityHit;

					entity.attackEntityFrom(DamageSource.causeIndirectDamage(this, this.thrower), 10.0F);
				}
				this.world.createExplosion(this.thrower, posX, posY, posZ, 1.5F, false);

				this.setDead();
			}

			super.onImpact(result);
		}
	}

	@Override
	protected void onUpdateInAir() {
		if (this.world.isRemote) {
			if (this.ticksExisted < 10) {
				this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("isFast", this.isFast);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.isFast = compound.getBoolean("isFast");
	}

	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable(source) || this.isFast) {
			return false;
		} else {
			this.markVelocityChanged();

			if (source.getTrueSource() != null) {
				Vec3d vec3d = source.getTrueSource().getLookVec();

				if (vec3d != null) {
					this.motionX = vec3d.x;
					this.motionY = vec3d.y;
					this.motionZ = vec3d.z;
				}

				if (source.getTrueSource() instanceof EntityLivingBase) {
					this.thrower = (EntityLivingBase) source.getTrueSource();
				}

				return true;
			} else {
				return false;
			}
		}
	}

}
