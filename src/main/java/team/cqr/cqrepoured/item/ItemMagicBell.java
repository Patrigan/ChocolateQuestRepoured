package team.cqr.cqrepoured.item;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.cqr.cqrepoured.init.CQRParticleType;
import team.cqr.cqrepoured.init.CQRSounds;
import team.cqr.cqrepoured.util.EntityUtil;
import team.cqr.cqrepoured.world.structure.protection.IProtectedRegionManager;
import team.cqr.cqrepoured.world.structure.protection.ProtectedRegion;
import team.cqr.cqrepoured.world.structure.protection.ProtectedRegionManager;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ItemMagicBell extends ItemLore {

	public ItemMagicBell(Properties properties)
	{
		super(properties);
	}
	@Override
	public UseAction getUseAnimation(ItemStack stack) {
		return UseAction.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 20;
	}

	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		playerIn.startUsingItem(handIn);
		return ActionResult.success(playerIn.getItemInHand(handIn));
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, World worldIn, LivingEntity entityLiving) {
		if (!worldIn.isClientSide) {
			IProtectedRegionManager protectedRegionManager = ProtectedRegionManager.getInstance(worldIn);
			List<ProtectedRegion> protectedRegions = protectedRegionManager.getProtectedRegionsAt(entityLiving.blockPosition());

			protectedRegions.stream().map(ProtectedRegion::getEntityDependencies).flatMap(Collection::stream).map(uuid -> EntityUtil.getEntityByUUID(worldIn, uuid)).filter(Objects::nonNull)
					.forEach(entity -> CQRParticleType.spawnParticles(CQRParticleType.BLOCK_HIGHLIGHT, worldIn, entityLiving.getX(), entityLiving.getY(), entityLiving.getZ(), 0, 0, 0, 1, 0, 0, 0, 200, 0xC00000, entity.getId()));

			protectedRegions.stream().map(ProtectedRegion::getBlockDependencies).flatMap(Collection::stream)
					.forEach(pos -> CQRParticleType.spawnParticles(CQRParticleType.BLOCK_HIGHLIGHT, worldIn, entityLiving.getX(), entityLiving.getY(), entityLiving.getZ(), 0, 0, 0, 1, 0, 0, 0, 200, 0x4050D0, pos.getX(), pos.getY(), pos.getZ()));

			protectedRegions.forEach(pr -> {
				BlockPos start = pr.getStartPos();
				BlockPos size = pr.getEndPos().subtract(pr.getStartPos()).offset(1, 1, 1);
				byte[] protectionStates = pr.getProtectionStates();
				for (int i = 0; i < protectionStates.length; i++) {
					byte protectionState = protectionStates[i];
					if (protectionState == 1 || protectionState == 2) {
						int color = protectionState == 1 ? 0xFFFF00 : 0x00FF00;
						int x = i / size.getZ() / size.getY();
						int y = i / size.getZ() % size.getY();
						int z = i % size.getZ();
						CQRParticleType.spawnParticles(CQRParticleType.BLOCK_HIGHLIGHT, worldIn, entityLiving.getX(), entityLiving.getY(), entityLiving.getZ(), 0, 0, 0, 1, 0, 0, 0, 200, color, start.getX() + x, start.getY() + y, start.getZ() + z);
					}
				}
			});

			worldIn.playSound(null, entityLiving.getX(), entityLiving.getY(), entityLiving.getZ(), CQRSounds.BELL_USE, entityLiving.getSoundSource(), 1.0F, 1.0F);
			if (entityLiving instanceof PlayerEntity) {
				if (protectedRegions.isEmpty()) {
					((PlayerEntity) entityLiving).getCooldowns().addCooldown(stack.getItem(), 60);
				} else {
					((PlayerEntity) entityLiving).getCooldowns().addCooldown(stack.getItem(), 200);
				}
			}
		}

		return stack;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return false;
	}

}
