package meetion.rc.modules.combat;
import meetion.rc.core.module.AutoModule;

import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.TickEvent;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.BooleanSetting;
import meetion.rc.core.setting.impl.NumberSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@AutoModule
public class AutoClicker extends Module {

    private final NumberSetting min = register(new NumberSetting("MinCPS", 8.0, 1.0, 20.0, 0.5));
    private final NumberSetting max = register(new NumberSetting("MaxCPS", 13.0, 1.0, 20.0, 0.5));
    private final BooleanSetting onlyEntities = register(new BooleanSetting("OnlyEntities", true));
    private final BooleanSetting requireMouseDown = register(new BooleanSetting("RequireMouseDown", true));

    private long nextClick;

    public AutoClicker() {
        super("AutoClicker", "Auto attack with humanized CPS", Category.COMBAT);
    }

    @EventHandler
    public void onTick(TickEvent ev) {
        if (ev.getEra() != meetion.rc.core.event.Event.Era.PRE) return;
        if (mc().player == null || mc().options == null) return;
        if (requireMouseDown.getValue() && !mc().options.attackKey.isPressed()) return;

        if (onlyEntities.getValue()) {
            if (mc().crosshairTarget == null || mc().crosshairTarget.getType() != HitResult.Type.ENTITY) return;
            if (!(((EntityHitResult) mc().crosshairTarget).getEntity() instanceof LivingEntity)) return;
        }

        long now = System.currentTimeMillis();
        if (now < nextClick) return;

        if (mc().crosshairTarget instanceof EntityHitResult ehr) {
            mc().getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(ehr.getEntity(), mc().player.isSneaking()));
        }
        mc().player.swingHand(Hand.MAIN_HAND);

        double cps = ThreadLocalRandom.current().nextDouble(Math.min(min.getValue(), max.getValue()), Math.max(min.getValue(), max.getValue()) + 0.001);
        nextClick = now + (long) (1000.0 / cps);
    }
}
