package mcmultipart.microblock;

import mcmultipart.multipart.IMultipart;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;

public class MicroblockDelegate {

    protected final IMicroblock delegated;

    public MicroblockDelegate(IMicroblock delegated) {

        this.delegated = delegated;
    }

    public Integer getLightValue() {

        return null;
    }

    public boolean harvest(EntityPlayer player, PartMOP hit) {

        return false;
    }

    public Float getStrength(EntityPlayer player, PartMOP hit) {

        return null;
    }

    public void onPartChanged(IMultipart part) {

    }

    public void onNeighborBlockChange(Block block) {

    }

    public void onNeighborTileChange(EnumFacing facing) {

    }

    public void onAdded() {

    }

    public void onRemoved() {

    }

    public void onLoaded() {

    }

    public void onUnloaded() {

    }

    public Boolean onActivated(EntityPlayer player, ItemStack stack, PartMOP hit) {

        return null;
    }

    public void onClicked(EntityPlayer player, ItemStack stack, PartMOP hit) {

    }

    public void writeToNBT(NBTTagCompound tag) {

    }

    public void readFromNBT(NBTTagCompound tag) {

    }

    public void writeUpdatePacket(PacketBuffer buf) {

    }

    public void readUpdatePacket(PacketBuffer buf) {

    }

    public void sendUpdatePacket() {

        delegated.sendUpdatePacket();
    }

}
