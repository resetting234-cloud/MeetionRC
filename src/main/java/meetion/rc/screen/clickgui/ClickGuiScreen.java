package meetion.rc.screen.clickgui;

import meetion.rc.MeetionRC;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.Setting;
import meetion.rc.core.setting.impl.BooleanSetting;
import meetion.rc.core.setting.impl.ModeSetting;
import meetion.rc.core.setting.impl.NumberSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGuiScreen extends Screen {

    private static final int CATEGORY_WIDTH = 110;
    private static final int CATEGORY_GAP = 8;
    private static final int START_X = 30;
    private static final int START_Y = 30;
    private static final int HEADER_H = 18;
    private static final int ROW_H = 16;

    private final Map<Category, Boolean> expanded = new HashMap<>();
    private final Map<Module, Boolean> moduleExpanded = new HashMap<>();

    public ClickGuiScreen() {
        super(Text.literal("MeetionRC ClickGUI"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ctx.fill(0, 0, this.width, this.height, 0x80000000);

        int x = START_X;
        for (Category cat : Category.values()) {
            renderCategory(ctx, cat, x, mouseX, mouseY);
            x += CATEGORY_WIDTH + CATEGORY_GAP;
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderCategory(DrawContext ctx, Category cat, int x, int mouseX, int mouseY) {
        int y = START_Y;
        ctx.fill(x, y, x + CATEGORY_WIDTH, y + HEADER_H, 0xFF1A1A1A);
        ctx.drawTextWithShadow(client.textRenderer, "§b" + cat.getName(), x + 6, y + 5, 0xFFFFFFFF);
        if (!expanded.getOrDefault(cat, true)) return;
        y += HEADER_H;

        List<Module> mods = MeetionRC.getInstance().getModuleManager().getByCategory(cat);
        for (Module m : mods) {
            int color = m.isEnabled() ? 0xFF1F4A6E : 0xFF222222;
            ctx.fill(x, y, x + CATEGORY_WIDTH, y + ROW_H, color);
            ctx.drawTextWithShadow(client.textRenderer, m.getName(), x + 6, y + 4,
                    m.isEnabled() ? 0xFF44C2FF : 0xFFB0B0B0);
            y += ROW_H;
            if (moduleExpanded.getOrDefault(m, false)) {
                for (Setting<?> s : m.getSettings()) {
                    if (!s.isVisible()) continue;
                    ctx.fill(x, y, x + CATEGORY_WIDTH, y + ROW_H, 0xFF161616);
                    ctx.drawTextWithShadow(client.textRenderer, s.getName() + ": §7" + valueOf(s),
                            x + 12, y + 4, 0xFFD0D0D0);
                    y += ROW_H;
                }
            }
        }
    }

    private String valueOf(Setting<?> s) {
        if (s instanceof BooleanSetting bs) return bs.getValue() ? "ON" : "OFF";
        if (s instanceof NumberSetting ns) return String.format("%.2f", ns.getValue());
        if (s instanceof ModeSetting ms) return ms.getValue();
        if (s.getValue() == null) return "";
        return String.valueOf(s.getValue());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = START_X;
        for (Category cat : Category.values()) {
            int y = START_Y;
            if (within(mouseX, mouseY, x, y, CATEGORY_WIDTH, HEADER_H)) {
                expanded.merge(cat, false, (a, b) -> !a);
                return true;
            }
            if (!expanded.getOrDefault(cat, true)) { x += CATEGORY_WIDTH + CATEGORY_GAP; continue; }
            y += HEADER_H;
            for (Module m : MeetionRC.getInstance().getModuleManager().getByCategory(cat)) {
                if (within(mouseX, mouseY, x, y, CATEGORY_WIDTH, ROW_H)) {
                    if (button == 0) m.toggle();
                    else moduleExpanded.merge(m, true, (a, b) -> !a);
                    return true;
                }
                y += ROW_H;
                if (moduleExpanded.getOrDefault(m, false)) {
                    for (Setting<?> s : m.getSettings()) {
                        if (!s.isVisible()) continue;
                        if (within(mouseX, mouseY, x, y, CATEGORY_WIDTH, ROW_H)) {
                            tweak(s, button);
                            return true;
                        }
                        y += ROW_H;
                    }
                }
            }
            x += CATEGORY_WIDTH + CATEGORY_GAP;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void tweak(Setting<?> s, int button) {
        if (s instanceof BooleanSetting bs) bs.setValue(!bs.getValue());
        else if (s instanceof ModeSetting ms) ms.cycle();
        else if (s instanceof NumberSetting ns) {
            double delta = button == 0 ? ns.getStep() : -ns.getStep();
            double next = Math.max(ns.getMin(), Math.min(ns.getMax(), ns.getValue() + delta));
            ns.setValue(next);
        }
    }

    private boolean within(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() { MeetionRC.getInstance().getConfigManager().save(); super.close(); }

    public static void open() {
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        mc.setScreen(new ClickGuiScreen());
    }
}
