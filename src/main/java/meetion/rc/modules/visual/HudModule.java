package meetion.rc.modules.visual;
import meetion.rc.core.module.AutoModule;

import meetion.rc.MeetionRC;
import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.Render2DEvent;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.BooleanSetting;
import meetion.rc.core.setting.impl.MultiSelectSetting;
import meetion.rc.ui.hud.ArrayList;
import meetion.rc.ui.hud.NotificationManager;
import meetion.rc.ui.hud.TargetHud;
import meetion.rc.ui.hud.Watermark;
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

    private static boolean firstRenderLogged = false;

    @EventHandler
    public void onRender(Render2DEvent ev) {
        DrawContext ctx = ev.getContext();
        if (!firstRenderLogged) {
            firstRenderLogged = true;
            System.out.println("[METTRC] HUD render pipeline online — Watermark/ArrayList/TargetHUD/Notifications");
        }
        if (has("Watermark"))     safe("Watermark",     () -> Watermark.render(ctx));
        if (has("ArrayList"))     safe("ArrayList",     () -> ArrayList.render(ctx));
        if (has("TargetHUD"))     safe("TargetHUD",     () -> TargetHud.render(ctx));
        if (has("Notifications")) safe("Notifications", () -> NotificationManager.render(ctx));
    }

    /** One bad renderer must not blow out the whole HUD; log first failure per element. */
    private static final java.util.Set<String> reportedFailures = new java.util.HashSet<>();
    private static void safe(String name, Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            if (reportedFailures.add(name)) {
                System.err.println("[METTRC] HUD element '" + name + "' threw " + t.getClass().getSimpleName()
                        + ": " + t.getMessage() + " — element disabled until next reload.");
                t.printStackTrace();
            }
        }
    }
}
