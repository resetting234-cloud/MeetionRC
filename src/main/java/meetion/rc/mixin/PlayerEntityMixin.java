package meetion.rc.mixin;

import meetion.rc.MeetionRC;
import meetion.rc.modules.movement.NoSlow;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "isUsingItem", at = @At("HEAD"), cancellable = true)
    private void meetion$noSlow(CallbackInfoReturnable<Boolean> cir) {
        if (MeetionRC.getInstance() == null) return;
        if (!((Object) this instanceof PlayerEntity player)) return;
        NoSlow noSlow = MeetionRC.getInstance().getModuleManager().get(NoSlow.class);
        if (noSlow == null || !noSlow.isEnabled()) return;
        if (noSlow.shouldCancelSlow(player)) {
            cir.setReturnValue(false);
        }
    }
}
