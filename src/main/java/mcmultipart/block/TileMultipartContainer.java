package mcmultipart.block;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import mcmultipart.MCMultiPart;
import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.multipart.MultipartOcclusionHelper;
import mcmultipart.api.ref.MCMPCapabilities;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.api.slot.SlotUtil;
import mcmultipart.capability.CapabilityJoiner;
import mcmultipart.client.TESRMultipartContainer;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.multipart.PartInfo;
import mcmultipart.network.MultipartNetworkHandler;
import mcmultipart.network.PacketMultipartAdd;
import mcmultipart.network.PacketMultipartRemove;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.Mirror;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.GameData;

public class TileMultipartContainer extends TileEntity implements IMultipartContainer {

    private boolean isInWorld = true;
    private final Map<IPartSlot, PartInfo> parts = new ConcurrentHashMap<>(), partView = Collections.unmodifiableMap(parts);
    private Map<IPartSlot, NBTTagCompound> missingParts;
    private World loadingWorld;

    private TileMultipartContainer(World world, BlockPos pos) {
        setWorld(world);
        setPos(pos);
        isInWorld = false;
    }

    public TileMultipartContainer() {
    }

    @Override
    public void setWorld(World world) {
        World prevWorld = getWorld();
        super.setWorld(world);
        isInWorld = true;
        if (world != prevWorld) {
            parts.values().forEach(PartInfo::refreshWorld);
        }
    }

    @Override
    protected void setWorldCreate(World world) {
        loadingWorld = world;
    }

    @Override
    public void setPos(BlockPos pos) {
        super.setPos(pos);
        forEachTile(te -> te.setPartPos(pos));
    }

    public boolean isInWorld() {
        return isInWorld;
    }

    @Override
    public World getPartWorld() {
        return getWorld();
    }

    @Override
    public BlockPos getPartPos() {
        return getPos();
    }

    @Override
    public Optional<IPartInfo> get(IPartSlot slot) {
        if (slot == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(parts.get(slot));
    }

    @Override
    public boolean canAddPart(IPartSlot slot, IBlockState state, IMultipartTile tile) {
        Preconditions.checkNotNull(slot);
        Preconditions.checkNotNull(state);

        PartInfo otherInfo = null;
        World otherWorld = null;
        try {
            if (!isInWorld) { // Simulate being a multipart if it's not one
                otherInfo = getParts().values().iterator().next();;
                otherWorld = otherInfo.getTile() != null ? otherInfo.getTile().getPartWorld() : null;
                otherInfo.refreshWorld();
            }

            IMultipart part = MultipartRegistry.INSTANCE.getPart(state.getBlock());
            Preconditions.checkState(part != null, "The blockstate " + state + " could not be converted to a multipart!");
            PartInfo info = new PartInfo(this, slot, part, state, tile);

            // If any of the slots required by this multipart aren't empty, fail.
            Set<IPartSlot> partSlots = Sets.newIdentityHashSet();
            partSlots.addAll(part.getGhostSlots(info));
            partSlots.add(slot);
            if (partSlots.stream().anyMatch(parts::containsKey)
                    || parts.values().stream().map(i -> i.getPart().getGhostSlots(i)).flatMap(Set::stream).anyMatch(partSlots::contains)) {
                partSlots.clear();
                return false;
            }
            partSlots.clear();

            // If the occlusion boxes of this part intersect with any other parts', fail.
            if (MultipartOcclusionHelper.testContainerPartIntersection(this, info)) {
                return false;
            }
        } finally {
            if (otherWorld != null) { // Return back to the old world if it's not a multipart
                otherInfo.setWorld(otherWorld);
            }
        }

        return true;
    }

    @Override
    public void addPart(IPartSlot slot, IBlockState state, IMultipartTile tile) {
        int currentTicking = countTickingParts();
        int newTicking = currentTicking + (tile != null && tile.isTickable() ? 1 : 0);

        TileMultipartContainer container = this;
        if (currentTicking == 0 && newTicking > 0) {
            container = new TileMultipartContainer.Ticking(getWorld(), getPos());
            transferTo(container);
        }

        if (!isInWorld) {
            PartInfo info = parts.values().iterator().next();
            info.setContainer(container);
            if (!container.isInWorld) {
                info.refreshWorld();
            }
        }

        IMultipart part = MultipartRegistry.INSTANCE.getPart(state.getBlock());
        Preconditions.checkState(part != null, "The blockstate " + state + " could not be converted to a multipart!");
        container.addPartDo(slot, state, part, tile);

        IBlockState prevSt = getWorld().getBlockState(getPos());

        if (container != this) { // If we require ticking, place a ticking TE
            isInWorld = false;
            getWorld().setBlockState(getPos(), MCMultiPart.multipart.getDefaultState()//
                    .withProperty(BlockMultipartContainer.PROPERTY_TICKING, true), 0);
            getWorld().setTileEntity(getPos(), container);
        } else if (!isInWorld) { // If we aren't in the world, place the TE
            getWorld().setBlockState(getPos(), MCMultiPart.multipart.getDefaultState()//
                    .withProperty(BlockMultipartContainer.PROPERTY_TICKING, this instanceof Ticking), 0);
            getWorld().setTileEntity(getPos(), this);
        }

        IBlockState st = getWorld().getBlockState(getPos());
        getWorld().markAndNotifyBlock(getPos(), null, prevSt, st, 1); // Only cause a block update, clients are notified through a packet
        getWorld().checkLight(getPos());
    }

    private void addPartDo(IPartSlot slot, IBlockState state, IMultipart part, IMultipartTile tile) {
        if (missingParts != null) {
            missingParts.remove(slot);
        }

        PartInfo info = new PartInfo(this, slot, part, state, tile);
        add(slot, info);

        if (tile != null) {
            tile.validatePart();
        }

        if (!getWorld().isRemote) {
            part.onAdded(info);
            parts.values().forEach(i -> {
                if (i != info) {
                    i.getPart().onPartAdded(i, info);
                }
            });

            MultipartNetworkHandler.sendToAllWatching(new PacketMultipartAdd(info), getWorld(), getPos());
        }
    }

    @Override
    public void removePart(IPartSlot slot) {
        PartInfo info = parts.get(slot);
        IMultipartTile tile = info.getTile();

        int currentTicking = countTickingParts();
        int newTicking = currentTicking - (tile != null && tile.isTickable() ? 1 : 0);

        TileMultipartContainer container = this;
        if (currentTicking > 0 && newTicking == 0) {
            container = new TileMultipartContainer(getWorld(), getPos());
            transferTo(container);
        }

        container.removePartDo(slot, info);

        IBlockState prevSt = getWorld().getBlockState(getPos());

        if (container != this) { // If we don't require ticking, place a normal TE
            isInWorld = false;
            getWorld().setBlockState(getPos(), MCMultiPart.multipart.getDefaultState()//
                    .withProperty(BlockMultipartContainer.PROPERTY_TICKING, false), 0);
            getWorld().setTileEntity(getPos(), container);
        }

        IBlockState st = getWorld().getBlockState(getPos());
        getWorld().markAndNotifyBlock(getPos(), null, prevSt, st, 1); // Only cause a block update, clients are notified through a packet
        getWorld().checkLight(getPos());
    }

    private void removePartDo(IPartSlot slot, PartInfo info) {
        if (missingParts != null) {
            missingParts.remove(slot);
        }

        remove(slot);

        if (!getWorld().isRemote) {
            info.getPart().breakPart(info);
            info.getPart().onRemoved(info);
            parts.values().forEach(i -> i.getPart().onPartRemoved(i, info));

            MultipartNetworkHandler.sendToAllWatching(new PacketMultipartRemove(getPos(), slot), getWorld(), getPos());
        }

        if (parts.size() == 1) {
            PartInfo part = parts.values().iterator().next();
            getWorld().setBlockState(getPos(), part.getState(), 0);
            if (part.getTile() != null) {
                getWorld().setTileEntity(getPos(), part.getTile().getTileEntity());
            }
        }
    }

    private int countTickingParts() {
        return (int) parts.values().stream().map(IPartInfo::getTile).filter(t -> t != null && t.isTickable()).count();
    }

    protected void add(IPartSlot slot, PartInfo partInfo) {
        parts.put(slot, partInfo);
        partInfo.setContainer(this);
    }

    protected void remove(IPartSlot slot) {
        parts.remove(slot);
    }

    protected void clear() {
        parts.clear();
    }

    protected void transferTo(TileMultipartContainer container) {
        container.isInWorld = isInWorld;
        parts.forEach(container::add); // Doing it like this to add them to the ticking list if needed
        if (missingParts != null) {
            container.missingParts = missingParts;
        }
    }

    @Override
    public Map<IPartSlot, PartInfo> getParts() {
        return partView;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tag = writeParts(tag, false);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        readParts(tag, false, loadingWorld);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        writeParts(tag, true);
        return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.readFromNBT(tag);
        readParts(tag, true, getPartWorld());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPartPos(), 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        handleUpdateTag(pkt.getNbtCompound());
    }

    private NBTTagCompound writeParts(NBTTagCompound tag, boolean update) {
        NBTTagCompound parts = new NBTTagCompound();
        this.parts.forEach((s, i) -> {
            NBTTagCompound t = new NBTTagCompound();
            t.setInteger("state", MCMultiPart.stateMap.get(i.getState()));
            IMultipartTile tile = i.getTile();
            if (tile != null) {
                if (update) {
                    t.setTag("tile", tile.getPartUpdateTag());
                } else {
                    t.setTag("tile", tile.writePartToNBT(new NBTTagCompound()));
                }
            }
            parts.setTag(Integer.toString(MCMultiPart.slotRegistry.getID(s)), t);
        });
        if (this.missingParts != null) {
            this.missingParts.forEach((s, t) -> parts.setTag(Integer.toString(MCMultiPart.slotRegistry.getID(s)), t));
        }
        tag.setTag("parts", parts);
        return tag;
    }

    private void readParts(NBTTagCompound tag, boolean update, World world) {
        World prevWorld = this.world;
        this.world = world;
        ObjectIntIdentityMap<IBlockState> stateMap = GameData.getBlockStateIDMap();
        NBTTagCompound parts = tag.getCompoundTag("parts");
        Set<IPartSlot> visitedSlots = new HashSet<>();
        for (String sID : parts.getKeySet()) {
            IPartSlot slot = MCMultiPart.slotRegistry.getValue(Integer.parseInt(sID));
            if (slot != null) {
                visitedSlots.add(slot);
                PartInfo prevInfo = this.parts.get(slot);

                NBTTagCompound t = parts.getCompoundTag(sID);
                IBlockState state = stateMap.getByValue(t.getInteger("state"));
                if (prevInfo != null) {
                    prevInfo.setState(state);
                    if (t.hasKey("tile")) {
                        NBTTagCompound tileTag = t.getCompoundTag("tile");
                        IMultipartTile tile = prevInfo.getTile();
                        if (update) {
                            if (tile == null) {
                                tile = prevInfo.getPart().createMultipartTile(world, slot, state);
                            }
                            tile.handlePartUpdateTag(tileTag);
                        } else {
                            tile = prevInfo.getPart().loadMultipartTile(world, tileTag);
                        }
                        prevInfo.setTile(tile);
                    }
                } else {
                    IMultipart part = MultipartRegistry.INSTANCE.getPart(state.getBlock());
                    if (part != null) {
                        IMultipartTile tile = null;
                        if (t.hasKey("tile")) {
                            NBTTagCompound tileTag = t.getCompoundTag("tile");
                            if (update) {
                                tile = part.createMultipartTile(world, slot, state);
                                tile.handlePartUpdateTag(tileTag);
                            } else {
                                tile = part.loadMultipartTile(world, tileTag);
                            }
                        }
                        add(slot, new PartInfo(this, slot, part, state, tile));
                    } else if (!update) {
                        if (missingParts == null) {
                            missingParts = new HashMap<>();
                        }
                        missingParts.put(slot, t);
                    } else {
                        throw new IllegalStateException("Server sent a multipart of type " + state + " which is not registered on the client.");
                    }
                }
            }
        }
        Set<IPartSlot> removed = new HashSet<>(this.parts.keySet());
        removed.removeAll(visitedSlots);
        removed.forEach(this::remove);
        this.world = prevWorld;
    }

    @Override
    public void onLoad() {
        forEachTile(te -> te.setPartPos(getPartPos()));
        parts.values().forEach(PartInfo::refreshWorld);
        forEachTile(IMultipartTile::onPartLoad);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        forEachTile(IMultipartTile::onPartChunkUnload);
    }

    @Override
    public void mirror(Mirror mirror) {
        super.mirror(mirror);
        forEachTile(te -> te.mirrorPart(mirror));
    }

    @Override
    public void rotate(Rotation rotation) {
        super.rotate(rotation);
        forEachTile(te -> te.rotatePart(rotation));
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!isInWorld) {
            forEachTile(IMultipartTile::invalidatePart);
        }
    }

    @Override
    public void validate() {
        super.validate();
        forEachTile(IMultipartTile::validatePart);
    }

    @Override
    public void updateContainingBlockInfo() {
        super.updateContainingBlockInfo();
        forEachTile(IMultipartTile::updatePartContainerInfo);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return parts.values().stream().map(IPartInfo::getTile).filter(t -> t != null).mapToDouble(IMultipartTile::getMaxPartRenderDistanceSquared)
                .max().orElse(super.getMaxRenderDistanceSquared());
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return parts.values().stream().map(IPartInfo::getTile).filter(t -> t != null)//
                .reduce(super.getRenderBoundingBox(), (a, b) -> a.union(b.getPartRenderBoundingBox()), (a, b) -> b);
    }

    @Override
    public boolean canRenderBreaking() {
        return true;
    }

    @Override
    public boolean hasFastRenderer() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return hasFastRendererC();
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    private boolean hasFastRendererC() {
        for (IPartInfo info : parts.values()) {
            TileEntity te = info.getTile() != null ? info.getTile().getTileEntity() : null;
            if (te != null && TileEntityRendererDispatcher.instance.getRenderer(te) != null && !te.hasFastRenderer()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            shouldRenderInPassC(pass);
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void shouldRenderInPassC(int pass) {
        TESRMultipartContainer.pass = pass;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == MCMPCapabilities.MULTIPART_CONTAINER) {
            return true;
        }
        if (SlotUtil.viewContainer(this, i -> i.getTile() != null && i.getTile().hasPartCapability(capability, facing),
                l -> l.stream().anyMatch(a -> a), false, true, facing)) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == MCMPCapabilities.MULTIPART_CONTAINER) {
            return (T) this;
        }
        T val = SlotUtil
                .viewContainer(this,
                        i -> i.getTile() != null && i.getTile().hasPartCapability(capability, facing)
                                ? i.getTile().getPartCapability(capability, facing) : null,
                        l -> CapabilityJoiner.join(capability, l), null, true, facing);
        if (val != null) {
            return val;
        }
        return super.getCapability(capability, facing);
    }

    protected void forEachTile(Consumer<IMultipartTile> consumer) {
        for (PartInfo info : parts.values()) {
            IMultipartTile tile = info.getTile();
            if (tile != null) {
                consumer.accept(tile);
            }
        }
    }

    public static class Ticking extends TileMultipartContainer implements ITickable {

        private final Set<ITickable> tickingParts = Collections.newSetFromMap(new WeakHashMap<>());

        private Ticking(World world, BlockPos pos) {
            super(world, pos);
        }

        public Ticking() {
        }

        @Override
        public void update() {
            if (tickingParts.isEmpty()) {
                IBlockState state = MCMultiPart.multipart.getDefaultState().withProperty(BlockMultipartContainer.PROPERTY_TICKING, false);
                getPartWorld().setBlockState(getPartPos(), state, 0);
                TileMultipartContainer container = (TileMultipartContainer) MultipartHelper.getContainer(getWorld(), getPos()).get();
                transferTo(container);

                getWorld().notifyBlockUpdate(getPos(), state, state, 3);
                getWorld().checkLight(getPos());
                return;
            }
            tickingParts.forEach(ITickable::update);
        }

        @Override
        protected void add(IPartSlot slot, PartInfo partInfo) {
            super.add(slot, partInfo);
            IMultipartTile te = partInfo.getTile();
            if (te != null && te.isTickable()) {
                tickingParts.add(te.getTickable());
            }
        }

        @Override
        protected void remove(IPartSlot slot) {
            getPartTile(slot).ifPresent(tickingParts::remove);
            super.remove(slot);
        }

        @Override
        protected void clear() {
            tickingParts.clear();
            super.clear();
        }

    }

    // Just make a tile. Not sure why this needs the world and position, but apparently it does...
    public static IMultipartContainer createTile(World world, BlockPos pos) {
        return new TileMultipartContainer(world, pos);
    }

    public static IMultipartContainer createTileFromWorldInfo(World world, BlockPos pos) {
        PartInfo info = PartInfo.fromWorld(world, pos);
        boolean tick = info.getTile() != null && info.getTile().isTickable();

        TileMultipartContainer container = tick ? new TileMultipartContainer.Ticking(world, pos) : new TileMultipartContainer(world, pos);
        container.isInWorld = false;
        container.add(info.getSlot(), info);

        return container;
    }

}
