package team.cqr.cqrepoured.client.render.entity;

import java.util.function.Function;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.ExtendedGeoEntityRenderer;
import team.cqr.cqrepoured.CQRMain;
import team.cqr.cqrepoured.client.render.entity.layer.geo.LayerMagicArmorGeo;
import team.cqr.cqrepoured.client.util.BlockRenderUtil;
import team.cqr.cqrepoured.entity.bases.AbstractEntityCQR;

@OnlyIn(Dist.CLIENT)
public abstract class RenderCQREntityGeo<T extends AbstractEntityCQR & IAnimatable> extends ExtendedGeoEntityRenderer<T> {

	public static final ResourceLocation TEXTURES_ARMOR = new ResourceLocation(CQRMain.MODID, "textures/entity/magic_armor/mages.png");

	public final Function<T, ResourceLocation> TEXTURE_GETTER;
	public final Function<T, ResourceLocation> MODEL_ID_GETTER;

	public RenderCQREntityGeo(EntityRendererManager renderManager, AnimatedGeoModel<T> modelProvider) {
		this(renderManager, modelProvider, 1F, 1F, 0);
	}
	
	@SuppressWarnings("resource")
	protected void bindTexture(ResourceLocation textureLocation) {
		Minecraft.getInstance().textureManager.bind(textureLocation);
	}

	protected RenderCQREntityGeo(EntityRendererManager renderManager, AnimatedGeoModel<T> modelProvider, float widthScale, float heightScale, float shadowSize) {
		super(renderManager, modelProvider);

		this.MODEL_ID_GETTER = modelProvider::getModelLocation;
		this.TEXTURE_GETTER = modelProvider::getTextureLocation;

		this.shadowRadius = shadowSize;
		this.widthScale = widthScale;
		this.heightScale = heightScale;

		// layers
		//this.addLayer(new LayerElectrocuteGeo<T>(this, this.TEXTURE_GETTER, this.MODEL_ID_GETTER));
		this.addLayer(new LayerMagicArmorGeo<T>(this, this.TEXTURE_GETTER, this.MODEL_ID_GETTER));
	}

	@Override
	protected float getWidthScale(T entity) {
		return this.widthScale * entity.getSizeVariation();
	}

	@Override
	protected float getHeightScale(T entity) {
		return this.heightScale * entity.getSizeVariation();
	}
	
	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return this.TEXTURE_GETTER.apply(entity);
	}

	protected T currentEntityBeingRendered;

	@Override
	public void renderLate(T animatable, MatrixStack stackIn, float ticks, IRenderTypeBuffer renderTypeBuffer, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue,
			float partialTicks) {
		super.renderLate(animatable, stackIn, ticks, renderTypeBuffer, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, partialTicks);
		this.currentEntityBeingRendered = animatable;
	}

	@Override
	protected void renderBlock(MatrixStack matrixStack, IRenderTypeBuffer rtb, int packedLightIn, BlockState iBlockState) {
		BlockRenderUtil.renderBlockAtEntity(matrixStack, rtb, packedLightIn, iBlockState, this.currentEntityBeingRendered, this);
	}

}
