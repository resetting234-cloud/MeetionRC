package meetion.rc.modules.visual;
import meetion.rc.core.module.AutoModule;

import meetion.rc.MeetionRC;
import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.Render2DEvent;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.BooleanSetting;
import meetion.rc.core.setting.impl.MultiSelectSetting;
import meetion.rc.screen.hud.ArrayListRenderer;
import meetion.rc.screen.hud.Notifications;
import meetion.rc.screen.hud.TargetHudRenderer;
import meetion.rc.screen.hud.WatermarkRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

@AutoModule(enabledByDefault = true)
public class HudModule extends Module {

    private final MultiSelectSetting elements = register(new MultiSelectSetting(
            "Elements",
            List.of("Watermark", "ArrayList", "Notifications", "TargetHUD"),
            "Watermark", "ArrayList", "Notifications", "TargetHUD", "TPS", "FPS", "Coords"));
    private final BooleanSetting compact  = register(new BooleanSetting("Compact", false));
    private final BooleanSetting realBlur = register(new BooleanSetting("Real Blur", false));
    {
        realBlur.describe("Use Mojang's native ClickGUI blur. Heavier on integrated GPUs.");
    }

    public HudModule() {
        super("HUD", "Configurable on-screen HUD elements", Category.VISUAL);
    }

    public boolean has(String element) { return elements.isSelected(element); }
    public boolean isCompact() { return compact.getValue(); }
    public boolean isRealBlurEnabled() { return realBlur.getValue(); }

    @EventHandler
    public void onRender(Render2DEvent ev) {
        DrawContext ctx = ev.getContext();
        if (has("Watermark")) WatermarkRenderer.render(ctx);
        if (has("ArrayList")) ArrayListRenderer.render(ctx);
        if (has("TargetHUD")) TargetHudRenderer.render(ctx);
        if (has("Notifications")) Notifications.render(ctx);
    }
}
