package meetion.rc.modules.combat;
import meetion.rc.core.module.AutoModule;

import meetion.rc.MeetionRC;
import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.TickEvent;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.BooleanSetting;
import meetion.rc.core.setting.impl.NumberSetting;
import meetion.rc.util.rotation.RotationUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Comparator;

@AutoModule
public class ElytraTarget extends Module {

    private final NumberSetting range = register(new NumberSetting("Range", 80.0, 10.0, 200.0, 1.0));
    private final NumberSetting smooth = register(new NumberSetting("Smooth", 4.0, 0.1, 20.0, 0.1));
    private final BooleanSetting onlyFlying = register(new BooleanSetting("OnlyWhileGliding", true));
    private final BooleanSetting playersOnly = register(new BooleanSetting("PlayersOnly", true));
    private final BooleanSetting ignoreFriends = register(new BooleanSetting("IgnoreFriends", true));

    public ElytraTarget() {
        super("ElytraTarget", "Auto-tracks a target while gliding", Category.COMBAT);
    }

    @EventHandler
    public void onTick(TickEvent ev) {
        if (ev.getEra() != meetion.rc.core.event.Event.Era.PRE) return;
        if (mc().player == null || mc().world == null) return;
        if (onlyFlying.getValue() && !mc().player.isGliding()) return;

        LivingEntity target = findTarget();
        if (target == null) return;

        float[] rot = RotationUtil.toEntity(target, 0);
        float yaw = RotationUtil.smooth(mc().player.getYaw(), rot[0], smooth.asFloat());
        float pitch = RotationUtil.smooth(mc().player.getPitch(), rot[1], smooth.asFloat());
        mc().player.setYaw(yaw);
        mc().player.setPitch(pitch);
    }

    private LivingEntity findTarget() {
        var p = mc().player;
        double r = range.getValue();
        return java.util.stream.StreamSupport.stream(mc().world.getEntities().spliterator(), false)
                .filter(e -> e instanceof LivingEntity).map(e -> (LivingEntity) e)
                .filter(e -> e != p)
                .filter(LivingEntity::isAlive)
                .filter(e -> !playersOnly.getValue() || e instanceof PlayerEntity)
                .filter(e -> !ignoreFriends.getValue() || !MeetionRC.getInstance().getFriendManager().is(e))
                .filter(e -> p.distanceTo(e) <= r)
                .min(Comparator.comparingDouble(p::distanceTo))
                .orElse(null);
    }
}
