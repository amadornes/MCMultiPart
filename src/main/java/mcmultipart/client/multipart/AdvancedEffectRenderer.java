package mcmultipart.client.multipart;

import java.lang.ref.WeakReference;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AdvancedEffectRenderer extends EffectRenderer {

    private static AdvancedEffectRenderer instance;

    public static AdvancedEffectRenderer getInstance(EffectRenderer effectRenderer) {

        if (instance == null)
            return instance = new AdvancedEffectRenderer(Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().renderEngine,
                    effectRenderer);
        instance.worldObj = Minecraft.getMinecraft().theWorld;
        instance.parent = new WeakReference<EffectRenderer>(effectRenderer);
        return instance;
    }

    private WeakReference<EffectRenderer> parent;

    private AdvancedEffectRenderer(World worldIn, TextureManager rendererIn, EffectRenderer parent) {

        super(worldIn, rendererIn);
        this.parent = new WeakReference<EffectRenderer>(parent);
    }

    @Override
    public void addBlockDestroyEffects(BlockPos pos, IBlockState state) {

        EffectRenderer p = parent.get();
        if (p != null) p.addBlockDestroyEffects(pos, state);
    }

    public void addBlockDestroyEffects(BlockPos pos, TextureAtlasSprite icon) {

        int i = 4;
        for (int j = 0; j < i; ++j) {
            for (int k = 0; k < i; ++k) {
                for (int l = 0; l < i; ++l) {
                    double d0 = pos.getX() + (j + 0.5D) / i;
                    double d1 = pos.getY() + (k + 0.5D) / i;
                    double d2 = pos.getZ() + (l + 0.5D) / i;
                    this.addEffect(new AdvancedEntityDiggingFX(this.worldObj, d0, d1, d2, d0 - pos.getX() - 0.5D, d1 - pos.getY() - 0.5D,
                            d2 - pos.getZ() - 0.5D, icon).func_174846_a(pos));
                }
            }
        }
    }

    @Override
    public void addBlockHitEffects(BlockPos pos, EnumFacing side) {

        EffectRenderer p = parent.get();
        if (p != null) p.addBlockHitEffects(pos, side);
    }

    public void addBlockHitEffects(BlockPos pos, EnumFacing side, AxisAlignedBB box, TextureAtlasSprite icon) {

        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        float f = 0.1F;
        double d0 = i + this.worldObj.rand.nextDouble() * (box.maxX - box.minX - f * 2.0F) + f + box.minX;
        double d1 = j + this.worldObj.rand.nextDouble() * (box.maxY - box.maxY - f * 2.0F) + f + box.minY;
        double d2 = k + this.worldObj.rand.nextDouble() * (box.maxZ - box.minZ - f * 2.0F) + f + box.minZ;

        if (side == EnumFacing.DOWN) d1 = j + box.minY - f;
        if (side == EnumFacing.UP) d1 = j + box.maxY + f;
        if (side == EnumFacing.NORTH) d2 = k + box.minZ - f;
        if (side == EnumFacing.SOUTH) d2 = k + box.maxZ + f;
        if (side == EnumFacing.WEST) d0 = i + box.minX - f;
        if (side == EnumFacing.EAST) d0 = i + box.maxX + f;

        this.addEffect((new AdvancedEntityDiggingFX(this.worldObj, d0, d1, d2, 0.0D, 0.0D, 0.0D, icon)).func_174846_a(pos)
                .multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
    }

    @Override
    public void addBlockHitEffects(BlockPos pos, MovingObjectPosition target) {

        EffectRenderer p = parent.get();
        if (p != null) p.addBlockHitEffects(pos, target);
    }

    public void addBlockHitEffects(BlockPos pos, MovingObjectPosition target, AxisAlignedBB box, TextureAtlasSprite icon) {

        addBlockHitEffects(pos, target.sideHit, box, icon);
    }

    @Override
    public void addEffect(EntityFX effect) {

        EffectRenderer p = parent.get();
        if (p != null) p.addEffect(effect);
    }

    @Override
    public void clearEffects(World worldIn) {

        EffectRenderer p = parent.get();
        if (p != null) p.clearEffects(worldIn);
    }

    @Override
    public void emitParticleAtEntity(Entity entityIn, EnumParticleTypes particleTypes) {

        EffectRenderer p = parent.get();
        if (p != null) p.emitParticleAtEntity(entityIn, particleTypes);
    }

    @Override
    public String getStatistics() {

        if (parent.get() != null) return parent.get().getStatistics();
        return null;
    }

    @Override
    public void moveToAlphaLayer(EntityFX effect) {

        EffectRenderer p = parent.get();
        if (p != null) p.moveToAlphaLayer(effect);
    }

    @Override
    public void moveToNoAlphaLayer(EntityFX effect) {

        EffectRenderer p = parent.get();
        if (p != null) p.moveToNoAlphaLayer(effect);
    }

    @Override
    public void registerParticle(int id, IParticleFactory particleFactory) {

    }

    @Override
    public void renderLitParticles(Entity entityIn, float p_78872_2_) {

        EffectRenderer p = parent.get();
        if (p != null) p.renderLitParticles(entityIn, p_78872_2_);
    }

    @Override
    public void renderParticles(Entity entityIn, float partialTicks) {

        EffectRenderer p = parent.get();
        if (p != null) p.renderParticles(entityIn, partialTicks);
    }

    @Override
    public EntityFX spawnEffectParticle(int particleId, double p_178927_2_, double p_178927_4_, double p_178927_6_, double p_178927_8_,
            double p_178927_10_, double p_178927_12_, int... p_178927_14_) {

        if (parent.get() != null)
            return parent.get().spawnEffectParticle(particleId, p_178927_2_, p_178927_4_, p_178927_6_, p_178927_8_, p_178927_10_,
                    p_178927_12_, p_178927_14_);
        return null;
    }

    @Override
    public void updateEffects() {

        EffectRenderer p = parent.get();
        if (p != null) p.updateEffects();
    }

    public static class AdvancedEntityDiggingFX extends EntityDiggingFX {

        protected AdvancedEntityDiggingFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
                double ySpeedIn, double zSpeedIn, TextureAtlasSprite icon) {

            super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, Blocks.stone.getDefaultState());
            setParticleIcon(icon);
        }

    }

}