package meetion.rc.modules.combat;

import meetion.rc.MeetionRC;
import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.MotionEvent;
import meetion.rc.core.event.events.TickEvent;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.*;
import meetion.rc.util.bypass.Anticheat;
import meetion.rc.util.rotation.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

public class KillAura extends Module {

    private final ModeSetting mode = register(new ModeSetting("Mode", "Single", "Single", "Switch", "Multi"));
    private final NumberSetting range = register(new NumberSetting("Range", 3.0, 1.5, 6.0, 0.05));
    private final NumberSetting cps = register(new NumberSetting("CPS", 12.0, 1.0, 20.0, 0.5));
    private final NumberSetting fov = register(new NumberSetting("FOV", 180.0, 30.0, 360.0, 5.0));
    private final BooleanSetting throughWalls = register(new BooleanSetting("ThroughWalls", false));
    private final BooleanSetting players = register(new BooleanSetting("Players", true));
    private final BooleanSetting mobs = register(new BooleanSetting("Mobs", false));
    private final BooleanSetting passives = register(new BooleanSetting("Passives", false));
    private final BooleanSetting friends = register(new BooleanSetting("IgnoreFriends", true));
    private final BooleanSetting silent = register(new BooleanSetting("Silent", true));
    private final ModeSetting bypass = register(new ModeSetting("Bypass",
            Anticheat.GRIM,
            Anticheat.GRIM, Anticheat.VERUS, Anticheat.VULCAN, Anticheat.MATRIX,
            Anticheat.THEMIS, Anticheat.SPARTAN, Anticheat.NEGATIVITY, Anticheat.OTHER));
    private final MultiSelectSetting bypassExtra = register(new MultiSelectSetting(
            "BypassExtra", java.util.List.of(),
            "RandomGCD", "Smooth", "TickDelay", "PreMotion", "PostMotion", "MouseFix"));

    private long lastAttack;
    private LivingEntity target;
    private float[] rotations;

    public KillAura() {
        super("KillAura", "Attacks nearby entities", Category.COMBAT);
    }

    @EventHandler
    public void onTick(TickEvent ev) {
        if (ev.getEra() != meetion.rc.core.event.Event.Era.PRE) return;
        if (mc().player == null || mc().world == null) return;

        target = findTarget();
        if (target == null) { rotations = null; return; }

        rotations = computeRotations(target);

        if (canAttack()) attack(target);
    }

    @EventHandler
    public void onMotion(MotionEvent ev) {
        if (target == null || rotations == null) return;
        if (!silent.getValue()) return;
        if (ev.getEra() == meetion.rc.core.event.Event.Era.PRE
                && (bypassExtra.isSelected("PreMotion") || isPreMotionBypass())) {
            ev.setYaw(rotations[0]);
            ev.setPitch(rotations[1]);
        }
    }

    private boolean isPreMotionBypass() {
        return switch (bypass.getValue()) {
            case Anticheat.GRIM, Anticheat.VULCAN, Anticheat.MATRIX -> true;
            default -> false;
        };
    }

    private LivingEntity findTarget() {
        var player = mc().player;
        double r = range.getValue();
        double maxFov = fov.getValue();
        var stream = java.util.stream.StreamSupport.stream(mc().world.getEntities().spliterator(), false)
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .filter(e -> e != player)
                .filter(LivingEntity::isAlive)
                .filter(e -> filterType(e))
                .filter(e -> player.distanceTo(e) <= r)
                .filter(e -> withinFov(e, maxFov))
                .filter(e -> throughWalls.getValue() || mc().player.canSee(e));

        return stream.min(Comparator.comparingDouble(e -> player.distanceTo(e))).orElse(null);
    }

    private boolean filterType(LivingEntity e) {
        if (friends.getValue() && MeetionRC.getInstance().getFriendManager().is(e)) return false;
        if (e instanceof PlayerEntity p) {
            if (p == mc().player) return false;
            return players.getValue();
        }
        if (e instanceof net.minecraft.entity.mob.HostileEntity) return mobs.getValue();
        if (e instanceof net.minecraft.entity.passive.PassiveEntity) return passives.getValue();
        return mobs.getValue();
    }

    private boolean withinFov(Entity e, double maxFov) {
        if (maxFov >= 360) return true;
        float[] r = RotationUtil.toEntity(e, 0);
        float diff = Math.abs(net.minecraft.util.math.MathHelper.wrapDegrees(r[0] - mc().player.getYaw()));
        return diff <= maxFov / 2;
    }

    private float[] computeRotations(Entity target) {
        float[] base = RotationUtil.toEntity(target, 0);
        float speed = switch (bypass.getValue()) {
            case Anticheat.GRIM -> 35f;
            case Anticheat.VULCAN -> 28f;
            case Anticheat.MATRIX -> 40f;
            case Anticheat.VERUS -> 22f;
            case Anticheat.THEMIS -> 55f;
            case Anticheat.SPARTAN -> 65f;
            case Anticheat.NEGATIVITY -> 70f;
            default -> 180f;
        };
        float yaw = mc().player.getYaw();
        float pitch = mc().player.getPitch();
        if (bypassExtra.isSelected("Smooth")) {
            yaw = RotationUtil.smooth(yaw, base[0], speed);
            pitch = RotationUtil.smooth(pitch, base[1], speed);
        } else {
            yaw = base[0];
            pitch = base[1];
        }
        if (bypassExtra.isSelected("RandomGCD")) {
            float gcd = RotationUtil.currentGcd();
            yaw = RotationUtil.gcdAdjust(yaw, gcd);
            pitch = RotationUtil.gcdAdjust(pitch, gcd);
        }
        return new float[]{ yaw, pitch };
    }

    private boolean canAttack() {
        long now = System.currentTimeMillis();
        long delay = (long) (1000.0 / cps.getValue());
        return now - lastAttack >= delay;
    }

    private void attack(LivingEntity entity) {
        var net = mc().getNetworkHandler();
        if (net == null) return;
        var packet = PlayerInteractEntityC2SPacket.attack(entity, mc().player.isSneaking());
        net.sendPacket(packet);
        mc().player.swingHand(Hand.MAIN_HAND);
        lastAttack = System.currentTimeMillis();
    }

    public LivingEntity getTarget() { return target; }
}
