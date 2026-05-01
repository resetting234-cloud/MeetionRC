package meetion.rc.ui.screens;

import meetion.rc.MeetionRC;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.modules.visual.HudModule;
import meetion.rc.ui.font.Fonts;
import meetion.rc.ui.framework.CategoryTab;
import meetion.rc.ui.framework.Component;
import meetion.rc.ui.framework.ModuleListPanel;
import meetion.rc.ui.framework.SearchField;
import meetion.rc.ui.framework.SettingsPanel;
import meetion.rc.ui.utils.BlurUtil;
import meetion.rc.ui.utils.ColorUtil;
import meetion.rc.ui.utils.Palette;
import meetion.rc.ui.utils.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * The METTRC Contextual Dock ClickGUI.
 *
 * <p>Layout: the screen is centered horizontally. A fixed 280px left panel holds the
 * wordmark, a search field, category tabs, and the {@link ModuleListPanel}. The
 * {@link SettingsPanel} on the right starts at 0px width and slides out to 300px when
 * a module is selected.
 *
 * <p>Background uses {@link BlurUtil} — Mojang native blur if {@code HudModule.realBlur}
 * is on, otherwise our cheap matte tint + scanlines fallback.
 */
public class ClickGuiScreen extends Screen {

    private static final int LEFT_WIDTH       = 280;
    private static final int HEADER_HEIGHT    = 96;   // wordmark + search + tabs
    private static final int FOOTER_HEIGHT    = 22;
    private static final int PANEL_HEIGHT_MAX = 480;

    private final List<Component> components = new ArrayList<>();
    private final SearchField search       = new SearchField();
    private final List<CategoryTab> tabs   = new ArrayList<>();
    private final ModuleListPanel listPanel = new ModuleListPanel();
    private final SettingsPanel settingsPanel = new SettingsPanel();

    private int panelX, panelY, panelH;

    private Category activeCategory = Category.COMBAT;

    public ClickGuiScreen() {
        super(Text.literal(MeetionRC.NAME + " ClickGUI"));
    }

    public static void open() {
        MinecraftClient.getInstance().setScreen(new ClickGuiScreen());
    }

    @Override
    protected void init() {
        super.init();

        components.clear();
        tabs.clear();

        // search wires straight into the list panel filter
        search.placeholder("Search modules…").onChange(listPanel::setQuery);

        // build a tab per category; clicking one swaps the active filter
        for (Category c : Category.values()) {
            CategoryTab tab = new CategoryTab(c).onSelect(this::switchCategory);
            tab.setSelected(c == activeCategory);
            tabs.add(tab);
        }

        listPanel.setCategory(activeCategory)
                 .onModuleSelected(this::onModuleSelected);

        components.add(search);
        components.addAll(tabs);
        components.add(listPanel);
        components.add(settingsPanel);

        layout();
    }

    private void layout() {
        // total visible width = left panel + (animated) settings panel; we lay out for
        // the *full* width so things don't shift around when the slide animator runs.
        int totalW = LEFT_WIDTH + SettingsPanel.FULL_WIDTH;
        panelH = Math.min(PANEL_HEIGHT_MAX, height - 60);
        panelX = (width - totalW) / 2;
        panelY = (height - panelH) / 2;

        int leftX = panelX;

        // search bar (under wordmark)
        search.setBounds(leftX + 16, panelY + 36, LEFT_WIDTH - 32, 22);

        // tabs row, evenly distributed under the search
        int tabsY = panelY + 66;
        int tabH  = 26;
        int gap   = 2;
        int tabW  = (LEFT_WIDTH - 16 - gap * (tabs.size() - 1)) / Math.max(1, tabs.size());
        int tabX  = leftX + 8;
        for (CategoryTab tab : tabs) {
            tab.setBounds(tabX, tabsY, tabW, tabH);
            tabX += tabW + gap;
        }

        // module list takes the remaining vertical space
        int listY = panelY + HEADER_HEIGHT + 4;
        int listH = panelH - HEADER_HEIGHT - FOOTER_HEIGHT - 8;
        listPanel.setBounds(leftX, listY, LEFT_WIDTH, listH);

        // settings panel anchored to the right edge of the left panel
        settingsPanel.anchor(leftX + LEFT_WIDTH, panelY, panelH);
    }

    @Override
    public void resize(MinecraftClient client, int w, int h) {
        super.resize(client, w, h);
        layout();
    }

    private void switchCategory(Category c) {
        activeCategory = c;
        for (CategoryTab tab : tabs) tab.setSelected(tab.getCategory() == c);
        listPanel.setCategory(c);
        // collapse settings panel — different category, different module set
        settingsPanel.show(null);
        listPanel.setSelected(null);
    }

    private void onModuleSelected(Module m) {
        listPanel.setSelected(m);
        settingsPanel.show(m);
    }

    // ---------------------------------------------------------------------------
    // Render
    // ---------------------------------------------------------------------------

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        BlurUtil.beginFrame();
        HudModule hud = MeetionRC.getInstance().getModuleManager().get(HudModule.class);
        boolean realBlur = hud != null && hud.isRealBlurEnabled();
        BlurUtil.apply(ctx, realBlur, 8f, this.width, this.height);

        // dark vignette behind the panels for extra legibility
        RenderUtil.dropShadow(ctx, panelX - 4, panelY - 4,
                LEFT_WIDTH + SettingsPanel.FULL_WIDTH + 8, panelH + 8, 8, 4, 140);

        // base panel
        RenderUtil.roundedRect(ctx, panelX, panelY, LEFT_WIDTH, panelH, 6, Palette.BG_PANEL);
        // settings panel renders its own frame in render()

        // wordmark (top of left panel)
        Fonts.wordmark(ctx, panelX + 16, panelY + 14, false);
        // version + close hint, right-aligned
        String hint = "v" + MeetionRC.VERSION + "  ·  RShift";
        int hintW = Fonts.width(Fonts.INTER, hint);
        Fonts.draw(ctx, Fonts.INTER, hint, panelX + LEFT_WIDTH - hintW - 16, panelY + 16,
                Palette.TEXT_MUTED);

        // header divider
        ctx.fill(panelX + 12, panelY + HEADER_HEIGHT, panelX + LEFT_WIDTH - 12,
                panelY + HEADER_HEIGHT + 1, ColorUtil.argb(255, 255, 255, 12));

        // tick + render every component
        for (Component c : components) c.update(mouseX, mouseY);
        for (Component c : components) c.render(ctx, mouseX, mouseY, delta);

        // footer hint bar inside the left panel
        renderFooter(ctx);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderFooter(DrawContext ctx) {
        int fy = panelY + panelH - FOOTER_HEIGHT;
        ctx.fill(panelX + 12, fy, panelX + LEFT_WIDTH - 12, fy + 1,
                ColorUtil.argb(255, 255, 255, 12));
        String left = "[Esc] close";
        String right = "[RShift] toggle";
        Fonts.draw(ctx, Fonts.INTER, left, panelX + 16, fy + 7, Palette.TEXT_MUTED);
        int rw = Fonts.width(Fonts.INTER, right);
        Fonts.draw(ctx, Fonts.INTER, right, panelX + LEFT_WIDTH - rw - 16, fy + 7,
                Palette.TEXT_MUTED);
    }

    // ---------------------------------------------------------------------------
    // Event passthrough
    // ---------------------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = components.size() - 1; i >= 0; i--) {
            if (components.get(i).mouseClicked(mouseX, mouseY, button)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean any = false;
        for (Component c : components) if (c.mouseReleased(mouseX, mouseY, button)) any = true;
        return any || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        boolean any = false;
        for (Component c : components) if (c.mouseDragged(mouseX, mouseY, button, dx, dy)) any = true;
        return any || super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        for (int i = components.size() - 1; i >= 0; i--) {
            if (components.get(i).mouseScrolled(mouseX, mouseY, horizontal, vertical)) return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        for (int i = components.size() - 1; i >= 0; i--) {
            if (components.get(i).keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (int i = components.size() - 1; i >= 0; i--) {
            if (components.get(i).charTyped(chr, modifiers)) return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // we draw our own background with BlurUtil; suppress vanilla blur which conflicts.
    }
}
