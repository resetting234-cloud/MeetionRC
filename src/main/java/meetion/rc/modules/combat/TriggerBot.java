package meetion.rc.modules.combat;
import meetion.rc.core.module.AutoModule;

import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.TickEvent;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.BooleanSetting;
import meetion.rc.core.setting.impl.NumberSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

@AutoModule
public class TriggerBot extends Module {

    private final NumberSetting cps = register(new NumberSetting("CPS", 10.0, 1.0, 20.0, 0.5));
    private final NumberSetting delay = register(new NumberSetting("StartDelay", 80.0, 0.0, 500.0, 5.0));
    private final BooleanSetting players = register(new BooleanSetting("Players", true));
    private final BooleanSetting mobs = register(new BooleanSetting("Mobs", false));

    private long lastAttack;
    private long lookSince;
    private LivingEntity lastTarget;

    public TriggerBot() {
        super("TriggerBot", "Attacks the entity you are looking at", Category.COMBAT);
    }

    @EventHandler
    public void onTick(TickEvent ev) {
        if (ev.getEra() != meetion.rc.core.event.Event.Era.PRE) return;
        if (mc().player == null || mc().crosshairTarget == null) return;
        if (mc().crosshairTarget.getType() != HitResult.Type.ENTITY) {
            lastTarget = null; lookSince = 0;
            return;
        }
        EntityHitResult hit = (EntityHitResult) mc().crosshairTarget;
        if (!(hit.getEntity() instanceof LivingEntity living)) { lastTarget = null; return; }
        if (living instanceof PlayerEntity ? !players.getValue() : !mobs.getValue()) return;

        if (living != lastTarget) {
            lookSince = System.currentTimeMillis();
            lastTarget = living;
        }
        if (System.currentTimeMillis() - lookSince < delay.getValue()) return;

        long now = System.currentTimeMillis();
        long d = (long) (1000.0 / cps.getValue());
        if (now - lastAttack < d) return;
        lastAttack = now;

        mc().getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(living, mc().player.isSneaking()));
        mc().player.swingHand(Hand.MAIN_HAND);
    }
}
