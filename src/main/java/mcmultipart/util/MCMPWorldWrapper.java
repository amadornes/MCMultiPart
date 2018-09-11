package mcmultipart.util;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSetMultimap;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.ref.MCMPCapabilities;
import mcmultipart.api.world.IMultipartWorld;
import mcmultipart.api.world.IWorldView;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.multipart.PartInfo;
import mcmultipart.network.MultipartAction;
import mcmultipart.network.MultipartNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.capabilities.Capability;

import java.util.*;

public class MCMPWorldWrapper extends World implements IMultipartWorld {

    private final PartInfo part;
    private final PartInfo partInfo;
    private final IWorldView view;

    public MCMPWorldWrapper(PartInfo part, PartInfo partInfo, IWorldView view) {
        super(part.getActualWorld().getSaveHandler(), part.getActualWorld().getWorldInfo(), part.getActualWorld().provider,
                part.getActualWorld().profiler, part.getActualWorld().isRemote);
        this.part = part;
        this.partInfo = partInfo;
        this.view = view;
    }

    @Override
    public World getActualWorld() {
        return part.getActualWorld();
    }

    @Override
    public IPartInfo getPartInfo() {
        return partInfo;
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return getActualWorld().getChunkProvider();
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return getActualWorld().isBlockLoaded(new BlockPos(x << 4, 0, z << 4), allowEmpty);
    }

    @Override
    public World init() {
        return this;// NO-OP
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return getActualWorld().getBiome(pos);
    }

    @Override
    public Biome getBiomeForCoordsBody(BlockPos pos) {
        return getActualWorld().getBiomeForCoordsBody(pos);
    }

    @Override
    public BiomeProvider getBiomeProvider() {
        return getActualWorld().getBiomeProvider();
    }

    @Override
    public void initialize(WorldSettings settings) {
        // NO-OP
    }

    @Override
    public MinecraftServer getMinecraftServer() {
        return getActualWorld().getMinecraftServer();
    }

    @Override
    public void setInitialSpawnLocation() {
        // NO-OP
    }

    @Override
    public IBlockState getGroundAboveSeaLevel(BlockPos pos) {
        return getActualWorld().getGroundAboveSeaLevel(pos);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return super.isAirBlock(pos);// NO-OP
    }

    @Override
    public boolean isBlockLoaded(BlockPos pos) {
        return getActualWorld().isBlockLoaded(pos);
    }

    @Override
    public boolean isBlockLoaded(BlockPos pos, boolean allowEmpty) {
        return getActualWorld().isBlockLoaded(pos, allowEmpty);
    }

    @Override
    public boolean isAreaLoaded(BlockPos center, int radius) {
        return getActualWorld().isAreaLoaded(center, radius);
    }

    @Override
    public boolean isAreaLoaded(BlockPos center, int radius, boolean allowEmpty) {
        return getActualWorld().isAreaLoaded(center, radius, allowEmpty);
    }

    @Override
    public boolean isAreaLoaded(BlockPos from, BlockPos to) {
        return getActualWorld().isAreaLoaded(from, to);
    }

    @Override
    public boolean isAreaLoaded(BlockPos from, BlockPos to, boolean allowEmpty) {
        return getActualWorld().isAreaLoaded(from, to, allowEmpty);
    }

    @Override
    public boolean isAreaLoaded(StructureBoundingBox box) {
        return getActualWorld().isAreaLoaded(box);
    }

    @Override
    public boolean isAreaLoaded(StructureBoundingBox box, boolean allowEmpty) {
        return getActualWorld().isAreaLoaded(box, allowEmpty);
    }

    @Override
    public Chunk getChunkFromBlockCoords(BlockPos pos) {
        return getActualWorld().getChunkFromBlockCoords(pos);
    }

    @Override
    public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ) {
        return getActualWorld().getChunkFromChunkCoords(chunkX, chunkZ);
    }

    @Override
    public boolean isChunkGeneratedAt(int p_190526_1_, int p_190526_2_) {
        return getActualWorld().isChunkGeneratedAt(p_190526_1_, p_190526_2_);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state) {
        return setBlockState(pos, state, 3);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state, int flags) {
        if (part.getPartPos().equals(pos)) {
            if (state.getBlock() == Blocks.AIR) {
                part.remove();
                return true;
            } else {
                IMultipart newPart = MultipartRegistry.INSTANCE.getPart(state.getBlock());
                if (part.getPart() == newPart) {
                    IBlockState prevState = part.getState();
                    part.setState(state);
                    notifyBlockUpdate(pos, prevState, state, flags);
                    return true;
                } else {
                    // TODO: Check if part replacement is possible
                    return false;
                }
            }
        }
        return getActualWorld().setBlockState(pos, state, flags);
    }

    @Override
    public void markAndNotifyBlock(BlockPos pos, Chunk chunk, IBlockState iblockstate, IBlockState newState, int flags) {
        if (part.getPartPos().equals(pos)) {
            if ((flags & 2) != 0 && (!this.isRemote || (flags & 4) == 0) && (chunk == null || chunk.isPopulated())) {
                notifyBlockUpdate(pos, iblockstate, newState, flags);
            }
            if ((flags & 0b00001) != 0) {
                notifyNeighborsRespectDebug(pos, newState.getBlock(), true);
                part.getContainer().getParts().values().forEach(i -> {
                    if (i != part) {
                        i.getPart().onPartChanged(i, part);
                    }
                });

                if (newState.hasComparatorInputOverride()) {
                    this.updateComparatorOutputLevel(pos, newState.getBlock());
                }
            } else if ((flags & 0b10000) == 0) {
                updateObservingBlocksAt(pos, newState.getBlock());
            }
        } else {
            getActualWorld().markAndNotifyBlock(pos, chunk, iblockstate, newState, flags);
        }
    }

    @Override
    public boolean setBlockToAir(BlockPos pos) {
        return setBlockState(pos, Blocks.AIR.getDefaultState());
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        if (part.getPartPos().equals(pos)) {
            this.playEvent(2001, pos, Block.getStateId(part.getState()));
            if (dropBlock) {
                part.getPart().dropPartAsItem(part, 0);
            }
            return setBlockToAir(pos);
        }
        return getActualWorld().destroyBlock(pos, dropBlock);
    }

    @Override
    public void notifyBlockUpdate(BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
        if (part.getPartPos().equals(pos)) {
            if ((flags & 0b00010) != 0) {
                MultipartNetworkHandler.queuePartChange(part.getActualWorld(), new MultipartAction.Change(part));
            }
            if ((flags & 0b00100) == 0) {
                markBlockRangeForRenderUpdate(pos, pos);
            }
            return;
        }
        getActualWorld().notifyBlockUpdate(pos, oldState, newState, flags);
    }

    @Override
    public void notifyNeighborsRespectDebug(BlockPos pos, Block blockType, boolean notifyObservers) {
        if (this.worldInfo.getTerrainType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
            part.getContainer().getParts().values().forEach(i -> {
                if (i != part) {
                    i.getPart().onPartChanged(i, part);
                }
            });
        }
        getActualWorld().notifyNeighborsRespectDebug(pos, blockType, notifyObservers);
    }

    @Override
    public void markBlocksDirtyVertical(int x1, int z1, int x2, int z2) {
        getActualWorld().markBlocksDirtyVertical(x1, z1, x2, z2);
    }

    @Override
    public void markBlockRangeForRenderUpdate(BlockPos rangeMin, BlockPos rangeMax) {
        getActualWorld().markBlockRangeForRenderUpdate(rangeMin, rangeMax);
    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        getActualWorld().markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
    }

    @Override
    public void updateObservingBlocksAt(BlockPos p_190522_1_, Block p_190522_2_) {
        getActualWorld().updateObservingBlocksAt(p_190522_1_, p_190522_2_);
    }

    @Override
    public void notifyNeighborsOfStateChange(BlockPos pos, Block blockType, boolean p_175685_3_) {
        part.getContainer().getParts().values().forEach(i -> {
            if (i != part) {
                i.getPart().onPartChanged(i, part);
            }
        });
        getActualWorld().notifyNeighborsOfStateChange(pos, blockType, p_175685_3_);
    }

    @Override
    public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, EnumFacing skipSide) {
        part.getContainer().getParts().values().forEach(i -> {
            if (i != part) {
                i.getPart().onPartChanged(i, part);
            }
        });
        getActualWorld().notifyNeighborsOfStateExcept(pos, blockType, skipSide);
    }

    @Override
    public void neighborChanged(BlockPos p_190524_1_, Block p_190524_2_, BlockPos p_190524_3_) {
        getActualWorld().neighborChanged(p_190524_1_, p_190524_2_, p_190524_3_);
    }

    @Override
    public void observedNeighborChanged(BlockPos p_190529_1_, Block p_190529_2_, BlockPos p_190529_3_) {
        getActualWorld().observedNeighborChanged(p_190529_1_, p_190529_2_, p_190529_3_);
    }

    @Override
    public boolean isBlockTickPending(BlockPos pos, Block blockType) {
        if (part.getPartPos().equals(pos)) {
            return part.hasPendingTicks();
        }
        return getActualWorld().isBlockTickPending(pos, blockType);
    }

    @Override
    public boolean canSeeSky(BlockPos pos) {
        return getActualWorld().canSeeSky(pos);
    }

    @Override
    public boolean canBlockSeeSky(BlockPos pos) {
        return getActualWorld().canBlockSeeSky(pos);
    }

    @Override
    public int getLight(BlockPos pos) {
        return getActualWorld().getLight(pos);
    }

    @Override
    public int getLightFromNeighbors(BlockPos pos) {
        return getActualWorld().getLightFromNeighbors(pos);
    }

    @Override
    public int getLight(BlockPos pos, boolean checkNeighbors) {
        return getActualWorld().getLight(pos, checkNeighbors);
    }

    @Override
    public BlockPos getHeight(BlockPos pos) {
        return getActualWorld().getHeight(pos);
    }

    @Override
    public int getHeight(int x, int z) {
        return getActualWorld().getHeight(x, z);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getChunksLowestHorizon(int x, int z) {
        return getActualWorld().getChunksLowestHorizon(x, z);
    }

    @Override
    public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos) {
        return getActualWorld().getLightFromNeighborsFor(type, pos);
    }

    @Override
    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        return getActualWorld().getLightFor(type, pos);
    }

    @Override
    public void setLightFor(EnumSkyBlock type, BlockPos pos, int lightValue) {
        getActualWorld().setLightFor(type, pos, lightValue);
    }

    @Override
    public void notifyLightSet(BlockPos pos) {
        getActualWorld().notifyLightSet(pos);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return getActualWorld().getCombinedLight(pos, lightValue);
    }

    @Override
    public float getLightBrightness(BlockPos pos) {
        return getActualWorld().getLightBrightness(pos);
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return view.getActualState(getActualWorld(), pos);
    }

    @Override
    public boolean isDaytime() {
        return getActualWorld().isDaytime();
    }

    @Override
    public RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end) {
        return getActualWorld().rayTraceBlocks(start, end);
    }

    @Override
    public RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end, boolean stopOnLiquid) {
        return getActualWorld().rayTraceBlocks(start, end, stopOnLiquid);
    }

    @Override
    public RayTraceResult rayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox,
                                         boolean returnLastUncollidableBlock) {
        return getActualWorld().rayTraceBlocks(vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
    }

    @Override
    public void playSound(EntityPlayer player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        getActualWorld().playSound(player, pos, soundIn, category, volume, pitch);
    }

    @Override
    public void playSound(EntityPlayer player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume,
                          float pitch) {
        getActualWorld().playSound(player, x, y, z, soundIn, category, volume, pitch);
    }

    @Override
    public void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch,
                          boolean distanceDelay) {
        getActualWorld().playSound(x, y, z, soundIn, category, volume, pitch, distanceDelay);
    }

    @Override
    public void playRecord(BlockPos blockPositionIn, SoundEvent soundEventIn) {
        getActualWorld().playRecord(blockPositionIn, soundEventIn);
    }

    @Override
    public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed,
                              double zSpeed, int... parameters) {
        getActualWorld().spawnParticle(particleType, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
    }

    @Override
    public void spawnAlwaysVisibleParticle(int p_190523_1_, double p_190523_2_, double p_190523_4_, double p_190523_6_, double p_190523_8_,
                                           double p_190523_10_, double p_190523_12_, int... p_190523_14_) {
        getActualWorld().spawnAlwaysVisibleParticle(p_190523_1_, p_190523_2_, p_190523_4_, p_190523_6_, p_190523_8_, p_190523_10_,
                p_190523_12_, p_190523_14_);
    }

    @Override
    public void spawnParticle(EnumParticleTypes particleType, boolean ignoreRange, double xCoord, double yCoord, double zCoord,
                              double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        getActualWorld().spawnParticle(particleType, ignoreRange, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
    }

    @Override
    public boolean addWeatherEffect(Entity entityIn) {
        return getActualWorld().addWeatherEffect(entityIn);
    }

    @Override
    public boolean spawnEntity(Entity entityIn) {
        return getActualWorld().spawnEntity(entityIn);
    }

    @Override
    public void onEntityAdded(Entity entityIn) {
        getActualWorld().onEntityAdded(entityIn);
    }

    @Override
    public void onEntityRemoved(Entity entityIn) {
        getActualWorld().onEntityRemoved(entityIn);
    }

    @Override
    public void removeEntity(Entity entityIn) {
        getActualWorld().removeEntity(entityIn);
    }

    @Override
    public void removeEntityDangerously(Entity entityIn) {
        getActualWorld().removeEntityDangerously(entityIn);
    }

    @Override
    public void addEventListener(IWorldEventListener listener) {
        getActualWorld().addEventListener(listener);
    }

    @Override
    public List<AxisAlignedBB> getCollisionBoxes(Entity entityIn, AxisAlignedBB aabb) {
        return getActualWorld().getCollisionBoxes(entityIn, aabb);
    }

    @Override
    public void removeEventListener(IWorldEventListener listener) {
        getActualWorld().removeEventListener(listener);
    }

    @Override
    public boolean collidesWithAnyBlock(AxisAlignedBB bbox) {
        return getActualWorld().collidesWithAnyBlock(bbox);
    }

    @Override
    public int calculateSkylightSubtracted(float partialTicks) {
        return getActualWorld().calculateSkylightSubtracted(partialTicks);
    }

    @Override
    public float getSunBrightnessFactor(float partialTicks) {
        return getActualWorld().getSunBrightnessFactor(partialTicks);
    }

    @Override
    public float getSunBrightness(float p_72971_1_) {
        return getActualWorld().getSunBrightness(p_72971_1_);
    }

    @Override
    public float getSunBrightnessBody(float p_72971_1_) {
        return getActualWorld().getSunBrightnessBody(p_72971_1_);
    }

    @Override
    public Vec3d getSkyColor(Entity entityIn, float partialTicks) {
        return getActualWorld().getSkyColor(entityIn, partialTicks);
    }

    @Override
    public Vec3d getSkyColorBody(Entity entityIn, float partialTicks) {
        return getActualWorld().getSkyColorBody(entityIn, partialTicks);
    }

    @Override
    public float getCelestialAngle(float partialTicks) {
        return getActualWorld().getCelestialAngle(partialTicks);
    }

    @Override
    public int getMoonPhase() {
        return getActualWorld().getMoonPhase();
    }

    @Override
    public float getCurrentMoonPhaseFactor() {
        return getActualWorld().getCurrentMoonPhaseFactor();
    }

    @Override
    public float getCurrentMoonPhaseFactorBody() {
        return getActualWorld().getCurrentMoonPhaseFactorBody();
    }

    @Override
    public float getCelestialAngleRadians(float partialTicks) {
        return getActualWorld().getCelestialAngleRadians(partialTicks);
    }

    @Override
    public Vec3d getCloudColour(float partialTicks) {
        return getActualWorld().getCloudColour(partialTicks);
    }

    @Override
    public Vec3d getCloudColorBody(float partialTicks) {
        return getActualWorld().getCloudColorBody(partialTicks);
    }

    @Override
    public Vec3d getFogColor(float partialTicks) {
        return getActualWorld().getFogColor(partialTicks);
    }

    @Override
    public BlockPos getPrecipitationHeight(BlockPos pos) {
        return getActualWorld().getPrecipitationHeight(pos);
    }

    @Override
    public BlockPos getTopSolidOrLiquidBlock(BlockPos pos) {
        return getActualWorld().getTopSolidOrLiquidBlock(pos);
    }

    @Override
    public float getStarBrightness(float partialTicks) {
        return getActualWorld().getStarBrightness(partialTicks);
    }

    @Override
    public float getStarBrightnessBody(float partialTicks) {
        return getActualWorld().getStarBrightnessBody(partialTicks);
    }

    @Override
    public boolean isUpdateScheduled(BlockPos pos, Block blk) {
        return getActualWorld().isUpdateScheduled(pos, blk);
    }

    @Override
    public void scheduleUpdate(BlockPos pos, Block block, int delay) {
        if (part.getPartPos().equals(pos)) {
            part.scheduleTick(delay);
        } else {
            getActualWorld().scheduleUpdate(pos, block, delay);
        }
    }

    @Override
    public void updateBlockTick(BlockPos pos, Block block, int delay, int priority) {
        if (part.getPartPos().equals(pos)) {
            part.scheduleTick(delay);
        } else {
            getActualWorld().updateBlockTick(pos, block, delay, priority);
        }
    }

    @Override
    public void scheduleBlockUpdate(BlockPos pos, Block block, int delay, int priority) {
        if (part.getPartPos().equals(pos)) {
            part.scheduleTick(delay);
        } else {
            getActualWorld().scheduleBlockUpdate(pos, block, delay, priority);
        }
    }

    @Override
    public void updateEntities() {
        getActualWorld().updateEntities();
    }

    @Override
    public boolean addTileEntity(TileEntity tile) {
        return getActualWorld().addTileEntity(tile);
    }

    @Override
    public void addTileEntities(Collection<TileEntity> tileEntityCollection) {
        getActualWorld().addTileEntities(tileEntityCollection);
    }

    @Override
    public void updateEntity(Entity ent) {
        getActualWorld().updateEntity(ent);
    }

    @Override
    public void updateEntityWithOptionalForce(Entity entityIn, boolean forceUpdate) {
        getActualWorld().updateEntityWithOptionalForce(entityIn, forceUpdate);
    }

    @Override
    public boolean checkNoEntityCollision(AxisAlignedBB bb) {
        return getActualWorld().checkNoEntityCollision(bb);
    }

    @Override
    public boolean checkNoEntityCollision(AxisAlignedBB bb, Entity entityIn) {
        return getActualWorld().checkNoEntityCollision(bb, entityIn);
    }

    @Override
    public boolean checkBlockCollision(AxisAlignedBB bb) {
        return getActualWorld().checkBlockCollision(bb);
    }

    @Override
    public boolean containsAnyLiquid(AxisAlignedBB bb) {
        return getActualWorld().containsAnyLiquid(bb);
    }

    @Override
    public boolean isFlammableWithin(AxisAlignedBB bb) {
        return getActualWorld().isFlammableWithin(bb);
    }

    @Override
    public boolean handleMaterialAcceleration(AxisAlignedBB bb, Material materialIn, Entity entityIn) {
        return getActualWorld().handleMaterialAcceleration(bb, materialIn, entityIn);
    }

    @Override
    public boolean isMaterialInBB(AxisAlignedBB bb, Material materialIn) {
        return getActualWorld().isMaterialInBB(bb, materialIn);
    }

    @Override
    public Explosion createExplosion(Entity entityIn, double x, double y, double z, float strength, boolean isSmoking) {
        return getActualWorld().createExplosion(entityIn, x, y, z, strength, isSmoking);
    }

    @Override
    public Explosion newExplosion(Entity entityIn, double x, double y, double z, float strength, boolean isFlaming, boolean isSmoking) {
        return getActualWorld().newExplosion(entityIn, x, y, z, strength, isFlaming, isSmoking);
    }

    @Override
    public float getBlockDensity(Vec3d vec, AxisAlignedBB bb) {
        return getActualWorld().getBlockDensity(vec, bb);
    }

    @Override
    public boolean extinguishFire(EntityPlayer player, BlockPos pos, EnumFacing side) {
        return getActualWorld().extinguishFire(player, pos, side);
    }

    @Override
    public String getDebugLoadedEntities() {
        return getActualWorld().getDebugLoadedEntities();
    }

    @Override
    public String getProviderName() {
        return getActualWorld().getProviderName();
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return view.getActualTile(getActualWorld(), pos);
    }

    @Override
    public void setTileEntity(BlockPos pos, TileEntity tile) {
        if (part.getPartPos().equals(pos)) {
            if (tile.hasCapability(MCMPCapabilities.MULTIPART_TILE, null)) {
                part.setTile(tile.getCapability(MCMPCapabilities.MULTIPART_TILE, null));
            } else {
                throw new IllegalArgumentException("The specified TileEntity is not a multipart!");
            }
        } else {
            getActualWorld().setTileEntity(pos, tile);
        }
    }

    @Override
    public void removeTileEntity(BlockPos pos) {
        if (part.getPartPos().equals(pos)) {
            TileEntity tileentity = this.getTileEntity(pos);
            if (tileentity != null) {
                tileentity.invalidate();
            }
            this.updateComparatorOutputLevel(pos, getBlockState(pos).getBlock());
        } else {
            getActualWorld().removeTileEntity(pos);
        }
    }

    @Override
    public void markTileEntityForRemoval(TileEntity tile) {
        if (tile != null) {
            BlockPos pos = tile.getPos();
            if (part.getPartPos().equals(pos)) {
                tile.invalidate();
                this.updateComparatorOutputLevel(pos, getBlockState(pos).getBlock());
                return;
            }
        }
        getActualWorld().markTileEntityForRemoval(tile);
    }

    @Override
    public boolean isBlockFullCube(BlockPos pos) {
        return getActualWorld().isBlockFullCube(pos);
    }

    @Override
    public boolean isBlockNormalCube(BlockPos pos, boolean _default) {
        return getActualWorld().isBlockNormalCube(pos, _default);
    }

    @Override
    public void calculateInitialSkylight() {
        getActualWorld().calculateInitialSkylight();
    }

    @Override
    public void setAllowedSpawnTypes(boolean hostile, boolean peaceful) {
        getActualWorld().setAllowedSpawnTypes(hostile, peaceful);
    }

    @Override
    public void tick() {
        getActualWorld().tick();
    }

    @Override
    public void calculateInitialWeatherBody() {
        getActualWorld().calculateInitialWeatherBody();
    }

    @Override
    public void updateWeatherBody() {
        getActualWorld().updateWeatherBody();
    }

    @Override
    public void immediateBlockTick(BlockPos pos, IBlockState state, Random random) {
        getActualWorld().immediateBlockTick(pos, state, random);
    }

    @Override
    public boolean canBlockFreezeWater(BlockPos pos) {
        return getActualWorld().canBlockFreezeWater(pos);
    }

    @Override
    public boolean canBlockFreezeNoWater(BlockPos pos) {
        return getActualWorld().canBlockFreezeNoWater(pos);
    }

    @Override
    public boolean canBlockFreeze(BlockPos pos, boolean noWaterAdj) {
        return getActualWorld().canBlockFreeze(pos, noWaterAdj);
    }

    @Override
    public boolean canBlockFreezeBody(BlockPos pos, boolean noWaterAdj) {
        return getActualWorld().canBlockFreezeBody(pos, noWaterAdj);
    }

    @Override
    public boolean canSnowAt(BlockPos pos, boolean checkLight) {
        return getActualWorld().canSnowAt(pos, checkLight);
    }

    @Override
    public boolean canSnowAtBody(BlockPos pos, boolean checkLight) {
        return getActualWorld().canSnowAtBody(pos, checkLight);
    }

    @Override
    public boolean checkLight(BlockPos pos) {
        return getActualWorld().checkLight(pos);
    }

    @Override
    public boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos) {
        return getActualWorld().checkLightFor(lightType, pos);
    }

    @Override
    public boolean tickUpdates(boolean p_72955_1_) {
        return getActualWorld().tickUpdates(p_72955_1_);
    }

    @Override
    public List<NextTickListEntry> getPendingBlockUpdates(Chunk chunkIn, boolean p_72920_2_) {
        return getActualWorld().getPendingBlockUpdates(chunkIn, p_72920_2_);// TODO: Handle later on?
    }

    @Override
    public List<NextTickListEntry> getPendingBlockUpdates(StructureBoundingBox structureBB, boolean p_175712_2_) {
        return getActualWorld().getPendingBlockUpdates(structureBB, p_175712_2_);// TODO: Handle later on?
    }

    @Override
    public List<Entity> getEntitiesWithinAABBExcludingEntity(Entity entityIn, AxisAlignedBB bb) {
        return getActualWorld().getEntitiesWithinAABBExcludingEntity(entityIn, bb);
    }

    @Override
    public List<Entity> getEntitiesInAABBexcluding(Entity entityIn, AxisAlignedBB boundingBox, Predicate<? super Entity> predicate) {
        return getActualWorld().getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntities(Class<? extends T> entityType, Predicate<? super T> filter) {
        return getActualWorld().getEntities(entityType, filter);
    }

    @Override
    public <T extends Entity> List<T> getPlayers(Class<? extends T> playerType, Predicate<? super T> filter) {
        return getActualWorld().getPlayers(playerType, filter);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> classEntity, AxisAlignedBB bb) {
        return getActualWorld().getEntitiesWithinAABB(classEntity, bb);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb, Predicate<? super T> filter) {
        return getActualWorld().getEntitiesWithinAABB(clazz, aabb, filter);
    }

    @Override
    public <T extends Entity> T findNearestEntityWithinAABB(Class<? extends T> entityType, AxisAlignedBB aabb, T closestTo) {
        return getActualWorld().findNearestEntityWithinAABB(entityType, aabb, closestTo);
    }

    @Override
    public Entity getEntityByID(int id) {
        return getActualWorld().getEntityByID(id);
    }

    @Override
    public List<Entity> getLoadedEntityList() {
        return getActualWorld().getLoadedEntityList();
    }

    @Override
    public void markChunkDirty(BlockPos pos, TileEntity unusedTileEntity) {
        getActualWorld().markChunkDirty(pos, unusedTileEntity);
    }

    @Override
    public int countEntities(Class<?> entityType) {
        return getActualWorld().countEntities(entityType);
    }

    @Override
    public void loadEntities(Collection<Entity> entityCollection) {
        getActualWorld().loadEntities(entityCollection);
    }

    @Override
    public void unloadEntities(Collection<Entity> entityCollection) {
        getActualWorld().unloadEntities(entityCollection);
    }

    @Override
    public boolean mayPlace(Block p_190527_1_, BlockPos p_190527_2_, boolean p_190527_3_, EnumFacing p_190527_4_, Entity p_190527_5_) {
        return getActualWorld().mayPlace(p_190527_1_, p_190527_2_, p_190527_3_, p_190527_4_, p_190527_5_);
    }

    @Override
    public int getSeaLevel() {
        return getActualWorld().getSeaLevel();
    }

    @Override
    public void setSeaLevel(int seaLevelIn) {
        getActualWorld().setSeaLevel(seaLevelIn);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return getActualWorld().getStrongPower(pos, direction);
    }

    @Override
    public WorldType getWorldType() {
        return getActualWorld().getWorldType();
    }

    @Override
    public int getStrongPower(BlockPos pos) {
        return getActualWorld().getStrongPower(pos);
    }

    @Override
    public boolean isSidePowered(BlockPos pos, EnumFacing side) {
        return getActualWorld().isSidePowered(pos, side);
    }

    @Override
    public int getRedstonePower(BlockPos pos, EnumFacing facing) {
        return getActualWorld().getRedstonePower(pos, facing);
    }

    @Override
    public boolean isBlockPowered(BlockPos pos) {
        return getActualWorld().isBlockPowered(pos);
    }

    @Override
    public int isBlockIndirectlyGettingPowered(BlockPos pos) {
        return getActualWorld().isBlockIndirectlyGettingPowered(pos);
    }

    @Override
    public EntityPlayer getClosestPlayerToEntity(Entity entityIn, double distance) {
        return getActualWorld().getClosestPlayerToEntity(entityIn, distance);
    }

    @Override
    public EntityPlayer getNearestPlayerNotCreative(Entity entityIn, double distance) {
        return getActualWorld().getNearestPlayerNotCreative(entityIn, distance);
    }

    @Override
    public EntityPlayer getClosestPlayer(double posX, double posY, double posZ, double distance, boolean spectator) {
        return getActualWorld().getClosestPlayer(posX, posY, posZ, distance, spectator);
    }

    @Override
    public EntityPlayer getClosestPlayer(double p_190525_1_, double p_190525_3_, double p_190525_5_, double p_190525_7_,
                                         Predicate<Entity> p_190525_9_) {
        return getActualWorld().getClosestPlayer(p_190525_1_, p_190525_3_, p_190525_5_, p_190525_7_, p_190525_9_);
    }

    @Override
    public boolean isAnyPlayerWithinRangeAt(double x, double y, double z, double range) {
        return getActualWorld().isAnyPlayerWithinRangeAt(x, y, z, range);
    }

    @Override
    public EntityPlayer getNearestAttackablePlayer(Entity entityIn, double maxXZDistance, double maxYDistance) {
        return getActualWorld().getNearestAttackablePlayer(entityIn, maxXZDistance, maxYDistance);
    }

    @Override
    public EntityPlayer getNearestAttackablePlayer(BlockPos pos, double maxXZDistance, double maxYDistance) {
        return getActualWorld().getNearestAttackablePlayer(pos, maxXZDistance, maxYDistance);
    }

    @Override
    public EntityPlayer getNearestAttackablePlayer(double posX, double posY, double posZ, double maxXZDistance, double maxYDistance,
                                                   Function<EntityPlayer, Double> playerToDouble, Predicate<EntityPlayer> p_184150_12_) {
        return getActualWorld().getNearestAttackablePlayer(posX, posY, posZ, maxXZDistance, maxYDistance, playerToDouble, p_184150_12_);
    }

    @Override
    public EntityPlayer getPlayerEntityByName(String name) {
        return getActualWorld().getPlayerEntityByName(name);
    }

    @Override
    public EntityPlayer getPlayerEntityByUUID(UUID uuid) {
        return getActualWorld().getPlayerEntityByUUID(uuid);
    }

    @Override
    public void sendQuittingDisconnectingPacket() {
        getActualWorld().sendQuittingDisconnectingPacket();
    }

    @Override
    public void checkSessionLock() throws MinecraftException {
        getActualWorld().checkSessionLock();
    }

    @Override
    public void setTotalWorldTime(long worldTime) {
        getActualWorld().setTotalWorldTime(worldTime);
    }

    @Override
    public long getSeed() {
        return getActualWorld().getSeed();
    }

    @Override
    public long getTotalWorldTime() {
        return getActualWorld().getTotalWorldTime();
    }

    @Override
    public long getWorldTime() {
        return getActualWorld().getWorldTime();
    }

    @Override
    public void setWorldTime(long time) {
        getActualWorld().setWorldTime(time);
    }

    @Override
    public BlockPos getSpawnPoint() {
        return getActualWorld().getSpawnPoint();
    }

    @Override
    public void setSpawnPoint(BlockPos pos) {
        getActualWorld().setSpawnPoint(pos);
    }

    @Override
    public void joinEntityInSurroundings(Entity entityIn) {
        getActualWorld().joinEntityInSurroundings(entityIn);
    }

    @Override
    public boolean isBlockModifiable(EntityPlayer player, BlockPos pos) {
        return getActualWorld().isBlockModifiable(player, pos);
    }

    @Override
    public boolean canMineBlockBody(EntityPlayer player, BlockPos pos) {
        return getActualWorld().canMineBlockBody(player, pos);
    }

    @Override
    public void setEntityState(Entity entityIn, byte state) {
        getActualWorld().setEntityState(entityIn, state);
    }

    @Override
    public IChunkProvider getChunkProvider() {
        return getActualWorld().getChunkProvider();
    }

    @Override
    public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam) {
        getActualWorld().addBlockEvent(pos, blockIn, eventID, eventParam);
    }

    @Override
    public ISaveHandler getSaveHandler() {
        return getActualWorld().getSaveHandler();
    }

    @Override
    public WorldInfo getWorldInfo() {
        return getActualWorld().getWorldInfo();
    }

    @Override
    public GameRules getGameRules() {
        return getActualWorld().getGameRules();
    }

    @Override
    public void updateAllPlayersSleepingFlag() {
        getActualWorld().updateAllPlayersSleepingFlag();
    }

    @Override
    public float getThunderStrength(float delta) {
        return getActualWorld().getThunderStrength(delta);
    }

    @Override
    public void setThunderStrength(float strength) {
        getActualWorld().setThunderStrength(strength);
    }

    @Override
    public float getRainStrength(float delta) {
        return getActualWorld().getRainStrength(delta);
    }

    @Override
    public void setRainStrength(float strength) {
        getActualWorld().setRainStrength(strength);
    }

    @Override
    public boolean isThundering() {
        return getActualWorld().isThundering();
    }

    @Override
    public boolean isRaining() {
        return getActualWorld().isRaining();
    }

    @Override
    public boolean isRainingAt(BlockPos strikePosition) {
        return getActualWorld().isRainingAt(strikePosition);
    }

    @Override
    public boolean isBlockinHighHumidity(BlockPos pos) {
        return getActualWorld().isBlockinHighHumidity(pos);
    }

    @Override
    public MapStorage getMapStorage() {
        return getActualWorld().getMapStorage();
    }

    @Override
    public void setData(String dataID, WorldSavedData worldSavedDataIn) {
        getActualWorld().setData(dataID, worldSavedDataIn);
    }

    @Override
    public WorldSavedData loadData(Class<? extends WorldSavedData> clazz, String dataID) {
        return getActualWorld().loadData(clazz, dataID);
    }

    @Override
    public int getUniqueDataId(String key) {
        return getActualWorld().getUniqueDataId(key);
    }

    @Override
    public void playBroadcastSound(int id, BlockPos pos, int data) {
        getActualWorld().playBroadcastSound(id, pos, data);
    }

    @Override
    public void playEvent(int type, BlockPos pos, int data) {
        getActualWorld().playEvent(type, pos, data);
    }

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos pos, int data) {
        getActualWorld().playEvent(player, type, pos, data);
    }

    @Override
    public int getHeight() {
        return getActualWorld().getHeight();
    }

    @Override
    public int getActualHeight() {
        return getActualWorld().getActualHeight();
    }

    @Override
    public Random setRandomSeed(int p_72843_1_, int p_72843_2_, int p_72843_3_) {
        return getActualWorld().setRandomSeed(p_72843_1_, p_72843_2_, p_72843_3_);
    }

    @Override
    public CrashReportCategory addWorldInfoToCrashReport(CrashReport report) {
        return getActualWorld().addWorldInfoToCrashReport(report);
    }

    @Override
    public double getHorizon() {
        return getActualWorld().getHorizon();
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        getActualWorld().sendBlockBreakProgress(breakerId, pos, progress);
    }

    @Override
    public Calendar getCurrentDate() {
        return getActualWorld().getCurrentDate();
    }

    @Override
    public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, NBTTagCompound compund) {
        getActualWorld().makeFireworks(x, y, z, motionX, motionY, motionZ, compund);
    }

    @Override
    public Scoreboard getScoreboard() {
        return getActualWorld().getScoreboard();
    }

    @Override
    public void updateComparatorOutputLevel(BlockPos pos, Block blockIn) {
        getActualWorld().updateComparatorOutputLevel(pos, blockIn);
    }

    @Override
    public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
        return getActualWorld().getDifficultyForLocation(pos);
    }

    @Override
    public EnumDifficulty getDifficulty() {
        return getActualWorld().getDifficulty();
    }

    @Override
    public int getSkylightSubtracted() {
        return getActualWorld().getSkylightSubtracted();
    }

    @Override
    public void setSkylightSubtracted(int newSkylightSubtracted) {
        getActualWorld().setSkylightSubtracted(newSkylightSubtracted);
    }

    @Override
    public int getLastLightningBolt() {
        return getActualWorld().getLastLightningBolt();
    }

    @Override
    public void setLastLightningBolt(int lastLightningBoltIn) {
        getActualWorld().setLastLightningBolt(lastLightningBoltIn);
    }

    @Override
    public VillageCollection getVillageCollection() {
        return getActualWorld().getVillageCollection();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return getActualWorld().getWorldBorder();
    }

    @Override
    public boolean isSpawnChunk(int x, int z) {
        return getActualWorld().isSpawnChunk(x, z);
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side) {
        return getActualWorld().isSideSolid(pos, side);
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return getActualWorld().isSideSolid(pos, side, _default);
    }

    @Override
    public ImmutableSetMultimap<ChunkPos, Ticket> getPersistentChunks() {
        return getActualWorld().getPersistentChunks();
    }

    @Override
    public Iterator<Chunk> getPersistentChunkIterable(Iterator<Chunk> chunkIterator) {
        return getActualWorld().getPersistentChunkIterable(chunkIterator);
    }

    @Override
    public int getBlockLightOpacity(BlockPos pos) {
        return getActualWorld().getBlockLightOpacity(pos);
    }

    @Override
    public int countEntities(EnumCreatureType type, boolean forSpawnCount) {
        return getActualWorld().countEntities(type, forSpawnCount);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return getActualWorld().hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return getActualWorld().getCapability(capability, facing);
    }

    @Override
    public MapStorage getPerWorldStorage() {
        return getActualWorld().getPerWorldStorage();
    }

    @Override
    public void sendPacketToServer(Packet<?> packetIn) {
        getActualWorld().sendPacketToServer(packetIn);
    }

    @Override
    public LootTableManager getLootTableManager() {
        return getActualWorld().getLootTableManager();
    }

    @Override
    public BlockPos findNearestStructure(String p_190528_1_, BlockPos p_190528_2_, boolean p_190528_3_) {
        return getActualWorld().findNearestStructure(p_190528_1_, p_190528_2_, p_190528_3_);
    }

}
