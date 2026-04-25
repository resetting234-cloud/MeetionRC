package meetion.rc.mixin;

import meetion.rc.MeetionRC;
import meetion.rc.core.event.Event;
import meetion.rc.core.event.EventBus;
import meetion.rc.core.event.events.MotionEvent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void meetion$preMotion(CallbackInfo ci) {
        if (MeetionRC.getInstance() == null) return;
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.player == null) return;
        EventBus.post(new MotionEvent(mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround(), Event.Era.PRE));
    }

    @Inject(method = "sendMovementPackets", at = @At("RETURN"))
    private void meetion$postMotion(CallbackInfo ci) {
        if (MeetionRC.getInstance() == null) return;
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.player == null) return;
        EventBus.post(new MotionEvent(mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround(), Event.Era.POST));
    }
}
