package mcmultipart.microblock;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Optional;

import mcmultipart.microblock.IMicroMaterial.IDelegatedMicroMaterial;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IRedstonePart;
import mcmultipart.multipart.IRedstonePart.ISlottedRedstonePart;
import mcmultipart.multipart.Multipart;
import mcmultipart.multipart.PartSlot;
import mcmultipart.property.PropertyBlockState;
import mcmultipart.property.PropertyMicroMaterial;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties.PropertyAdapter;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public abstract class Microblock extends Multipart implements IMicroblock, IRedstonePart, ISlottedRedstonePart {

    public static final IUnlistedProperty<?>[] PROPERTIES = new IUnlistedProperty[4];
    public static final IUnlistedProperty<IMicroMaterial> PROPERTY_MATERIAL;
    public static final IUnlistedProperty<IBlockState> PROPERTY_MATERIAL_STATE;
    public static final IUnlistedProperty<Integer> PROPERTY_SIZE;
    public static final IUnlistedProperty<PartSlot> PROPERTY_SLOT;

    static {
        PROPERTIES[0] = PROPERTY_MATERIAL = new PropertyMicroMaterial("material");
        PROPERTIES[1] = PROPERTY_MATERIAL_STATE = new PropertyBlockState("material_state");
        PROPERTIES[2] = PROPERTY_SIZE = new PropertyAdapter<Integer>(PropertyInteger.create("size", 0, 7));
        PROPERTIES[3] = PROPERTY_SLOT = new PropertyAdapter<PartSlot>(PropertyEnum.create("slot", PartSlot.class));
    }

    protected IMicroMaterial material;
    protected PartSlot slot;
    protected int size;
    protected MicroblockDelegate delegate;

    public Microblock(IMicroMaterial material, PartSlot slot, int size, boolean isRemote) {

        this.material = material;
        this.slot = slot;
        this.size = size;
        this.delegate = material instanceof IDelegatedMicroMaterial ? ((IDelegatedMicroMaterial) material).provideDelegate(this, isRemote)
                : null;
    }

    @Override
    public abstract MicroblockClass getMicroClass();

    @Override
    public IMicroMaterial getMicroMaterial() {

        return material;
    }

    @Override
    public EnumSet<PartSlot> getSlotMask() {

        return slot == null ? EnumSet.noneOf(PartSlot.class) : EnumSet.of(slot);
    }

    @Override
    public PartSlot getSlot() {

        return slot;
    }

    @Override
    public void setSlot(PartSlot slot) {

        this.slot = slot;
    }

    @Override
    public int getSize() {

        return size;
    }

    @Override
    public void setSize(int size) {

        this.size = size;
    }

    @Override
    public ResourceLocation getType() {

        return getMicroClass().getFullQualifiedType();
    }

    @Override
    public int getLightValue() {

        return getMicroMaterial().getLightValue();
    }

    @Override
    public float getHardness(PartMOP hit) {

        return getMicroMaterial().getHardness();
    }

    @Override
    public ItemStack getPickBlock(EntityPlayer player, PartMOP hit) {

        int size = getSize();
        int picked = 1;

        for (int i = 2; i >= 0; i--)
            if (size - (1 << i) >= 0) size -= (picked = (1 << i));

        return getMicroClass().createStack(getMicroMaterial(), picked, 1);
    }

    @Override
    public List<ItemStack> getDrops() {

        MicroblockClass microclass = getMicroClass();
        IMicroMaterial material = getMicroMaterial();
        int size = getSize();
        List<ItemStack> drops = new ArrayList<ItemStack>();

        for (int i = 2; i >= 0; i--) {
            if (size - (1 << i) >= 0) {
                size -= 1 << i;
                drops.add(microclass.createStack(material, 1 << i, 1));
            }
        }

        return drops;
    }

    @Override
    public boolean occlusionTest(IMultipart part) {

        if (part instanceof IMicroblock) return true;
        return super.occlusionTest(part);
    }

    @Override
    public IExtendedBlockState getExtendedState(IBlockState state) {

        return ((IExtendedBlockState) state).withProperty(PROPERTY_MATERIAL, getMicroMaterial())
                .withProperty(PROPERTY_MATERIAL_STATE, getMicroMaterial().getMaterialState(getWorld(), getPos(), this))
                .withProperty(PROPERTY_SIZE, getSize()).withProperty(PROPERTY_SLOT, slot);
    }

    @Override
    public ExtendedBlockState createBlockState() {

        return new ExtendedBlockState(null, new IProperty[0], PROPERTIES);
    }

    @Override
    public void writeUpdatePacket(PacketBuffer buf) {

        super.writeUpdatePacket(buf);

        ByteBufUtils.writeUTF8String(buf, getMicroMaterial().getName());
        buf.writeInt(slot != null ? slot.ordinal() : -1);
        buf.writeInt(getSize());
        if (delegate != null) delegate.writeUpdatePacket(buf);
    }

    @Override
    public void readUpdatePacket(PacketBuffer buf) {

        super.readUpdatePacket(buf);

        IMicroMaterial oldMat = material;
        material = MicroblockRegistry.getMaterial(ByteBufUtils.readUTF8String(buf));
        int iSlot = buf.readInt();
        slot = iSlot == -1 ? null : PartSlot.VALUES[iSlot];
        size = buf.readInt();
        if (oldMat != material) delegate = material instanceof IDelegatedMicroMaterial
                ? ((IDelegatedMicroMaterial) material).provideDelegate(this, true) : null;
        if (delegate != null) delegate.readUpdatePacket(buf);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {

        tag = super.writeToNBT(tag);

        tag.setString("material", getMicroMaterial().getName());
        tag.setInteger("slot", slot != null ? slot.ordinal() : -1);
        tag.setInteger("size", getSize());
        if (delegate != null) tag = delegate.writeToNBT(tag);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {

        super.readFromNBT(tag);

        material = MicroblockRegistry.getMaterial(tag.getString("material"));
        int iSlot = tag.getInteger("slot");
        slot = iSlot == -1 ? null : PartSlot.VALUES[iSlot];
        size = tag.getInteger("size");
        delegate = material instanceof IDelegatedMicroMaterial ? ((IDelegatedMicroMaterial) material).provideDelegate(this, false) : null;
        if (delegate != null) delegate.readFromNBT(tag);
    }

    // Delegation

    @Override
    public void harvest(EntityPlayer player, PartMOP hit) {

        if (delegate == null || !delegate.harvest(player, hit)) super.harvest(player, hit);
    }

    @Override
    public float getStrength(EntityPlayer player, PartMOP hit) {

        if (delegate != null) {
            Optional<Float> strength = delegate.getStrength(player, hit);
            if (strength.isPresent()) return strength.get();
        }
        return super.getStrength(player, hit);
    }

    @Override
    public void onPartChanged(IMultipart part) {

        super.onPartChanged(part);
        if (delegate != null) delegate.onPartChanged(part);
    }

    @Override
    public void onNeighborBlockChange(Block block) {

        super.onNeighborBlockChange(block);
        if (delegate != null) delegate.onNeighborBlockChange(block);
    }

    @Override
    public void onNeighborTileChange(EnumFacing facing) {

        super.onNeighborTileChange(facing);
        if (delegate != null) delegate.onNeighborTileChange(facing);
    }

    @Override
    public void onAdded() {

        super.onAdded();
        if (delegate != null) delegate.onAdded();
    }

    @Override
    public void onRemoved() {

        super.onRemoved();
        if (delegate != null) delegate.onRemoved();
    }

    @Override
    public void onLoaded() {

        super.onLoaded();
        if (delegate != null) delegate.onLoaded();
    }

    @Override
    public void onUnloaded() {

        super.onUnloaded();
        if (delegate != null) delegate.onUnloaded();
    }

    @Override
    public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack heldItem, PartMOP hit) {

        if (delegate != null) {
            Optional<Boolean> activated = delegate.onActivated(player, hand, heldItem, hit);
            if (activated.isPresent()) return activated.get();
        }
        return super.onActivated(player, hand, heldItem, hit);
    }

    @Override
    public void onClicked(EntityPlayer player, PartMOP hit) {

        if (delegate != null && delegate.onClicked(player, hit)) return;
        super.onClicked(player, hit);
    }

    @Override
    public boolean canConnectRedstone(EnumFacing side) {

        if (delegate != null) {
            Optional<Boolean> can = delegate.canConnectRedstone(side);
            if (can.isPresent()) return can.get();
        }
        return false;
    }

    @Override
    public int getWeakSignal(EnumFacing side) {

        if (delegate != null) {
            Optional<Integer> signal = delegate.getWeakSignal(side);
            if (signal.isPresent()) return signal.get();
        }
        return 0;
    }

    @Override
    public int getStrongSignal(EnumFacing side) {

        if (delegate != null) {
            Optional<Integer> signal = delegate.getStrongSignal(side);
            if (signal.isPresent()) return signal.get();
        }
        return 0;
    }

    public static class PropertyAABB implements IUnlistedProperty<AxisAlignedBB> {

        private final String name;

        public PropertyAABB(String name) {

            this.name = name;
        }

        @Override
        public String getName() {

            return name;
        }

        @Override
        public boolean isValid(AxisAlignedBB value) {

            return value != null;
        }

        @Override
        public Class<AxisAlignedBB> getType() {

            return AxisAlignedBB.class;
        }

        @Override
        public String valueToString(AxisAlignedBB value) {

            return value.toString();
        }

    }

    public static class PropertyEnumSet<T extends Enum<T>> implements IUnlistedProperty<EnumSet<T>> {

        private final String name;

        public PropertyEnumSet(String name) {

            this.name = name;
        }

        @Override
        public String getName() {

            return name;
        }

        @Override
        public boolean isValid(EnumSet<T> value) {

            return value != null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<EnumSet<T>> getType() {

            return (Class<EnumSet<T>>) (Class<?>) EnumSet.class;
        }

        @Override
        public String valueToString(EnumSet<T> value) {

            return value.toString();
        }

    }

}
