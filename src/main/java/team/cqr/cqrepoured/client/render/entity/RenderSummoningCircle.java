package team.cqr.cqrepoured.client.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import team.cqr.cqrepoured.objects.entity.misc.EntitySummoningCircle;
import team.cqr.cqrepoured.util.Reference;
import team.cqr.cqrepoured.util.VectorUtil;

public class RenderSummoningCircle extends Render<EntitySummoningCircle> {

	public static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
			new ResourceLocation(Reference.MODID, "textures/entity/summoning_circles/zombie.png"),
			new ResourceLocation(Reference.MODID, "textures/entity/summoning_circles/skeleton.png"),
			new ResourceLocation(Reference.MODID, "textures/entity/summoning_circles/flying_skull.png"),
			new ResourceLocation(Reference.MODID, "textures/entity/summoning_circles/flying_sword.png"),
			new ResourceLocation(Reference.MODID, "textures/entity/summoning_circles/meteor.png") };

	public RenderSummoningCircle(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySummoningCircle entity) {
		if (entity.getTextureID() >= TEXTURES.length) {
			return TEXTURES[0];
		} else {
			return TEXTURES[entity.getTextureID()];
		}
	}

	@Override
	public void doRender(EntitySummoningCircle entity, double x, double y, double z, float entityYaw, float partialTicks) {
		//GlStateManager.translate((float) x, (float) y, (float) z);

		/*GlStateManager.color(new Float(0.3F * (Math.sin(0.125 * entity.ticksExisted) + 1)), 0F, 0F);

		this.bindTexture(this.getEntityTexture(entity));
		this.model.render(entity, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);*/
		EntityPlayer player = Minecraft.getMinecraft().player;
		double xo = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
        double yo = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
        double zo = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
		
		GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
		GlStateManager.disableFog();
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		this.setLightmapDisabled(true);
		
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder builder = tess.getBuffer();
		
		GlStateManager.glLineWidth(4.0F);
		builder.begin(3, DefaultVertexFormats.POSITION_COLOR);
		
		double radius = 1.5D;
		double corners = 5;
		int r =  128 + (int)  Math.round(127 * 0.5 * (Math.sin(0.125 * entity.ticksExisted) + 1));
		int g = 1;
		int b = 1;

		Vec3d vector = new Vec3d(radius, 0, 0);
		Vec3d center = entity.getPositionVector();
		double alpha = 360D / corners;
		int skipCorners = 2;
		alpha *= (double)skipCorners;
		for(int i = 0; i <= corners; i++) {
			
			Vec3d pos1 = center.add(vector);
			builder.pos(pos1.x -xo + 0.5D, pos1.y -yo + 0.5D, pos1.z -zo + 0.5D).color(r, g, b, 255).endVertex();
			
			vector = VectorUtil.rotateVectorAroundY(vector, alpha);
		}
		alpha /= (double)skipCorners;
		vector = VectorUtil.rotateVectorAroundY(vector, alpha);
		for(int i = 0; i < corners; i++) {
			Vec3d pos1 = center.add(vector);
			builder.pos(pos1.x -xo + 0.5D, pos1.y -yo + 0.5D, pos1.z -zo + 0.5D).color(r, g, b, 255).endVertex();
			
			vector = VectorUtil.rotateVectorAroundY(vector, alpha);
		}
		
		tess.draw();
		
		this.setLightmapDisabled(false);
		GlStateManager.glLineWidth(1.0F);
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.enableCull();
		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		GlStateManager.enableFog();
		GlStateManager.popMatrix();
		
	}
	
	protected void setLightmapDisabled(boolean disabled)
    {
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);

        if (disabled)
        {
            GlStateManager.disableTexture2D();
        }
        else
        {
            GlStateManager.enableTexture2D();
        }

        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

	@Override
	public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
		// Should be empty!!
	}

}
