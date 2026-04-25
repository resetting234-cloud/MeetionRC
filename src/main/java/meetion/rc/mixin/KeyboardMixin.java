package meetion.rc.mixin;

import meetion.rc.MeetionRC;
import meetion.rc.core.event.EventBus;
import meetion.rc.core.event.events.KeyEvent;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void meetion$onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (MeetionRC.getInstance() == null) return;
        EventBus.post(new KeyEvent(key, action));
        if (action == 1) {
            MeetionRC.getInstance().getModuleManager().onKey(key, action);
        }
    }
}
