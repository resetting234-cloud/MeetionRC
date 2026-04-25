package meetion.rc.modules.movement;

import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.TickEvent;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.BooleanSetting;
import meetion.rc.core.setting.impl.NumberSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class NoWeb extends Module {

    private final NumberSetting boost = register(new NumberSetting("Boost", 7.0, 1.0, 25.0, 0.5));
    private final BooleanSetting onlyHorizontal = register(new BooleanSetting("OnlyHorizontal", true));

    public NoWeb() {
        super("NoWeb", "Speeds you up when stuck in cobwebs", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(TickEvent ev) {
        if (ev.getEra() != meetion.rc.core.event.Event.Era.PRE) return;
        if (mc().player == null || mc().world == null) return;
        if (!isInWeb()) return;

        Vec3d v = mc().player.getVelocity();
        double m = boost.getValue();
        if (onlyHorizontal.getValue()) {
            mc().player.setVelocity(v.x * m, v.y, v.z * m);
        } else {
            mc().player.setVelocity(v.multiply(m));
        }
    }

    private boolean isInWeb() {
        Box box = mc().player.getBoundingBox().contract(0.001);
        for (BlockPos pos : BlockPos.iterate(BlockPos.ofFloored(box.minX, box.minY, box.minZ),
                BlockPos.ofFloored(box.maxX, box.maxY, box.maxZ))) {
            if (mc().world.getBlockState(pos).isOf(Blocks.COBWEB)) return true;
        }
        return false;
    }
}
