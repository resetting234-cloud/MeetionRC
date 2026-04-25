package meetion.rc.mixin;

import meetion.rc.MeetionRC;
import meetion.rc.core.event.EventBus;
import meetion.rc.core.event.events.Render3DEvent;
import meetion.rc.modules.visual.AspectRatio;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void meetion$adjustFov(net.minecraft.client.render.Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        if (MeetionRC.getInstance() == null) return;
        AspectRatio ar = MeetionRC.getInstance().getModuleManager().get(AspectRatio.class);
        if (ar == null || !ar.isEnabled()) return;
        cir.setReturnValue(ar.adjust(cir.getReturnValueF()));
    }
}
