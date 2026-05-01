package meetion.rc.ui.framework;

import meetion.rc.MeetionRC;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Scrollable list of {@link ModuleComponent} rows for the currently active category,
 * filtered by an optional search query.
 *
 * <p>Owned by {@code ClickGuiScreen}. The screen pushes new state in via
 * {@link #setCategory} and {@link #setQuery}; the panel rebuilds its children list
 * every time either changes. {@link #onModuleSelected} routes row clicks back up
 * to the screen so the right SettingsPanel can swap content.
 */
public class ModuleListPanel extends Panel {

    private static final int ROW_PADDING_X = 6;
    private static final int ROW_GAP       = 2;

    private Category currentCategory = Category.COMBAT;
    private String query = "";
    private Consumer<Module> onModuleSelected = m -> {};
    private Module selected = null;

    public ModuleListPanel() {
        this.clip = true;
    }

    public ModuleListPanel onModuleSelected(Consumer<Module> handler) {
        this.onModuleSelected = handler == null ? m -> {} : handler;
        return this;
    }

    public Category getCategory() { return currentCategory; }

    public ModuleListPanel setCategory(Category c) {
        if (c == null || c == currentCategory) return this;
        this.currentCategory = c;
        rebuild();
        return this;
    }

    public ModuleListPanel setQuery(String q) {
        String n = q == null ? "" : q.toLowerCase(Locale.ROOT).trim();
        if (n.equals(this.query)) return this;
        this.query = n;
        rebuild();
        return this;
    }

    public ModuleListPanel setSelected(Module m) {
        if (this.selected == m) return this;
        this.selected = m;
        for (Component c : children) {
            if (c instanceof ModuleComponent mc) {
                mc.setSelected(mc.getModule() == m);
            }
        }
        return this;
    }

    @Override
    public Component setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        rebuild();
        return this;
    }

    private void rebuild() {
        clearChildren();
        if (w <= 0 || h <= 0) return;

        List<Module> modules = MeetionRC.getInstance().getModuleManager().getByCategory(currentCategory);
        int rowY = y;
        for (Module m : modules) {
            if (!query.isEmpty() && !m.getName().toLowerCase(Locale.ROOT).contains(query)) continue;
            ModuleComponent mc = new ModuleComponent(m).onSelect(onModuleSelected);
            mc.setBounds(x + ROW_PADDING_X, rowY, w - ROW_PADDING_X * 2, 22);
            mc.setSelected(m == selected);
            addChild(mc);
            rowY += 22 + ROW_GAP;
        }
        setContentHeight(rowY - y);
    }
}
