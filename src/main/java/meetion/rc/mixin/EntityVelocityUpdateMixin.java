package meetion.rc.mixin;

import meetion.rc.MeetionRC;
import meetion.rc.modules.combat.Velocity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public interface EntityVelocityUpdateMixin {

    @Accessor("velocityX")
    @Mutable
    void meetion$setVelocityX(int x);

    @Accessor("velocityY")
    @Mutable
    void meetion$setVelocityY(int y);

    @Accessor("velocityZ")
    @Mutable
    void meetion$setVelocityZ(int z);

    @Accessor("entityId")
    int meetion$getEntityId();
}
