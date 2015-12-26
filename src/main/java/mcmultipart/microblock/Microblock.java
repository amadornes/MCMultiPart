package mcmultipart.microblock;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import mcmultipart.multipart.Multipart;
import mcmultipart.multipart.PartSlot;
import mcmultipart.property.PropertyMicroMaterial;
import mcmultipart.property.PropertySlot;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public abstract class Microblock extends Multipart implements IMicroblock {

    public static final IProperty<?>[] PROPERTIES = new IProperty[3];
    public static final IProperty<IMicroMaterial> PROPERTY_MATERIAL;
    public static final IProperty<Integer> PROPERTY_SIZE;
    public static final IProperty<PartSlot> PROPERTY_SLOT;

    static {
        PROPERTIES[0] = PROPERTY_MATERIAL = new PropertyMicroMaterial("material");
        PROPERTIES[1] = PROPERTY_SIZE = PropertyInteger.create("size", 0, 7);
        PROPERTIES[2] = PROPERTY_SLOT = new PropertySlot("slot");
    }

    protected IMicroMaterial material;
    protected PartSlot slot;
    protected int size;

    public Microblock(IMicroMaterial material, PartSlot slot, int size) {

        this.material = material;
        this.slot = slot;
        this.size = size;
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
    public String getType() {

        return getMicroClass().getType();
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
    public IBlockState getExtendedState(IBlockState state) {

        return state.withProperty(PROPERTY_MATERIAL, getMicroMaterial()).withProperty(PROPERTY_SIZE, getSize())
                .withProperty(PROPERTY_SLOT, slot);
    }

    @Override
    public BlockState createBlockState() {

        return new BlockState(null, PROPERTIES);
    }

    @Override
    public void writeUpdatePacket(PacketBuffer buf) {

        super.writeUpdatePacket(buf);

        ByteBufUtils.writeUTF8String(buf, getMicroMaterial().getName());
        buf.writeInt(slot != null ? slot.ordinal() : -1);
        buf.writeInt(getSize());
    }

    @Override
    public void readUpdatePacket(PacketBuffer buf) {

        super.readUpdatePacket(buf);

        material = MicroblockRegistry.getMaterial(ByteBufUtils.readUTF8String(buf));
        int iSlot = buf.readInt();
        slot = iSlot == -1 ? null : PartSlot.VALUES[iSlot];
        size = buf.readInt();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {

        super.writeToNBT(tag);

        tag.setString("material", getMicroMaterial().getName());
        tag.setInteger("slot", slot != null ? slot.ordinal() : -1);
        tag.setInteger("size", getSize());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {

        super.readFromNBT(tag);

        material = MicroblockRegistry.getMaterial(tag.getString("material"));
        int iSlot = tag.getInteger("slot");
        slot = iSlot == -1 ? null : PartSlot.VALUES[iSlot];
        size = tag.getInteger("size");
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
