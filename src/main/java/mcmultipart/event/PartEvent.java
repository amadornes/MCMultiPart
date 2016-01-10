package mcmultipart.event;

import mcmultipart.multipart.IMultipart;
import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class PartEvent extends Event {

    public final IMultipart part;

    public PartEvent(IMultipart part) {

        this.part = part;
    }

    public static class Add extends PartEvent {

        public Add(IMultipart part) {

            super(part);
        }

    }

    public static class Remove extends PartEvent {

        public Remove(IMultipart part) {

            super(part);
        }

    }

}
