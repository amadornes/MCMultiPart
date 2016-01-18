package mcmultipart.capabilities;

import java.util.ArrayList;
import java.util.List;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.IRedstonePart;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.PartSlot;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

/**
 * A general use multipart capability helper.
 */
public class MultipartCapabilityHelper {

    public static boolean hasCapability(IMultipartContainer container, Capability<?> capability, EnumFacing side) {

        for (EnumFacing face : EnumFacing.VALUES)
            if (face != side && face != side.getOpposite() && hasCapability(container, capability, side, face)) return true;
        return false;
    }

    public static <T> T getCapability(IMultipartContainer container, Capability<T> capability, EnumFacing side) {

        List<T> implementations = new ArrayList<T>();
        for (EnumFacing face : EnumFacing.VALUES) {
            if (face != side && face != side.getOpposite()) {
                T impl = getCapability(container, capability, side, face);
                if (impl != null) implementations.add(impl);
            }
        }
        if (implementations.isEmpty()) return null;
        else if (implementations.size() == 1) return implementations.get(0);
        else return CapabilityWrapperRegistry.wrap(capability, implementations);
    }

    public static boolean hasCapability(IMultipartContainer container, Capability<?> capability, EnumFacing side, EnumFacing face) {

        if (container == null) return false;

        PartSlot slot;
        IMultipart part = container.getPartInSlot(slot = PartSlot.getFaceSlot(side));
        if (part != null)
            return part instanceof ISlottedCapabilityProvider ? ((ISlottedCapabilityProvider) part).hasCapability(capability, slot, side)
                    : part instanceof ICapabilityProvider ? ((ICapabilityProvider) part).hasCapability(capability, side) : null;
        part = container.getPartInSlot(slot = PartSlot.getEdgeSlot(side, face));
        if (part != null)
            return part instanceof ISlottedCapabilityProvider ? ((ISlottedCapabilityProvider) part).hasCapability(capability, slot, side)
                    : part instanceof ICapabilityProvider ? ((ICapabilityProvider) part).hasCapability(capability, side) : null;
        part = container.getPartInSlot(slot = PartSlot.getFaceSlot(face));
        if (part != null)
            return part instanceof ISlottedCapabilityProvider ? ((ISlottedCapabilityProvider) part).hasCapability(capability, slot, side)
                    : part instanceof ICapabilityProvider ? ((ICapabilityProvider) part).hasCapability(capability, side) : null;

        if (face == null) {
            part = container.getPartInSlot(PartSlot.CENTER);
            if (part != null && (part instanceof ISlottedCapabilityProvider || part instanceof ICapabilityProvider))
                return part instanceof ISlottedCapabilityProvider ? ((ISlottedCapabilityProvider) part).hasCapability(capability, slot,
                        side) : part instanceof ICapabilityProvider ? ((ICapabilityProvider) part).hasCapability(capability, side) : null;
        }

        for (IMultipart p : container.getParts())
            if (!(p instanceof ISlottedPart) || ((ISlottedPart) p).getSlotMask().isEmpty())
                if (p instanceof ICapabilityProvider) return ((ICapabilityProvider) part).hasCapability(capability, side);

        return false;
    }

    public static <T> T getCapability(IMultipartContainer container, Capability<T> capability, EnumFacing side, EnumFacing face) {

        if (container == null) return null;

        PartSlot slot;
        IMultipart part = container.getPartInSlot(slot = PartSlot.getFaceSlot(side));
        if (part != null)
            return part instanceof ISlottedCapabilityProvider ? ((ISlottedCapabilityProvider) part).getCapability(capability, slot, side)
                    : part instanceof ICapabilityProvider ? ((ICapabilityProvider) part).getCapability(capability, side) : null;
        part = container.getPartInSlot(slot = PartSlot.getEdgeSlot(side, face));
        if (part != null)
            return part instanceof ISlottedCapabilityProvider ? ((ISlottedCapabilityProvider) part).getCapability(capability, slot, side)
                    : part instanceof ICapabilityProvider ? ((ICapabilityProvider) part).getCapability(capability, side) : null;
        part = container.getPartInSlot(slot = PartSlot.getFaceSlot(face));
        if (part != null)
            return part instanceof ISlottedCapabilityProvider ? ((ISlottedCapabilityProvider) part).getCapability(capability, slot, side)
                    : part instanceof ICapabilityProvider ? ((ICapabilityProvider) part).getCapability(capability, side) : null;

        if (face == null) {
            part = container.getPartInSlot(PartSlot.CENTER);
            if (part instanceof IRedstonePart)
                return part instanceof ISlottedCapabilityProvider ? ((ISlottedCapabilityProvider) part).getCapability(capability, slot,
                        side) : part instanceof ICapabilityProvider ? ((ICapabilityProvider) part).getCapability(capability, side) : null;
        }

        List<T> implementations = new ArrayList<T>();
        for (IMultipart p : container.getParts()) {
            if (!(p instanceof ISlottedPart) || ((ISlottedPart) p).getSlotMask().isEmpty()) {
                if (p instanceof ICapabilityProvider) {
                    T impl = ((ICapabilityProvider) part).getCapability(capability, side);
                    if (impl != null) implementations.add(impl);
                }
            }
        }

        if (implementations.isEmpty()) return null;
        else if (implementations.size() == 1) return implementations.get(0);
        else return CapabilityWrapperRegistry.wrap(capability, implementations);
    }

}
