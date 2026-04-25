package meetion.rc.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import meetion.rc.MeetionRC;
import meetion.rc.core.module.Module;
import meetion.rc.core.setting.Setting;
import meetion.rc.core.setting.impl.*;
import net.minecraft.client.MinecraftClient;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConfigManager {

    private final Path root;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ConfigManager() {
        this.root = MinecraftClient.getInstance().runDirectory.toPath().resolve(MeetionRC.NAME);
        //noinspection ResultOfMethodCallIgnored
        root.toFile().mkdirs();
    }

    public void load() { load("default"); }

    public void save() { save("default"); }

    public void save(String name) {
        JsonObject root = new JsonObject();
        JsonArray modulesArr = new JsonArray();
        for (Module module : MeetionRC.getInstance().getModuleManager().getModules()) {
            JsonObject mObj = new JsonObject();
            mObj.addProperty("name", module.getName());
            mObj.addProperty("enabled", module.isEnabled());
            mObj.addProperty("key", module.getKey());
            JsonObject settings = new JsonObject();
            for (Setting<?> s : module.getSettings()) {
                writeSetting(settings, s);
            }
            mObj.add("settings", settings);
            modulesArr.add(mObj);
        }
        root.add("modules", modulesArr);
        root.add("friends", gson.toJsonTree(MeetionRC.getInstance().getFriendManager().list()));

        try (FileWriter w = new FileWriter(file(name))) {
            gson.toJson(root, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load(String name) {
        File f = file(name);
        if (!f.exists()) return;
        try (FileReader r = new FileReader(f)) {
            JsonObject root = gson.fromJson(r, JsonObject.class);
            if (root == null) return;

            if (root.has("modules")) {
                for (var el : root.getAsJsonArray("modules")) {
                    JsonObject mObj = el.getAsJsonObject();
                    Module module = MeetionRC.getInstance().getModuleManager().getByName(mObj.get("name").getAsString());
                    if (module == null) continue;
                    module.setKey(mObj.has("key") ? mObj.get("key").getAsInt() : module.getKey());
                    if (mObj.has("settings")) {
                        JsonObject settings = mObj.getAsJsonObject("settings");
                        for (Setting<?> s : module.getSettings()) {
                            if (settings.has(s.getName())) readSetting(settings, s);
                        }
                    }
                    if (mObj.has("enabled") && mObj.get("enabled").getAsBoolean()) module.setEnabled(true);
                }
            }

            if (root.has("friends")) {
                for (var el : root.getAsJsonArray("friends")) {
                    MeetionRC.getInstance().getFriendManager().add(el.getAsString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void writeSetting(JsonObject obj, Setting<?> s) {
        if (s instanceof BooleanSetting bs) obj.addProperty(s.getName(), bs.getValue());
        else if (s instanceof NumberSetting ns) obj.addProperty(s.getName(), ns.getValue());
        else if (s instanceof ModeSetting ms) obj.addProperty(s.getName(), ms.getValue());
        else if (s instanceof BindSetting bk) obj.addProperty(s.getName(), bk.getValue());
        else if (s instanceof ColorSetting cs) obj.addProperty(s.getName(), cs.getValue().getRGB());
        else if (s instanceof MultiSelectSetting mss) obj.add(s.getName(), gson.toJsonTree(mss.getValue()));
    }

    @SuppressWarnings("unchecked")
    private void readSetting(JsonObject obj, Setting<?> s) {
        if (s instanceof BooleanSetting) s.setValue(obj.get(s.getName()).getAsBoolean());
        else if (s instanceof NumberSetting) s.setValue(obj.get(s.getName()).getAsDouble());
        else if (s instanceof ModeSetting) s.setValue(obj.get(s.getName()).getAsString());
        else if (s instanceof BindSetting) s.setValue(obj.get(s.getName()).getAsInt());
        else if (s instanceof ColorSetting) s.setValue(new Color(obj.get(s.getName()).getAsInt(), true));
        else if (s instanceof MultiSelectSetting) {
            var set = new HashSet<String>();
            for (var el : obj.getAsJsonArray(s.getName())) set.add(el.getAsString());
            ((Setting<java.util.Set<String>>) s).setValue(set);
        }
    }

    public List<String> list() {
        File[] files = root.toFile().listFiles((dir, n) -> n.endsWith(".json"));
        List<String> result = new ArrayList<>();
        if (files != null) for (File f : files) result.add(f.getName().replace(".json", ""));
        return result;
    }

    private File file(String name) {
        return root.resolve(name + ".json").toFile();
    }
}
