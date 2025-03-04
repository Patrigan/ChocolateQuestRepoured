package team.cqr.cqrepoured.client.event;

import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import team.cqr.cqrepoured.CQRMain;
import team.cqr.cqrepoured.init.CQRItems;
import team.cqr.cqrepoured.item.armor.ItemArmorDyable;

@Mod.EventBusSubscriber(modid = CQRMain.MODID, value = Dist.CLIENT)
public class DyableItemEventHandler {

	@SubscribeEvent
	public static void colorItemArmors(ColorHandlerEvent.Item event) {
		Item[] dyables = CQRItems.ITEMS.getEntries().stream().filter(itemRegistryObject -> itemRegistryObject.get() instanceof ItemArmorDyable).map(RegistryObject::get).toArray(Item[]::new);

		ItemColors itemColors = event.getItemColors();
		itemColors.register(
				(stack, tintIndex) -> tintIndex > 0 ? -1 : ((ItemArmorDyable) stack.getItem()).getColor(stack),
				dyables);
	}

}
