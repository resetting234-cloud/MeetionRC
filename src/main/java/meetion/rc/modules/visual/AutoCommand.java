package meetion.rc.modules.visual;

import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.TickEvent;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.NumberSetting;

import java.util.ArrayList;
import java.util.List;

public class AutoCommand extends Module {

    private final NumberSetting interval = register(new NumberSetting("IntervalSec", 30.0, 1.0, 600.0, 1.0));

    private long nextRun;
    private final List<String> commands = new ArrayList<>();

    public AutoCommand() {
        super("AutoCommand", "Runs configured commands on a timer", Category.VISUAL);
    }

    public List<String> commands() { return commands; }

    @Override
    protected void onEnable() { nextRun = System.currentTimeMillis() + (long) (interval.getValue() * 1000.0); }

    @EventHandler
    public void onTick(TickEvent ev) {
        if (ev.getEra() != meetion.rc.core.event.Event.Era.PRE) return;
        if (mc().player == null || mc().getNetworkHandler() == null) return;
        if (System.currentTimeMillis() < nextRun) return;
        if (commands.isEmpty()) { nextRun = System.currentTimeMillis() + (long) (interval.getValue() * 1000.0); return; }

        for (String cmd : commands) {
            String c = cmd.startsWith("/") ? cmd.substring(1) : cmd;
            mc().getNetworkHandler().sendChatCommand(c);
        }
        nextRun = System.currentTimeMillis() + (long) (interval.getValue() * 1000.0);
    }
}
