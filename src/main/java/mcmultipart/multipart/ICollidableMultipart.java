package mcmultipart.multipart;

import net.minecraft.entity.Entity;

public interface ICollidableMultipart extends IMultipart {

    public void onEntityStanding(Entity entity);

    public void onEntityCollided(Entity entity);

}
