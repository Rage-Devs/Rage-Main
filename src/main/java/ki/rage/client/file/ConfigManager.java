package ki.rage.client.file;

import com.google.gson.*;
import ki.rage.client.Client;
import ki.rage.feature.module.api.Module;
import ki.rage.feature.module.api.setting.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
    private static final Path CONFIG_DIR = Paths.get("rage", "configs");
    private static final Path LAST_CONFIG_FILE = CONFIG_DIR.resolve("last.txt");
    private static final String DEFAULT_CONFIG = "startconfig";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private String currentConfig = DEFAULT_CONFIG;

    public ConfigManager() {
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        String configName = loadLastUsedConfig();
        loadConfig(configName);
    }

    public void save() {
        saveConfig(currentConfig);
        saveLastUsedConfig(currentConfig);
    }

    public void loadConfig(String name) {
        File file = CONFIG_DIR.resolve(name + ".json").toFile();
        if (!file.exists()) {
            currentConfig = name;
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            
            for (Module module : Client.getInstance().getModuleManager().all()) {
                if (!root.has(module.name())) continue;
                
                JsonObject moduleObj = root.getAsJsonObject(module.name());
                
                if (moduleObj.has("enabled")) {
                    module.setEnabled(moduleObj.get("enabled").getAsBoolean());
                }
                
                if (moduleObj.has("key")) {
                    module.setKey(moduleObj.get("key").getAsInt());
                }
                
                if (moduleObj.has("settings")) {
                    JsonObject settingsObj = moduleObj.getAsJsonObject("settings");
                    for (Setting<?> setting : module.settings()) {
                        if (!settingsObj.has(setting.name())) continue;
                        
                        loadSetting(setting, settingsObj.get(setting.name()));
                    }
                }
            }
            
            currentConfig = name;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveConfig(String name) {
        JsonObject root = new JsonObject();
        
        for (Module module : Client.getInstance().getModuleManager().all()) {
            JsonObject moduleObj = new JsonObject();
            moduleObj.addProperty("enabled", module.enabled());
            moduleObj.addProperty("key", module.key());
            
            JsonObject settingsObj = new JsonObject();
            for (Setting<?> setting : module.settings()) {
                saveSetting(setting, settingsObj);
            }
            moduleObj.add("settings", settingsObj);
            
            root.add(module.name(), moduleObj);
        }
        
        try (FileWriter writer = new FileWriter(CONFIG_DIR.resolve(name + ".json").toFile())) {
            GSON.toJson(root, writer);
            currentConfig = name;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSetting(Setting<?> setting, JsonElement element) {
        if (setting instanceof BooleanSetting boolSetting) {
            boolSetting.setValue(element.getAsBoolean());
        } else if (setting instanceof SliderSetting numSetting) {
            numSetting.setValue(element.getAsDouble());
        } else if (setting instanceof ModeSetting modeSetting) {
            modeSetting.setValue(element.getAsString());
        } else if (setting instanceof ColorSetting colorSetting) {
            JsonObject colorObj = element.getAsJsonObject();
            colorSetting.setRgba(
                colorObj.get("r").getAsInt(),
                colorObj.get("g").getAsInt(),
                colorObj.get("b").getAsInt(),
                colorObj.get("a").getAsInt()
            );
        }
    }

    private void saveSetting(Setting<?> setting, JsonObject parent) {
        if (setting instanceof BooleanSetting boolSetting) {
            parent.addProperty(setting.name(), boolSetting.enabled());
        } else if (setting instanceof SliderSetting numSetting) {
            parent.addProperty(setting.name(), numSetting.value());
        } else if (setting instanceof ModeSetting modeSetting) {
            parent.addProperty(setting.name(), modeSetting.value());
        } else if (setting instanceof ColorSetting colorSetting) {
            JsonObject colorObj = new JsonObject();
            colorObj.addProperty("r", colorSetting.r());
            colorObj.addProperty("g", colorSetting.g());
            colorObj.addProperty("b", colorSetting.b());
            colorObj.addProperty("a", colorSetting.a());
            parent.add(setting.name(), colorObj);
        }
    }

    private String loadLastUsedConfig() {
        File file = LAST_CONFIG_FILE.toFile();
        if (!file.exists()) {
            return DEFAULT_CONFIG;
        }
        
        try {
            String name = Files.readString(file.toPath()).trim();
            return name.isEmpty() ? DEFAULT_CONFIG : name;
        } catch (Exception e) {
            e.printStackTrace();
            return DEFAULT_CONFIG;
        }
    }

    private void saveLastUsedConfig(String name) {
        try (FileWriter writer = new FileWriter(LAST_CONFIG_FILE.toFile())) {
            writer.write(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCurrentConfig() {
        return currentConfig;
    }
}
