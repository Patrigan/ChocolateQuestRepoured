package team.cqr.cqrepoured.util;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;

public class ItemUtil {

	public static boolean hasFullSet(EntityLivingBase entity, Class<? extends Item> itemClass) {
		Iterator<ItemStack> iterable = entity.getArmorInventoryList().iterator();
		Class<? extends Item> helm, chest, legs, feet;
		try {
			helm = iterable.next().getItem().getClass();
			chest = iterable.next().getItem().getClass();
			legs = iterable.next().getItem().getClass();
			feet = iterable.next().getItem().getClass();
		} catch (NoSuchElementException ex) {
			return false;
		}

		return helm == itemClass && chest == itemClass && legs == itemClass && feet == itemClass;
	}

	public static boolean compareRotations(double yaw1, double yaw2, double maxDiff) {
		maxDiff = Math.abs(maxDiff);
		double d = Math.abs(yaw1 - yaw2) % 360;
		double diff = d > 180.0D ? 360.0D - d : d;

		return diff < maxDiff;
	}

	public static boolean isCheaterItem(ItemStack item) {
		if (!item.isItemEnchanted()) {
			return false;
		}
		Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(item);
		for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
			if (entry.getValue() > entry.getKey().getMaxLevel() * 2) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Copied from {@link EntityPlayer#attackTargetEntityWithCurrentItem(Entity)}
	 */
	public static void attackTarget(ItemStack stack, EntityPlayer player, Entity targetEntity, boolean fakeCrit,
			float damageBonusFlat, float damageBonusPercentage, boolean sweepingEnabled, float sweepingDamageFlat,
			float sweepingDamagePercentage) {
		// CQR replacement for ForgeHooks.onPlayerAttackTarget to prevent infinity loop
		if (MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(player, targetEntity)))
			return;
		if (targetEntity.canBeAttackedWithItem()) {
			if (!targetEntity.hitByEntity(player)) {
				float f = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
				// CQR damage boni
				f += damageBonusFlat + damageBonusPercentage * f;
				float f1;

				if (targetEntity instanceof EntityLivingBase) {
					f1 = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(),
							((EntityLivingBase) targetEntity).getCreatureAttribute());
				} else {
					f1 = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(),
							EnumCreatureAttribute.UNDEFINED);
				}

				float f2 = player.getCooledAttackStrength(0.5F);
				f = f * (0.2F + f2 * f2 * 0.8F);
				f1 = f1 * f2;
				player.resetCooldown();

				if (f > 0.0F || f1 > 0.0F) {
					boolean flag = f2 > 0.9F;
					boolean flag1 = false;
					int i = 0;
					i = i + EnchantmentHelper.getKnockbackModifier(player);

					if (player.isSprinting() && flag) {
						player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ,
								SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, player.getSoundCategory(), 1.0F, 1.0F);
						++i;
						flag1 = true;
					}

					boolean flag2 = flag && player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder()
							&& !player.isInWater() && !player.isPotionActive(MobEffects.BLINDNESS) && !player.isRiding()
							&& targetEntity instanceof EntityLivingBase;
					flag2 = flag2 && !player.isSprinting();

					CriticalHitEvent hitResult = ForgeHooks.getCriticalHit(player, targetEntity, flag2,
							flag2 ? 1.5F : 1.0F);
					flag2 = hitResult != null;
					if (flag2) {
						f *= hitResult.getDamageModifier();
					}

					f = f + f1;
					boolean flag3 = false;
					double d0 = (double) (player.distanceWalkedModified - player.prevDistanceWalkedModified);

					if (sweepingEnabled && flag && !flag2 && !flag1 && player.onGround
							&& d0 < (double) player.getAIMoveSpeed()) {
						ItemStack itemstack = player.getHeldItem(EnumHand.MAIN_HAND);

						if (itemstack.getItem() instanceof ItemSword) {
							flag3 = true;
						}
					}

					float f4 = 0.0F;
					boolean flag4 = false;
					int j = EnchantmentHelper.getFireAspectModifier(player);

					if (targetEntity instanceof EntityLivingBase) {
						f4 = ((EntityLivingBase) targetEntity).getHealth();

						if (j > 0 && !targetEntity.isBurning()) {
							flag4 = true;
							targetEntity.setFire(1);
						}
					}

					double d1 = targetEntity.motionX;
					double d2 = targetEntity.motionY;
					double d3 = targetEntity.motionZ;
					boolean flag5 = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage(player), f);

					if (flag5) {
						if (i > 0) {
							if (targetEntity instanceof EntityLivingBase) {
								((EntityLivingBase) targetEntity).knockBack(player, (float) i * 0.5F,
										(double) MathHelper.sin(player.rotationYaw * 0.017453292F),
										(double) (-MathHelper.cos(player.rotationYaw * 0.017453292F)));
							} else {
								targetEntity.addVelocity(
										(double) (-MathHelper.sin(player.rotationYaw * 0.017453292F) * (float) i
												* 0.5F),
										0.1D, (double) (MathHelper.cos(player.rotationYaw * 0.017453292F) * (float) i
												* 0.5F));
							}

							player.motionX *= 0.6D;
							player.motionZ *= 0.6D;
							player.setSprinting(false);
						}

						if (flag3) {
							// CQR allow modification of sweeping damage
							float f3 = sweepingDamageFlat + sweepingDamagePercentage * f;
							f3 += EnchantmentHelper.getSweepingDamageRatio(player) * f;

							for (EntityLivingBase entitylivingbase : player.world.getEntitiesWithinAABB(
									EntityLivingBase.class,
									targetEntity.getEntityBoundingBox().grow(1.0D, 0.25D, 1.0D))) {
								if (entitylivingbase != player && entitylivingbase != targetEntity
										&& !player.isOnSameTeam(entitylivingbase)
										&& player.getDistanceSq(entitylivingbase) < 9.0D) {
									entitylivingbase.knockBack(player, 0.4F,
											(double) MathHelper.sin(player.rotationYaw * 0.017453292F),
											(double) (-MathHelper.cos(player.rotationYaw * 0.017453292F)));
									entitylivingbase.attackEntityFrom(DamageSource.causePlayerDamage(player), f3);
								}
							}

							player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ,
									SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1.0F, 1.0F);
							player.spawnSweepParticles();
						}

						if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged) {
							((EntityPlayerMP) targetEntity).connection
									.sendPacket(new SPacketEntityVelocity(targetEntity));
							targetEntity.velocityChanged = false;
							targetEntity.motionX = d1;
							targetEntity.motionY = d2;
							targetEntity.motionZ = d3;
						}

						if (flag2) {
							player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ,
									SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.0F);
							player.onCriticalHit(targetEntity);
						} else if (fakeCrit) {
							player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ,
									SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.2F);
					        Minecraft.getMinecraft().effectRenderer.emitParticleAtEntity(targetEntity, EnumParticleTypes.CRIT_MAGIC);
						}

						if (!flag2 && !fakeCrit && !flag3) {
							if (flag) {
								player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ,
										SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1.0F, 1.0F);
							} else {
								player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ,
										SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1.0F, 1.0F);
							}
						}

						if (f1 > 0.0F) {
							player.onEnchantmentCritical(targetEntity);
						}

						player.setLastAttackedEntity(targetEntity);

						if (targetEntity instanceof EntityLivingBase) {
							EnchantmentHelper.applyThornEnchantments((EntityLivingBase) targetEntity, player);
						}

						EnchantmentHelper.applyArthropodEnchantments(player, targetEntity);
						ItemStack itemstack1 = player.getHeldItemMainhand();
						Entity entity = targetEntity;

						if (targetEntity instanceof MultiPartEntityPart) {
							IEntityMultiPart ientitymultipart = ((MultiPartEntityPart) targetEntity).parent;

							if (ientitymultipart instanceof EntityLivingBase) {
								entity = (EntityLivingBase) ientitymultipart;
							}
						}

						if (!itemstack1.isEmpty() && entity instanceof EntityLivingBase) {
							ItemStack beforeHitCopy = itemstack1.copy();
							itemstack1.hitEntity((EntityLivingBase) entity, player);

							if (itemstack1.isEmpty()) {
								ForgeEventFactory.onPlayerDestroyItem(player, beforeHitCopy, EnumHand.MAIN_HAND);
								player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
							}
						}

						if (targetEntity instanceof EntityLivingBase) {
							float f5 = f4 - ((EntityLivingBase) targetEntity).getHealth();
							player.addStat(StatList.DAMAGE_DEALT, Math.round(f5 * 10.0F));

							if (j > 0) {
								targetEntity.setFire(j * 4);
							}

							if (player.world instanceof WorldServer && f5 > 2.0F) {
								int k = (int) ((double) f5 * 0.5D);
								((WorldServer) player.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR,
										targetEntity.posX, targetEntity.posY + (double) (targetEntity.height * 0.5F),
										targetEntity.posZ, k, 0.1D, 0.0D, 0.1D, 0.2D);
							}
						}

						player.addExhaustion(0.1F);
					} else {
						player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ,
								SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(), 1.0F, 1.0F);

						if (flag4) {
							targetEntity.extinguish();
						}
					}
				}
			}
		}
	}

}
