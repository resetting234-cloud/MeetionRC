package meetion.rc.mixin;

import meetion.rc.MeetionRC;
import meetion.rc.core.event.EventBus;
import meetion.rc.core.event.Event;
import meetion.rc.core.event.events.TickEvent;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void meetion$preTick(CallbackInfo ci) {
        if (MeetionRC.getInstance() == null) return;
        EventBus.post(new TickEvent(Event.Era.PRE));
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void meetion$postTick(CallbackInfo ci) {
        if (MeetionRC.getInstance() == null) return;
        EventBus.post(new TickEvent(Event.Era.POST));
    }
}
