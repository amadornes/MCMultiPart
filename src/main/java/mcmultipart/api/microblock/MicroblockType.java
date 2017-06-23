package mcmultipart.api.microblock;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class MicroblockType extends IForgeRegistryEntry.Impl<MicroblockType> {

    public abstract String getLocalizedName(MicroMaterial material, int size);

    public abstract ItemStack createStack(MicroMaterial material, int size);

    public abstract List<ItemStack> createDrops(MicroMaterial material, int size);

    public abstract MicroMaterial getMaterial(ItemStack stack);

    public abstract int getSize(ItemStack stack);

    public abstract boolean place(World world, EntityPlayer player, ItemStack stack, RayTraceResult hit);

    @SideOnly(Side.CLIENT)
    public abstract void drawPlacement(IBlockAccess world, EntityPlayer player, ItemStack stack, RayTraceResult hit);

    public int getMinSize() {
        return 1;
    }

    public abstract int getMaxSize();

}
