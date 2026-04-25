package meetion.rc.mixin;

import meetion.rc.MeetionRC;
import meetion.rc.modules.combat.Velocity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerVelocityMixin {

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
    private void meetion$velocity(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        if (MeetionRC.getInstance() == null) return;
        Velocity vel = MeetionRC.getInstance().getModuleManager().get(Velocity.class);
        if (vel == null || !vel.isEnabled()) return;
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.player == null) return;

        EntityVelocityUpdateMixin acc = (EntityVelocityUpdateMixin) packet;
        if (acc.meetion$getEntityId() != mc.player.getId()) return;

        double hMul = vel.horizontalMultiplier();
        double vMul = vel.verticalMultiplier();
        if (hMul == 0 && vMul == 0) {
            ci.cancel();
            return;
        }
        acc.meetion$setVelocityX((int) (packet.getVelocityX() * hMul));
        acc.meetion$setVelocityY((int) (packet.getVelocityY() * vMul));
        acc.meetion$setVelocityZ((int) (packet.getVelocityZ() * hMul));
    }
}
