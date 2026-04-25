package meetion.rc.manager;

import meetion.rc.MeetionRC;
import meetion.rc.core.event.EventBus;
import meetion.rc.core.event.EventHandler;
import meetion.rc.core.event.events.ChatSendEvent;
import meetion.rc.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class CommandManager {

    public void init() {
        EventBus.register(this);
    }

    @EventHandler
    public void onChatSend(ChatSendEvent event) {
        String msg = event.getMessage();
        if (!msg.startsWith(MeetionRC.PREFIX)) return;
        event.cancel();

        String body = msg.substring(MeetionRC.PREFIX.length()).trim();
        if (body.isEmpty()) { help(); return; }

        String[] parts = body.split("\\s+");
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "help", "h" -> help();
            case "toggle", "t" -> toggle(parts);
            case "bind", "b" -> bind(parts);
            case "config", "cfg" -> config(parts);
            case "friend", "f" -> friend(parts);
            case "prefix" -> info("Current prefix: " + MeetionRC.PREFIX);
            default -> info("Unknown command: " + cmd);
        }
    }

    private void help() {
        info("§b" + MeetionRC.NAME + " §7v" + MeetionRC.VERSION);
        info("§7.help - show this");
        info("§7.toggle <module>");
        info("§7.bind <module> <key|none>");
        info("§7.config <save|load|list> [name]");
        info("§7.friend <add|remove|list> [name]");
    }

    private void toggle(String[] parts) {
        if (parts.length < 2) { info("Usage: .toggle <module>"); return; }
        Module m = MeetionRC.getInstance().getModuleManager().getByName(parts[1]);
        if (m == null) { info("Module not found: " + parts[1]); return; }
        m.toggle();
        info(m.getName() + " -> " + (m.isEnabled() ? "§aON" : "§cOFF"));
    }

    private void bind(String[] parts) {
        if (parts.length < 3) { info("Usage: .bind <module> <key|none>"); return; }
        Module m = MeetionRC.getInstance().getModuleManager().getByName(parts[1]);
        if (m == null) { info("Module not found: " + parts[1]); return; }
        String key = parts[2];
        if (key.equalsIgnoreCase("none")) {
            m.setKey(org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN);
            info(m.getName() + " unbound");
            return;
        }
        int code = MeetionRC.getInstance().getKeybindManager().resolve(key);
        m.setKey(code);
        info(m.getName() + " bound to " + key.toUpperCase());
    }

    private void config(String[] parts) {
        if (parts.length < 2) { info("Usage: .config <save|load|list> [name]"); return; }
        ConfigManager cm = MeetionRC.getInstance().getConfigManager();
        switch (parts[1].toLowerCase()) {
            case "save" -> {
                String name = parts.length > 2 ? parts[2] : "default";
                cm.save(name);
                info("Config saved: " + name);
            }
            case "load" -> {
                String name = parts.length > 2 ? parts[2] : "default";
                cm.load(name);
                info("Config loaded: " + name);
            }
            case "list" -> info(String.join(", ", cm.list()));
            default -> info("Unknown config subcommand");
        }
    }

    private void friend(String[] parts) {
        FriendManager fm = MeetionRC.getInstance().getFriendManager();
        if (parts.length < 2) { info("Usage: .friend <add|remove|list> [name]"); return; }
        switch (parts[1].toLowerCase()) {
            case "add" -> { if (parts.length >= 3) { fm.add(parts[2]); info("Added " + parts[2]); } }
            case "remove", "rm" -> { if (parts.length >= 3) { fm.remove(parts[2]); info("Removed " + parts[2]); } }
            case "list" -> info(String.join(", ", fm.list()));
            default -> info("Unknown friend subcommand");
        }
    }

    public static void info(String msg) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§8[§bMRC§8] §r" + msg), false);
        }
    }
}
