package meetion.rc.mixin;

import meetion.rc.MeetionRC;
import meetion.rc.core.event.Event;
import meetion.rc.core.event.EventBus;
import meetion.rc.core.event.events.PacketEvent;
import meetion.rc.core.event.events.ChatSendEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void meetion$onChatSend(String message, CallbackInfo ci) {
        if (MeetionRC.getInstance() == null) return;
        ChatSendEvent ev = EventBus.post(new ChatSendEvent(message));
        if (ev.isCancelled()) ci.cancel();
    }

    @Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
    private void meetion$onCommandSend(String command, CallbackInfo ci) {
        if (MeetionRC.getInstance() == null) return;
        ChatSendEvent ev = EventBus.post(new ChatSendEvent("/" + command));
        if (ev.isCancelled()) ci.cancel();
    }
}
