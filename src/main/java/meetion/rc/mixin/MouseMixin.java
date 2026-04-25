package meetion.rc.mixin;

import meetion.rc.MeetionRC;
import meetion.rc.core.event.EventBus;
import meetion.rc.core.event.events.KeyEvent;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void meetion$onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (MeetionRC.getInstance() == null) return;
        EventBus.post(new KeyEvent(-100 - button, action));
        if (action == 1) {
            MeetionRC.getInstance().getModuleManager().onKey(-100 - button, action);
        }
    }
}
