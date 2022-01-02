package team.cqr.cqrepoured.item.armor;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import team.cqr.cqrepoured.capability.armor.kingarmor.CapabilityDynamicCrown;
import team.cqr.cqrepoured.capability.armor.kingarmor.CapabilityDynamicCrownProvider;
import team.cqr.cqrepoured.client.init.CQRArmorModels;

public class ItemCrown extends ArmorItem {

	public static final String NBT_KEY_CROWN = "CQR Crown";

	public ItemCrown(ArmorMaterial materialIn, int renderIndexIn) {
		super(materialIn, renderIndexIn, EquipmentSlotType.HEAD);
	}

	@Override
	@Dist(OnlyIn.CLIENT)
	public ModelBiped getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, ModelBiped _default) {
		return CQRArmorModels.crown;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
		return CapabilityDynamicCrownProvider.createProvider();
	}

	@Nullable
	public Item getAttachedItem(ItemStack stack) {
		if (stack.hasCapability(CapabilityDynamicCrownProvider.DYNAMIC_CROWN, null)) {
			return stack.getCapability(CapabilityDynamicCrownProvider.DYNAMIC_CROWN, null).getAttachedItem();
		}
		return null;
	}

	public void attachItem(ItemStack crown, Item toAttach) {
		if (crown.hasCapability(CapabilityDynamicCrownProvider.DYNAMIC_CROWN, null)) {
			crown.getCapability(CapabilityDynamicCrownProvider.DYNAMIC_CROWN, null).attachItem(toAttach);
		}
	}

	public void attachItem(ItemStack crown, ItemStack toAttach) {
		if (crown.hasCapability(CapabilityDynamicCrownProvider.DYNAMIC_CROWN, null)) {
			crown.getCapability(CapabilityDynamicCrownProvider.DYNAMIC_CROWN, null).attachItem(toAttach);
		}
	}

	public void attachItem(ItemStack crown, ResourceLocation toAttach) {
		if (crown.hasCapability(CapabilityDynamicCrownProvider.DYNAMIC_CROWN, null)) {
			crown.getCapability(CapabilityDynamicCrownProvider.DYNAMIC_CROWN, null).attachItem(toAttach);
		}
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		CapabilityDynamicCrown capability = stack.getCapability(CapabilityDynamicCrownProvider.DYNAMIC_CROWN, null);
		if (capability.getAttachedItem() != null) {
			tooltip.add("Attached helmet: " + new ItemStack(capability.getAttachedItem(), 1).getDisplayName());
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
			tooltip.add(TextFormatting.BLUE + I18n.format("description." + this.getRegistryName().getPath() + ".name"));
		} else {
			tooltip.add(TextFormatting.BLUE + I18n.format("description.click_shift.name"));
		}
	}

	// TODO: Tooltip that shows the attachment
	// TODO: Stats are affected by attachment

	public static boolean hasCrown(ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_KEY_CROWN, Constants.NBT.TAG_COMPOUND);
	}

}
