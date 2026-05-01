package meetion.rc;

import meetion.rc.core.event.EventBus;
import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.KeyEvent;
import meetion.rc.manager.*;
import meetion.rc.screen.clickgui.ClickGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class MeetionRC implements ClientModInitializer {

    public static final String NAME = "METTRC";
    public static final String NAME_RED = "MET";   // styled red half of the wordmark
    public static final String NAME_WHITE = "TRC"; // styled white half
    public static final String VERSION = "0.2.0";
    public static final String PREFIX = ".";

    private static MeetionRC instance;
    private ModuleManager moduleManager;
    private CommandManager commandManager;
    private ConfigManager configManager;
    private FriendManager friendManager;
    private KeybindManager keybindManager;

    @Override
    public void onInitializeClient() {
        instance = this;
        moduleManager = new ModuleManager();
        commandManager = new CommandManager();
        friendManager = new FriendManager();
        keybindManager = new KeybindManager();
        configManager = new ConfigManager();

        moduleManager.init();
        commandManager.init();
        configManager.load();
        EventBus.register(this);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { configManager.save(); } catch (Exception ignored) {}
        }));
    }

    @EventHandler
    public void onKey(KeyEvent ev) {
        if (ev.getAction() != GLFW.GLFW_PRESS) return;
        if (ev.getKey() == GLFW.GLFW_KEY_RIGHT_SHIFT && mc().currentScreen == null && mc().player != null) {
            ClickGuiScreen.open();
        }
    }

    public static MeetionRC getInstance() { return instance; }
    public static MinecraftClient mc() { return MinecraftClient.getInstance(); }
    public ModuleManager getModuleManager() { return moduleManager; }
    public CommandManager getCommandManager() { return commandManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public FriendManager getFriendManager() { return friendManager; }
    public KeybindManager getKeybindManager() { return keybindManager; }
}
