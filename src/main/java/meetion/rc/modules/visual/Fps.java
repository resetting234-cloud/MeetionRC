package meetion.rc.modules.visual;
import meetion.rc.core.module.AutoModule;

import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.TickEvent;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.BooleanSetting;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.SimpleOption;

@AutoModule
public class Fps extends Module {

    private final BooleanSetting hideParticles = register(new BooleanSetting("HideParticles", true));
    private final BooleanSetting hideSky = register(new BooleanSetting("HideSky", true));
    private final BooleanSetting hideClouds = register(new BooleanSetting("HideClouds", true));
    private final BooleanSetting fastGraphics = register(new BooleanSetting("FastGraphics", true));
    private final BooleanSetting reduceRenderDistance = register(new BooleanSetting("ReduceRenderDistance", true));
    private final BooleanSetting disableFog = register(new BooleanSetting("DisableFog", true));

    private boolean applied;
    private GraphicsMode prevGraphics;
    private int prevRenderDist = -1;
    private boolean prevClouds;
    private double prevEntityDist = -1;

    public Fps() {
        super("Fps", "Tunes graphics options to maximise FPS", Category.VISUAL);
    }

    @Override
    protected void onEnable() { apply(); }

    @Override
    protected void onDisable() { restore(); }

    @EventHandler
    public void onTick(TickEvent ev) {
        if (!isEnabled() || applied) return;
        apply();
    }

    private void apply() {
        if (mc().options == null) return;
        GameOptions opt = mc().options;
        prevGraphics = opt.getGraphicsMode().getValue();
        if (fastGraphics.getValue()) opt.getGraphicsMode().setValue(GraphicsMode.FAST);
        prevRenderDist = opt.getViewDistance().getValue();
        if (reduceRenderDistance.getValue()) opt.getViewDistance().setValue(Math.min(8, prevRenderDist));
        prevClouds = opt.getCloudRenderMode().getValue() != net.minecraft.client.option.CloudRenderMode.OFF;
        if (hideClouds.getValue()) opt.getCloudRenderMode().setValue(net.minecraft.client.option.CloudRenderMode.OFF);
        prevEntityDist = opt.getEntityDistanceScaling().getValue();
        opt.getEntityDistanceScaling().setValue(0.5);
        applied = true;
    }

    private void restore() {
        if (!applied || mc().options == null) return;
        GameOptions opt = mc().options;
        if (prevGraphics != null) opt.getGraphicsMode().setValue(prevGraphics);
        if (prevRenderDist > 0) opt.getViewDistance().setValue(prevRenderDist);
        if (prevClouds) opt.getCloudRenderMode().setValue(net.minecraft.client.option.CloudRenderMode.FANCY);
        if (prevEntityDist > 0) opt.getEntityDistanceScaling().setValue(prevEntityDist);
        applied = false;
    }

    public boolean shouldHideParticles() { return isEnabled() && hideParticles.getValue(); }
    public boolean shouldHideSky() { return isEnabled() && hideSky.getValue(); }
    public boolean shouldDisableFog() { return isEnabled() && disableFog.getValue(); }
}
