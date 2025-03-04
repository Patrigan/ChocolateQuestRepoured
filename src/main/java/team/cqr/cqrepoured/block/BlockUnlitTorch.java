package team.cqr.cqrepoured.block;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import team.cqr.cqrepoured.init.CQRItemTags;

import java.util.Random;

public class BlockUnlitTorch extends TorchBlock implements IWaterLoggable {

	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public BlockUnlitTorch() {
		super(Properties.copy(Blocks.TORCH).lightLevel(state -> 0), null);
		this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(LIT, WATERLOGGED);
	}

	@SuppressWarnings("deprecation")
	@Override
	public FluidState getFluidState(BlockState pState) {
		return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		IWorld level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = super.getStateForPlacement(context);
		if (state == null) {
			return null;
		}
		return state.setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
		if (pState.getValue(WATERLOGGED)) {
			pLevel.getLiquidTicks().scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
		}

		return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
	}

	@Override
	public void onPlace(BlockState state, World level, BlockPos pos, BlockState oldState, boolean p_220082_5_) {
		if (state.getValue(LIT)) {
			level.setBlock(pos, Blocks.TORCH.defaultBlockState(), 11);
			this.spawnIgniteParticles(level, pos, state);
		}
	}

	@Override
	public void entityInside(BlockState state, World level, BlockPos pos, Entity entity) {
		if (!level.isClientSide && entity.isOnFire()) {
			level.setBlock(pos, Blocks.TORCH.defaultBlockState(), 11);
			level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
			this.spawnIgniteParticles(level, pos, state);
		}
	}

	private void spawnIgniteParticles(World level, BlockPos pos, BlockState state) {
		if (!level.isClientSide) {
			double x = pos.getX() + 0.5D;
			double y = pos.getY() + 0.7D;
			double z = pos.getZ() + 0.5D;
			((ServerWorld) level).sendParticles(ParticleTypes.FLAME, x, y, z, 4, 0.0625D, 0.0625D, 0.0625D, 0.0078125D);
		}
	}

	@Override
	public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {

	}

	@Override
	public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
		ItemStack stack = pPlayer.getItemInHand(pHand);

		if (!stack.isEmpty() && stack.getItem().is(CQRItemTags.TORCH_IGNITERS)) {
			if (!pLevel.isClientSide) {
				pLevel.setBlock(pHit.getBlockPos(), Blocks.TORCH.defaultBlockState(), 11);
				pLevel.playSound(null, pHit.getBlockPos(), SoundEvents.FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
				this.spawnIgniteParticles(pLevel, pHit.getBlockPos(), pState);
			}
			return ActionResultType.SUCCESS;
		}
		return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
	}

}
