package mcmultipart.multipart;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import mcmultipart.network.MessageMultipartChange;
import mcmultipart.network.MessageMultipartChange.Type;
import mcmultipart.raytrace.PartMOP;
import mcmultipart.raytrace.RayTraceUtils.RayTraceResult;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import putsomewhereelse.IWorldLocation;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class MultipartContainer implements IMultipartContainer {

    private IWorldLocation location;
    private BiMap<UUID, IMultipart> partMap = HashBiMap.create();
    private Map<PartSlot, ISlottedPart> slotMap = new HashMap<PartSlot, ISlottedPart>();

    public MultipartContainer(IWorldLocation location) {

        this.location = location;
    }

    public MultipartContainer(IWorldLocation location, MultipartContainer container) {

        this.location = location;
        this.partMap = HashBiMap.create(container.partMap);
        this.slotMap = new HashMap<PartSlot, ISlottedPart>(container.slotMap);
        for (IMultipart part : partMap.values())
            part.setContainer(this);
    }

    @Override
    public World getWorld() {

        return location.getWorld();
    }

    @Override
    public BlockPos getPos() {

        return location.getPos();
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

        if (part instanceof ISlottedPart) {
            for (PartSlot s : ((ISlottedPart) part).getSlotMask())
                if (getPartInSlot(s) != null) return false;
        }

        for (IMultipart p : getParts())
            if (!p.occlusionTest(part) || !part.occlusionTest(p)) return false;

        return true;
    }

    @Override
    public boolean canReplacePart(IMultipart oldPart, IMultipart newPart) {

        if (newPart instanceof ISlottedPart) {
            for (PartSlot s : ((ISlottedPart) newPart).getSlotMask()) {
                IMultipart p = getPartInSlot(s);
                if (p != null && p != oldPart) return false;
            }
        }

        for (IMultipart p : getParts())
            if (p != oldPart && (!p.occlusionTest(newPart) || !newPart.occlusionTest(p))) return false;

        return true;
    }

    @Override
    public void addPart(IMultipart part) {

        if (getWorld().isRemote) throw new IllegalStateException("Attempted to add a part on the client!");
        addPart(part, true, true, UUID.randomUUID());
    }

    public void addPart(IMultipart part, boolean notifyPart, boolean notifyNeighbors, UUID id) {

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

        if (notifyPart) part.onAdded();
        if (notifyNeighbors) notifyPartChanged(part);

        if (getWorld() != null && !getWorld().isRemote)
            MessageMultipartChange.newPacket(getWorld(), getPos(), part, Type.ADD).send(getWorld());
    }

    @Override
    public void removePart(IMultipart part) {

        removePart(part, true, true);
    }

    public void removePart(IMultipart part, boolean notifyPart, boolean notifyNeighbors) {

        if (getWorld() != null && !getWorld().isRemote)
            MessageMultipartChange.newPacket(getWorld(), getPos(), part, Type.REMOVE).send(getWorld());

        part.setContainer(null);

        BiMap<UUID, IMultipart> partMap = HashBiMap.create(this.partMap);
        Map<PartSlot, ISlottedPart> slotMap = new HashMap<PartSlot, ISlottedPart>(this.slotMap);

        partMap.inverse().remove(part);
        if (part instanceof ISlottedPart) {
            Iterator<Entry<PartSlot, ISlottedPart>> it = slotMap.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getValue() == part) it.remove();
            }
        }

        this.partMap = partMap;
        this.slotMap = slotMap;

        if (notifyPart) part.onRemoved();
        if (notifyNeighbors) notifyPartChanged(part);
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

        addPart(part, true, true, id);
    }

    public void notifyPartChanged(IMultipart part) {

        for (IMultipart p : getParts())
            if (p != part) p.onPartChanged(part);
        getWorld().notifyNeighborsOfStateChange(getPos(), getWorld().getBlockState(getPos()).getBlock());
    }

    public RayTraceResult collisionRayTrace(Vec3 start, Vec3 end) {

        double dist = Double.POSITIVE_INFINITY;
        RayTraceResult current = null;

        for (IMultipart p : getParts()) {
            RayTraceResult result = p.collisionRayTrace(start, end);
            if (result == null) continue;
            double d = result.squareDistanceTo(start);
            if (d <= dist) {
                dist = d;
                current = result;
            }
        }

        return current;
    }

    public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {

        List<AxisAlignedBB> collisionBoxes = new ArrayList<AxisAlignedBB>();
        AxisAlignedBB offsetMask = mask.offset(-getPos().getX(), -getPos().getY(), -getPos().getZ());
        for (IMultipart p : getParts())
            p.addCollisionBoxes(offsetMask, collisionBoxes, collidingEntity);
        Iterator<AxisAlignedBB> it = collisionBoxes.iterator();
        while (it.hasNext()) {
            list.add(it.next().offset(getPos().getX(), getPos().getY(), getPos().getZ()));
            it.remove();
        }
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

        if (getWorld().isRemote) return false;
        if (hit == null) return false;
        if (!partMap.values().contains(hit.partHit)) return false;
        if (getWorld().isRemote) return getParts().size() - 1 == 0;
        hit.partHit.harvest(player, hit);
        return getParts().isEmpty();
    }

    public float getHardness(EntityPlayer player, PartMOP hit) {

        if (!partMap.values().contains(hit.partHit)) return -1;
        return hit.partHit.getStrength(player, hit);
    }

    public void onNeighborBlockChange(Block block) {

        for (IMultipart part : getParts())
            part.onNeighborBlockChange(block);
    }

    public void onNeighborTileChange(EnumFacing facing) {

        for (IMultipart part : getParts())
            part.onNeighborTileChange(facing);
    }

    public boolean onActivated(EntityPlayer playerIn, ItemStack stack, PartMOP hit) {

        if (hit == null) return false;
        if (!partMap.values().contains(hit.partHit)) return false;
        return hit.partHit.onActivated(playerIn, stack, hit);
    }

    public void onClicked(EntityPlayer playerIn, ItemStack stack, PartMOP hit) {

        if (hit == null) return;
        if (!partMap.values().contains(hit.partHit)) return;
        hit.partHit.onClicked(playerIn, stack, hit);
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

    public void writeToNBT(NBTTagCompound tag) {

        NBTTagList partList = new NBTTagList();
        for (Entry<UUID, IMultipart> entry : partMap.entrySet()) {
            NBTTagCompound t = new NBTTagCompound();
            t.setString("__partID", entry.getKey().toString());
            t.setString("__partType", entry.getValue().getType());
            entry.getValue().writeToNBT(t);
            partList.appendTag(t);
        }
        tag.setTag("partList", partList);
    }

    public void readFromNBT(NBTTagCompound tag) {

        partMap.clear();
        slotMap.clear();

        NBTTagList partList = tag.getTagList("partList", new NBTTagCompound().getId());
        for (int i = 0; i < partList.tagCount(); i++) {
            NBTTagCompound t = partList.getCompoundTagAt(i);
            UUID id = UUID.fromString(t.getString("__partID"));
            IMultipart part = MultipartRegistry.createPart(t.getString("__partType"), t);
            part.readFromNBT(t);
            addPart(part, false, false, id);
        }
    }

    public void writeDescription(NBTTagCompound tag) {

        NBTTagList partList = new NBTTagList();
        for (Entry<UUID, IMultipart> entry : partMap.entrySet()) {
            NBTTagCompound t = new NBTTagCompound();
            t.setString("__partID", entry.getKey().toString());
            t.setString("__partType", entry.getValue().getType());
            ByteBuf buf = Unpooled.buffer();
            entry.getValue().writeUpdatePacket(new PacketBuffer(buf));
            t.setByteArray("data", buf.array());
            partList.appendTag(t);
        }
        tag.setTag("partList", partList);
    }

    public void readDescription(NBTTagCompound tag) {

        NBTTagList partList = tag.getTagList("partList", new NBTTagCompound().getId());
        for (int i = 0; i < partList.tagCount(); i++) {
            NBTTagCompound t = partList.getCompoundTagAt(i);
            UUID id = UUID.fromString(t.getString("__partID"));
            IMultipart part = partMap.get(id);
            if (part == null) {
                part = MultipartRegistry.createPart(t.getString("__partType"), Unpooled.copiedBuffer(t.getByteArray("data")));
                part.readUpdatePacket(new PacketBuffer(Unpooled.copiedBuffer(t.getByteArray("data"))));
            } else {
                part.readUpdatePacket(new PacketBuffer(Unpooled.copiedBuffer(t.getByteArray("data"))));
            }
            addPart(part, true, false, id);
        }
    }

}
