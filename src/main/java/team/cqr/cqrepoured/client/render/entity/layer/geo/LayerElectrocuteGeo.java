package team.cqr.cqrepoured.client.render.entity.layer.geo;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import team.cqr.cqrepoured.client.render.entity.layer.AbstractCQRLayerGeo;
import team.cqr.cqrepoured.client.render.entity.layer.IElectrocuteLayerRenderLogic;

import java.util.function.Function;

public class LayerElectrocuteGeo<T extends LivingEntity & IAnimatable> extends AbstractCQRLayerGeo<T> implements IElectrocuteLayerRenderLogic<T> {

	public LayerElectrocuteGeo(GeoEntityRenderer<T> renderer, Function<T, ResourceLocation> funcGetCurrentTexture, Function<T, ResourceLocation> funcGetCurrentModel) {
		super(renderer, funcGetCurrentTexture, funcGetCurrentModel);
	}

	@Override
	public void performPreLineRenderPreparation(MatrixStack matrix) {
		//Fixes rotations on glib entities
	}

	@Override
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		this.renderLayerLogic(entityLivingBaseIn, matrixStackIn, bufferIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
	}

}
