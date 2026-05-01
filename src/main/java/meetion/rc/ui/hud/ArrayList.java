package meetion.rc.ui.hud;

import meetion.rc.MeetionRC;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.Setting;
import meetion.rc.core.setting.impl.ModeSetting;
import meetion.rc.ui.animation.Animator;
import meetion.rc.ui.animation.Easing;
import meetion.rc.ui.font.Fonts;
import meetion.rc.ui.utils.ColorUtil;
import meetion.rc.ui.utils.Palette;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Top-right ArrayList of currently enabled modules.
 *
 * <ul>
 *   <li>Sorted by rendered text width (longest at top) — re-sorted every 200ms, not
 *       every frame, to avoid recomputing for a list that rarely changes.</li>
 *   <li>Each entry has a per-module {@link Animator} that drives both vertical height
 *       and horizontal slide-in offset, so toggling a module gracefully expands /
 *       retracts the surrounding list rather than snapping.</li>
 *   <li>Mode tags: any module that exposes a {@link ModeSetting} renders its current
 *       mode in muted grey square brackets after the name (e.g. {@code KillAura [Switch]}).</li>
 *   <li>Right edge of every row gets a 2-pixel red accent strip flush against the
 *       screen boundary.</li>
 * </ul>
 */
public final class ArrayList {

    private ArrayList() {}

    private static final Identifier FONT = Fonts.MONO;
    private static final int ROW_HEIGHT  = Fonts.lineHeight() + 4;
    private static final int PAD_X       = 6;
    private static final int STRIP_WIDTH = 2;

    private static final Map<Module, Entry> entries = new LinkedHashMap<>();
    private static long lastSort = 0;
    private static java.util.List<Module> sortedCache = java.util.List.of();

    public static void render(DrawContext ctx) {
        if (MeetionRC.getInstance() == null) return;

        long now = System.currentTimeMillis();
        if (now - lastSort > 200) {
            sortedCache = computeSorted();
            lastSort = now;
        }

        // (1) sync entries with currently-enabled modules — fade out the rest
        Map<Module, Boolean> seen = new HashMap<>();
        for (Module m : sortedCache) {
            seen.put(m, true);
            Entry e = entries.computeIfAbsent(m, Entry::new);
            e.targetVisible(true);
        }
        for (Map.Entry<Module, Entry> en : entries.entrySet()) {
            if (!seen.containsKey(en.getKey())) en.getValue().targetVisible(false);
        }
        // remove fully-collapsed entries
        entries.entrySet().removeIf(en -> {
            Entry e = en.getValue();
            return !e.visible && e.slide.getValue() < 0.005 && e.height.getValue() < 0.5;
        });

        // (2) layout & draw, top to bottom in sortedCache order
        int screenW = ctx.getScaledWindowWidth();
        int y = 4;
        for (Module m : sortedCache) {
            Entry e = entries.get(m);
            if (e == null) continue;
            String label = labelFor(m);
            int textW = Fonts.width(FONT, label);
            int rowH  = (int) e.height.getValue();
            if (rowH < 1) continue;

            float slide = e.slide.floatValue();          // 0 = off-screen right, 1 = fully in
            int travel  = textW + PAD_X * 2 + STRIP_WIDTH + 6;
            int xRight  = screenW - (int) ((1 - slide) * travel);  // right edge animates in
            int xLeft   = xRight - textW - PAD_X * 2 - STRIP_WIDTH;

            // background row
            int alpha = (int) (180 * slide);
            ctx.fill(xLeft, y, xRight - STRIP_WIDTH, y + rowH, ColorUtil.argb(0, 0, 0, alpha));
            // 2-pixel red accent strip flush right
            ctx.fill(xRight - STRIP_WIDTH, y, xRight, y + rowH,
                    ColorUtil.withAlpha(Palette.RED, (int) (255 * slide)));

            // text — name in white, mode tag in muted grey
            int textY = y + (rowH - Fonts.lineHeight()) / 2;
            String name = m.getName();
            String mode = modeFor(m);
            int nameColor = ColorUtil.withAlpha(Palette.TEXT_PRIMARY, (int) (255 * slide));
            int tagColor  = ColorUtil.withAlpha(Palette.TEXT_MUTED,   (int) (255 * slide));

            Fonts.draw(ctx, FONT, name, xLeft + PAD_X, textY, nameColor);
            if (mode != null) {
                int nameW = Fonts.width(FONT, name);
                Fonts.draw(ctx, FONT, " " + mode, xLeft + PAD_X + nameW, textY, tagColor);
            }

            y += rowH;
        }
    }

    // ---------------------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------------------

    private static java.util.List<Module> computeSorted() {
        return MeetionRC.getInstance().getModuleManager().getModules().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt(ArrayList::widthOf).reversed())
                .toList();
    }

    private static int widthOf(Module m) {
        String mode = modeFor(m);
        String label = mode == null ? m.getName() : m.getName() + " " + mode;
        return Fonts.width(FONT, label);
    }

    private static String labelFor(Module m) {
        String mode = modeFor(m);
        return mode == null ? m.getName() : m.getName() + " " + mode;
    }

    private static String modeFor(Module m) {
        for (Setting<?> s : m.getSettings()) {
            if (s instanceof ModeSetting ms) {
                return "[" + ms.getValue() + "]";
            }
        }
        return null;
    }

    // ---------------------------------------------------------------------------
    // Entry
    // ---------------------------------------------------------------------------

    private static final class Entry {
        @SuppressWarnings("unused")
        final Module module;
        final Animator slide;
        final Animator height;
        boolean visible = false;

        Entry(Module module) {
            this.module = module;
            this.slide  = new Animator(0, 220, Easing.EASE_OUT_CUBIC);
            this.height = new Animator(0, 220, Easing.EASE_OUT_CUBIC);
        }

        void targetVisible(boolean v) {
            if (this.visible == v) return;
            this.visible = v;
            this.slide.setTarget(v ? 1 : 0);
            this.height.setTarget(v ? ROW_HEIGHT : 0);
        }
    }
}
