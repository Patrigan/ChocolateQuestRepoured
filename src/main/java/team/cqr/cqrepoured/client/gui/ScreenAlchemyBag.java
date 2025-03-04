package team.cqr.cqrepoured.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.cqr.cqrepoured.inventory.ContainerAlchemyBag;

@OnlyIn(Dist.CLIENT)
public class ScreenAlchemyBag extends ContainerScreen<ContainerAlchemyBag> {

	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/hopper.png");

	public ScreenAlchemyBag(ContainerAlchemyBag container, PlayerInventory playerInv, ITextComponent title) {
		super(container, playerInv, title);
		this.imageHeight = 133;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
		this.renderBackground(pMatrixStack);
		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
		this.renderTooltip(pMatrixStack, pMouseX, pMouseY);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void renderBg(MatrixStack pMatrixStack, float pPartialTicks, int pX, int pY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(TEXTURE);
		this.blit(pMatrixStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}

}
