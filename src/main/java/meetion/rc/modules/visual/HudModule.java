package meetion.rc.modules.visual;

import meetion.rc.MeetionRC;
import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.Render2DEvent;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.BooleanSetting;
import meetion.rc.core.setting.impl.MultiSelectSetting;
import meetion.rc.screen.hud.ArrayListRenderer;
import meetion.rc.screen.hud.WatermarkRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class HudModule extends Module {

    private final MultiSelectSetting elements = register(new MultiSelectSetting(
            "Elements",
            List.of("Watermark", "ArrayList", "Notifications", "TargetHUD"),
            "Watermark", "ArrayList", "Notifications", "TargetHUD", "TPS", "FPS", "Coords"));
    private final BooleanSetting compact = register(new BooleanSetting("Compact", false));

    public HudModule() {
        super("HUD", "Configurable on-screen HUD elements", Category.VISUAL);
        setEnabled(true);
    }

    public boolean has(String element) { return elements.isSelected(element); }
    public boolean isCompact() { return compact.getValue(); }

    @EventHandler
    public void onRender(Render2DEvent ev) {
        DrawContext ctx = ev.getContext();
        if (has("Watermark")) WatermarkRenderer.render(ctx);
        if (has("ArrayList")) ArrayListRenderer.render(ctx);
    }
}
