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
import net.minecraft.util.math.MathHelper;

import java.util.Comparator;

@AutoModule
public class AimAssist extends Module {

    private final NumberSetting range = register(new NumberSetting("Range", 4.0, 1.0, 8.0, 0.1));
    private final NumberSetting fov = register(new NumberSetting("FOV", 45.0, 5.0, 180.0, 1.0));
    private final NumberSetting yawSpeed = register(new NumberSetting("YawSpeed", 12.0, 1.0, 45.0, 0.5));
    private final NumberSetting pitchSpeed = register(new NumberSetting("PitchSpeed", 8.0, 1.0, 45.0, 0.5));
    private final BooleanSetting onlyWhileAttacking = register(new BooleanSetting("OnlyWhileAttacking", true));
    private final BooleanSetting playersOnly = register(new BooleanSetting("PlayersOnly", true));
    private final BooleanSetting ignoreFriends = register(new BooleanSetting("IgnoreFriends", true));

    public AimAssist() {
        super("AimAssist", "Smoothly snaps your aim toward nearby targets", Category.COMBAT);
    }

    @EventHandler
    public void onTick(TickEvent ev) {
        if (ev.getEra() != meetion.rc.core.event.Event.Era.PRE) return;
        if (mc().player == null || mc().world == null) return;
        if (onlyWhileAttacking.getValue() && (mc().options == null || !mc().options.attackKey.isPressed())) return;

        LivingEntity target = findTarget();
        if (target == null) return;

        float[] rot = RotationUtil.toEntity(target, 0);
        float yaw = RotationUtil.smooth(mc().player.getYaw(), rot[0], yawSpeed.asFloat());
        float pitch = RotationUtil.smooth(mc().player.getPitch(), rot[1], pitchSpeed.asFloat());
        mc().player.setYaw(yaw);
        mc().player.setPitch(pitch);
    }

    private LivingEntity findTarget() {
        var p = mc().player;
        double r = range.getValue();
        double maxFov = fov.getValue();
        return java.util.stream.StreamSupport.stream(mc().world.getEntities().spliterator(), false)
                .filter(e -> e instanceof LivingEntity).map(e -> (LivingEntity) e)
                .filter(e -> e != p)
                .filter(LivingEntity::isAlive)
                .filter(e -> !playersOnly.getValue() || e instanceof PlayerEntity)
                .filter(e -> !ignoreFriends.getValue() || !MeetionRC.getInstance().getFriendManager().is(e))
                .filter(e -> p.distanceTo(e) <= r)
                .filter(e -> {
                    float[] rot = RotationUtil.toEntity(e, 0);
                    float diff = Math.abs(MathHelper.wrapDegrees(rot[0] - p.getYaw()));
                    return diff <= maxFov / 2;
                })
                .min(Comparator.comparingDouble(p::distanceTo))
                .orElse(null);
    }
}
