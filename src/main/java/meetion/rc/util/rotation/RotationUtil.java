package meetion.rc.util.rotation;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class RotationUtil {

    private RotationUtil() {}

    public static float[] calc(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        return new float[]{ MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90f, 90f) };
    }

    public static float[] toEntity(Entity target, double yOffset) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return new float[]{0f, 0f};
        Vec3d eye = mc.player.getEyePos();
        Vec3d to = target.getPos().add(0, target.getHeight() / 2.0 + yOffset, 0);
        return calc(eye, to);
    }

    public static float smooth(float current, float target, float speed) {
        float delta = MathHelper.wrapDegrees(target - current);
        float clamped = MathHelper.clamp(delta, -speed, speed);
        return MathHelper.wrapDegrees(current + clamped);
    }

    public static float gcdAdjust(float angle, float gcdSensitivity) {
        return Math.round(angle / gcdSensitivity) * gcdSensitivity;
    }

    public static float currentGcd() {
        MinecraftClient mc = MinecraftClient.getInstance();
        float sens = mc.options.getMouseSensitivity().getValue().floatValue() * 0.6f + 0.2f;
        return sens * sens * sens * 8f * 0.15f;
    }
}
