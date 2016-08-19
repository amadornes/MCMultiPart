package mcmultipart.multipart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mcmultipart.block.BlockMultipartContainer;
import mcmultipart.capabilities.CapabilityWrapperRegistry;
import mcmultipart.capabilities.ISlottedCapabilityProvider;
import mcmultipart.event.PartEvent;
import mcmultipart.multipart.ISolidPart.ISolidTopPart;
import mcmultipart.network.MessageMultipartChange;
import mcmultipart.network.MessageMultipartChange.Type;
import mcmultipart.raytrace.PartMOP;
import mcmultipart.raytrace.RayTraceUtils.AdvancedRayTraceResultPart;
import mcmultipart.util.IWorldLocation;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Helper class that contains all the logic required for an {@link IMultipartContainer} to work, as well as methods that are forwarded to
 * each of the parts.<br/>
 * You can implement {@link IMultipartContainer} in your {@link TileEntity} and forward the calls to an instance of this class for default
 * multipart container logic.
 */
public class MultipartContainer implements IMultipartContainer {

    private IWorldLocation location;
    private IMultipartContainerListener listener;

    private boolean canTurnIntoBlock;
    private BiMap<UUID, IMultipart> partMap = HashBiMap.create();
    private Map<PartSlot, ISlottedPart> slotMap = new HashMap<PartSlot, ISlottedPart>();

    public MultipartContainer(IWorldLocation location, boolean canTurnIntoBlock) {

        this.location = location;
        this.canTurnIntoBlock = canTurnIntoBlock;
    }

    public MultipartContainer(IWorldLocation location, boolean canTurnIntoBlock, MultipartContainer container) {

        this.location = location;
        this.canTurnIntoBlock = canTurnIntoBlock;
        this.partMap = HashBiMap.create(container.partMap);
        this.slotMap = new HashMap<PartSlot, ISlottedPart>(container.slotMap);
        for (IMultipart part : partMap.values())
            part.setContainer(this);
    }

    public void setListener(IMultipartContainerListener listener) {

        this.listener = listener;
    }

    @Override
    public World getWorldIn() {

        return location.getWorldIn();
    }

    @Override
    public BlockPos getPosIn() {

        return location.getPosIn();
    }

    public boolean canTurnIntoBlock() {

        return canTurnIntoBlock;
    }

    @Override
    public Collection<? extends IMultipart> getParts() {

        return partMap.values();
    }

    @Override
    public ISlottedPart getPartInSlot(PartSlot slot) {

        return slotMap.get(slot);
    }

    @Override
    public boolean canAddPart(IMultipart part) {

        if (part == null || getParts().contains(part)) return false;

        if (part instanceof ISlottedPart) {
            for (PartSlot s : ((ISlottedPart) part).getSlotMask())
                if (getPartInSlot(s) != null) return false;
        }

        if (!occlusionTest(part)) return false;

        List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
        part.addCollisionBoxes(new AxisAlignedBB(0, 0, 0, 1, 1, 1), list, null);
        if (getWorldIn() != null && getPosIn() != null) for (AxisAlignedBB bb : list)
            if (!getWorldIn().checkNoEntityCollision(bb.offset(getPosIn().getX(), getPosIn().getY(), getPosIn().getZ()))) return false;

        return true;
    }

    @Override
    public boolean canReplacePart(IMultipart oldPart, IMultipart newPart) {

        if (oldPart == null) return canAddPart(newPart);
        if (newPart == null || getParts().contains(newPart)) return false;

        if (newPart instanceof ISlottedPart) {
            for (PartSlot s : ((ISlottedPart) newPart).getSlotMask()) {
                IMultipart p = getPartInSlot(s);
                if (p != null && p != oldPart) return false;
            }
        }

        if (!occlusionTest(newPart, oldPart)) return false;

        List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
        newPart.addCollisionBoxes(new AxisAlignedBB(0, 0, 0, 1, 1, 1), list, null);
        if (getWorldIn() != null && getPosIn() != null) for (AxisAlignedBB bb : list)
            if (!getWorldIn().checkNoEntityCollision(bb.offset(getPosIn().getX(), getPosIn().getY(), getPosIn().getZ()))) return false;

        return true;
    }

    @Override
    public void addPart(IMultipart part) {

        if (getWorldIn().isRemote) throw new IllegalStateException("Attempted to add a part on the client!");
        addPart(part, true, true, true, true, UUID.randomUUID());
    }

    public void addPart(IMultipart part, boolean notifyPart, boolean notifyNeighbors, boolean tryConvert, boolean postEvent, UUID id) {

        if (part == null) throw new NullPointerException("Attempted to add a null part at " + getPosIn());
        if (getParts().contains(part))
            throw new IllegalArgumentException("Attempted to add a duplicate part at " + getPosIn() + " (" + part + ")");

        if (listener != null) listener.onAddPartPre(part);

        part.setContainer(this);

        BiMap<UUID, IMultipart> partMap = HashBiMap.create(this.partMap);
        Map<PartSlot, ISlottedPart> slotMap = new HashMap<PartSlot, ISlottedPart>(this.slotMap);

        partMap.put(id, part);
        if (part instanceof ISlottedPart) {
            for (PartSlot s : ((ISlottedPart) part).getSlotMask())
                slotMap.put(s, (ISlottedPart) part);
        }

        this.partMap = partMap;
        this.slotMap = slotMap;

        if (postEvent) MinecraftForge.EVENT_BUS.post(new PartEvent.Add(part));

        if (notifyPart) part.onAdded();
        if (notifyNeighbors) {
            notifyPartChanged(part);
            getWorldIn().checkLight(getPosIn());
        }

        if (listener != null) listener.onAddPartPost(part);

        if (getWorldIn() != null && !getWorldIn().isRemote && (!canTurnIntoBlock || !tryConvert || !MultipartRegistry.convertToBlock(this)))
            MessageMultipartChange.newPacket(getWorldIn(), getPosIn(), part, Type.ADD).send(getWorldIn());
    }

    @Override
    public void removePart(IMultipart part) {

        removePart(part, true, true, true);
    }

    public void removePart(IMultipart part, boolean notifyPart, boolean notifyNeighbors, boolean postEvent) {

        if (part == null) throw new NullPointerException("Attempted to remove a null part from " + getPosIn());
        if (!getParts().contains(part))
            throw new IllegalArgumentException("Attempted to remove a part that doesn't exist from " + getPosIn() + " (" + part + ")");

        BiMap<UUID, IMultipart> partMap = HashBiMap.create(this.partMap), oldPartMap = this.partMap;
        Map<PartSlot, ISlottedPart> slotMap = new HashMap<PartSlot, ISlottedPart>(this.slotMap), oldSlotMap = this.slotMap;

        if (listener != null) listener.onRemovePartPre(part);

        partMap.inverse().remove(part);
        if (part instanceof ISlottedPart) {
            Iterator<Entry<PartSlot, ISlottedPart>> it = slotMap.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getValue() == part) it.remove();
            }
        }

        this.partMap = partMap;
        this.slotMap = slotMap;

        if (postEvent) MinecraftForge.EVENT_BUS.post(new PartEvent.Remove(part));

        // Yes, it's a bit of a dirty solution, but it's the best I could come up with. The part must not be there in the if statement :P
        if (getWorldIn() != null && !getWorldIn().isRemote && (!canTurnIntoBlock || !MultipartRegistry.convertToBlock(this))) {
            this.partMap = oldPartMap;
            this.slotMap = oldSlotMap;
            MessageMultipartChange.newPacket(getWorldIn(), getPosIn(), part, Type.REMOVE).send(getWorldIn());
            this.partMap = partMap;
            this.slotMap = slotMap;
        }

        if (notifyPart) part.onRemoved();
        if (notifyNeighbors) {
            notifyPartChanged(part);
            getWorldIn().checkLight(getPosIn());
        }

        part.setContainer(null);

        if (listener != null) listener.onRemovePartPost(part);
    }

    @Override
    public UUID getPartID(IMultipart part) {

        return partMap.inverse().get(part);
    }

    @Override
    public IMultipart getPartFromID(UUID id) {

        return partMap.get(id);
    }

    @Override
    public void addPart(UUID id, IMultipart part) {

        addPart(part, true, true, true, true, id);
    }

    @Override
    public boolean occlusionTest(IMultipart part, IMultipart... ignored) {

        List<IMultipart> ignoredList = Arrays.asList(ignored);

        for (IMultipart p : getParts())
            if (!ignoredList.contains(p) && (!p.occlusionTest(part) || !part.occlusionTest(p))) return false;

        return true;
    }

    public void notifyPartChanged(IMultipart part) {

        for (IMultipart p : getParts())
            if (p != part) p.onPartChanged(part);
        getWorldIn().notifyNeighborsOfStateChange(getPosIn(), getWorldIn().getBlockState(getPosIn()).getBlock());
    }

    public AdvancedRayTraceResultPart collisionRayTrace(Vec3d start, Vec3d end) {

        double dist = Double.POSITIVE_INFINITY;
        AdvancedRayTraceResultPart current = null;

        for (IMultipart p : getParts()) {
            AdvancedRayTraceResultPart result = p.collisionRayTrace(start, end);
            if (result == null) continue;
            double d = result.squareDistanceTo(start);
            if (d <= dist) {
                dist = d;
                current = result;
            }
        }

        return current;
    }

    public void addCollisionBoxes(AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity collidingEntity) {

        for (IMultipart p : getParts())
            p.addCollisionBoxes(entityBox, collidingBoxes, collidingEntity);
    }

    public int getLightValue() {

        int max = 0;
        for (IMultipart part : getParts())
            max = Math.max(max, part.getLightValue());
        return max;
    }

    public ItemStack getPickBlock(EntityPlayer player, PartMOP hit) {

        return hit.partHit.getPickBlock(player, hit);
    }

    public List<ItemStack> getDrops() {

        List<ItemStack> list = new ArrayList<ItemStack>();
        for (IMultipart part : getParts())
            list.addAll(part.getDrops());
        return list;
    }

    public boolean harvest(EntityPlayer player, PartMOP hit) {

        if (getWorldIn().isRemote) return false;
        if (hit == null) {
            for (IMultipart part : getParts())
                part.harvest(null, hit);
            return true;
        }
        if (!partMap.values().contains(hit.partHit)) return false;
        if (getWorldIn().isRemote) return getParts().size() - 1 == 0;
        hit.partHit.harvest(player, hit);
        return getParts().isEmpty();
    }

    public float getHardness(EntityPlayer player, PartMOP hit) {

        if (!partMap.values().contains(hit.partHit)) return -1;
        BlockMultipartContainer.breakingPart =  hit.partHit instanceof IMaterialPart ? (IMaterialPart) hit.partHit : null;
        try {
            return hit.partHit.getStrength(player, hit);
        } finally {
            BlockMultipartContainer.breakingPart =  null;
        }
    }

    public void onNeighborBlockChange(Block block) {

        for (IMultipart part : getParts())
            part.onNeighborBlockChange(block);
    }

    public void onNeighborTileChange(EnumFacing facing) {

        for (IMultipart part : getParts())
            part.onNeighborTileChange(facing);
    }

    public boolean onActivated(EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, PartMOP hit) {

        if (hit == null) return false;
        if (!partMap.values().contains(hit.partHit)) return false;
        return hit.partHit.onActivated(playerIn, hand, heldItem, hit);
    }

    public void onClicked(EntityPlayer playerIn, PartMOP hit) {

        if (hit == null) return;
        if (!partMap.values().contains(hit.partHit)) return;
        hit.partHit.onClicked(playerIn, hit);
    }

    public boolean canConnectRedstone(EnumFacing side) {

        return MultipartRedstoneHelper.canConnectRedstone(this, side);
    }

    public int getWeakSignal(EnumFacing side) {

        return MultipartRedstoneHelper.getWeakSignal(this, side);
    }

    public int getStrongSignal(EnumFacing side) {

        return MultipartRedstoneHelper.getStrongSignal(this, side);
    }

    public boolean isSideSolid(EnumFacing side) {

        IMultipart slotPart = getPartInSlot(PartSlot.getFaceSlot(side));
        if (slotPart != null && slotPart instanceof ISolidPart) return ((ISolidPart) slotPart).isSideSolid(side);
        for (IMultipart p : getParts())
            if ((!(p instanceof ISlottedPart) || ((ISlottedPart) p).getSlotMask().isEmpty()) && p instanceof ISolidPart)
                if (((ISolidPart) p).isSideSolid(side)) return true;
        return false;
    }

    public boolean canPlaceTorchOnTop() {

        IMultipart slotPart = getPartInSlot(PartSlot.getFaceSlot(EnumFacing.UP));
        if (slotPart != null && slotPart instanceof ISolidTopPart) return ((ISolidTopPart) slotPart).canPlaceTorchOnTop();
        for (IMultipart p : getParts())
            if (p instanceof ISolidTopPart) if (((ISolidTopPart) p).canPlaceTorchOnTop()) return true;
        return false;
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(Random rand) {

        for (IMultipart p : getParts())
            p.randomDisplayTick(rand);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {

        NBTTagList partList = new NBTTagList();
        for (Entry<UUID, IMultipart> entry : partMap.entrySet()) {
            NBTTagCompound t = new NBTTagCompound();
            t.setString("__partID", entry.getKey().toString());
            t.setString("__partType", entry.getValue().getType().toString());
            t = entry.getValue().writeToNBT(t);
            partList.appendTag(t);
        }
        tag.setTag("partList", partList);
        return tag;
    }

    public void readFromNBT(NBTTagCompound tag) {

        partMap.clear();
        slotMap.clear();

        NBTTagList partList = tag.getTagList("partList", new NBTTagCompound().getId());
        for (int i = 0; i < partList.tagCount(); i++) {
            NBTTagCompound t = partList.getCompoundTagAt(i);
            UUID id = UUID.fromString(t.getString("__partID"));
            IMultipart part = MultipartRegistry.createPart(new ResourceLocation(t.getString("__partType")), t);
            if (part != null) addPart(part, false, false, false, false, id);
        }
    }

    public NBTTagCompound writeDescription(NBTTagCompound tag) {

        NBTTagList partList = new NBTTagList();
        for (Entry<UUID, IMultipart> entry : partMap.entrySet()) {
            NBTTagCompound t = new NBTTagCompound();
            t.setString("__partID", entry.getKey().toString());
            t.setString("__partType", entry.getValue().getType().toString());
            ByteBuf buf = Unpooled.buffer();
            entry.getValue().writeUpdatePacket(new PacketBuffer(buf));
            t.setByteArray("data", buf.array());
            partList.appendTag(t);
        }
        tag.setTag("partList", partList);
        return tag;
    }

    public void readDescription(NBTTagCompound tag) {

        NBTTagList partList = tag.getTagList("partList", new NBTTagCompound().getId());
        for (int i = 0; i < partList.tagCount(); i++) {
            NBTTagCompound t = partList.getCompoundTagAt(i);
            UUID id = UUID.fromString(t.getString("__partID"));
            IMultipart part = partMap.get(id);
            if (part == null) {
                part = MultipartRegistry.createPart(new ResourceLocation(t.getString("__partType")),
                        new PacketBuffer(Unpooled.copiedBuffer(t.getByteArray("data"))));
                addPart(part, false, false, false, false, id);
            } else {
                part.readUpdatePacket(new PacketBuffer(Unpooled.copiedBuffer(t.getByteArray("data"))));
            }
        }
    }

    public List<PartState> getExtendedStates(IBlockAccess world, BlockPos pos) {

        List<PartState> states = new ArrayList<PartState>();
        for (IMultipart part : getParts()) {
            PartState state = PartState.fromPart(part, world, pos);
            if (state != null) states.add(state);
        }
        return states;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, PartSlot slot, EnumFacing facing) {

        if (slot == null) {
            for (IMultipart p : getParts())
                if (!(p instanceof ISlottedPart) || ((ISlottedPart) p).getSlotMask().isEmpty())
                    if (p instanceof ICapabilityProvider && ((ICapabilityProvider) p).hasCapability(capability, facing)) return true;
            return false;
        }

        IMultipart part = getPartInSlot(slot);
        return part instanceof ISlottedCapabilityProvider ? ((ISlottedCapabilityProvider) part).hasCapability(capability, slot, facing)
                : part instanceof ICapabilityProvider ? ((ICapabilityProvider) part).hasCapability(capability, facing) : false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, PartSlot slot, EnumFacing facing) {

        if (slot == null) {
            List<T> implementations = new ArrayList<T>();
            for (IMultipart p : getParts()) {
                if (!(p instanceof ISlottedPart) || ((ISlottedPart) p).getSlotMask().isEmpty()) {
                    if (p instanceof ICapabilityProvider) {
                        T impl = ((ICapabilityProvider) p).getCapability(capability, facing);
                        if (impl != null) implementations.add(impl);
                    }
                }
            }

            if (implementations.isEmpty()) return null;
            else if (implementations.size() == 1) return implementations.get(0);
            else return CapabilityWrapperRegistry.wrap(capability, implementations);
        }

        IMultipart part = getPartInSlot(slot);
        return part instanceof ISlottedCapabilityProvider ? ((ISlottedCapabilityProvider) part).getCapability(capability, slot, facing)
                : part instanceof ICapabilityProvider ? ((ICapabilityProvider) part).getCapability(capability, facing) : null;
    }

    public Boolean isAABBInsideMaterial(AxisAlignedBB aabb, Material material) {

        Boolean def = null;
        for (IMultipart part : getParts()) {
            Boolean is = part.isAABBInsideMaterial(aabb, material);
            if (is != null) {
                if (is == true) return true;
                else def = false;
            }
        }
        return def;
    }

    public Boolean isEntityInsideMaterial(Entity entity, double yToTest, Material material, boolean testingHead) {

        Boolean def = null;
        for (IMultipart part : getParts()) {
            Boolean is = part.isEntityInsideMaterial(entity, yToTest, material, testingHead);
            if (is != null) {
                if (is == true) return true;
                else def = false;
            }
        }
        return def;
    }

    public void onEntityStanding(Entity entity) {

        for (IMultipart part : getParts())
            part.onEntityStanding(entity);
    }

    public void onEntityCollided(Entity entity) {

        for (IMultipart part : getParts())
            part.onEntityCollided(entity);
    }

}
