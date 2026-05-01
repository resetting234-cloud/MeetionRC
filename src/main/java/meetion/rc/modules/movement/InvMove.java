package meetion.rc.modules.movement;
import meetion.rc.core.module.AutoModule;

import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.TickEvent;
import meetion.rc.core.module.Category;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.impl.BooleanSetting;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;

@AutoModule
public class InvMove extends Module {

    private final BooleanSetting allowChat = register(new BooleanSetting("InChat", false));
    private final BooleanSetting allowContainer = register(new BooleanSetting("InContainer", true));

    public InvMove() {
        super("InvMove", "Move while inventory or chat is open", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(TickEvent ev) {
        if (ev.getEra() != meetion.rc.core.event.Event.Era.PRE) return;
        if (mc().player == null) return;
        Screen screen = mc().currentScreen;
        if (screen == null) return;

        boolean ok = false;
        if (screen instanceof HandledScreen<?>) ok = allowContainer.getValue();
        else if (screen instanceof net.minecraft.client.gui.screen.ChatScreen) ok = allowChat.getValue();

        if (!ok) return;

        var opt = mc().options;
        KeyBinding.setKeyPressed(opt.forwardKey.getDefaultKey(), isKeyDown(opt.forwardKey));
        KeyBinding.setKeyPressed(opt.backKey.getDefaultKey(), isKeyDown(opt.backKey));
        KeyBinding.setKeyPressed(opt.leftKey.getDefaultKey(), isKeyDown(opt.leftKey));
        KeyBinding.setKeyPressed(opt.rightKey.getDefaultKey(), isKeyDown(opt.rightKey));
        KeyBinding.setKeyPressed(opt.jumpKey.getDefaultKey(), isKeyDown(opt.jumpKey));
        KeyBinding.setKeyPressed(opt.sprintKey.getDefaultKey(), isKeyDown(opt.sprintKey));
    }

    private boolean isKeyDown(KeyBinding bind) {
        var key = bind.getDefaultKey();
        if (key.getCategory() == net.minecraft.client.util.InputUtil.Type.KEYSYM) {
            return org.lwjgl.glfw.GLFW.glfwGetKey(mc().getWindow().getHandle(), key.getCode()) == 1;
        }
        return false;
    }
}
