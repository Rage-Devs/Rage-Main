package ki.rage.client.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ki.rage.client.Client;
import ki.rage.client.event.api.EventTarget;
import ki.rage.client.event.impl.KeyEvent;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MacroManager {
    private static final Path FILES_DIR = Paths.get("rage", "files");
    private static final Path MACROS_FILE = FILES_DIR.resolve("macros.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final List<Macro> macros = new ArrayList<>();
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public MacroManager() {
        try {
            Files.createDirectories(FILES_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Client.getInstance().getEventBus().register(this);
    }

    public void load() {
        File file = MACROS_FILE.toFile();
        if (!file.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            List<Macro> loaded = GSON.fromJson(reader, new TypeToken<List<Macro>>(){}.getType());
            if (loaded != null) {
                macros.clear();
                macros.addAll(loaded);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(MACROS_FILE.toFile())) {
            GSON.toJson(macros, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(String name, int key, String text) {
        macros.removeIf(m -> m.getName().equalsIgnoreCase(name));
        macros.add(new Macro(name, key, text));
        save();
    }

    public void remove(String name) {
        if (macros.removeIf(m -> m.getName().equalsIgnoreCase(name))) {
            save();
        }
    }

    public void clear() {
        macros.clear();
        save();
    }

    public List<Macro> getAll() {
        return new ArrayList<>(macros);
    }

    @EventTarget
    public void onKey(KeyEvent event) {
        if (event.getAction() != GLFW.GLFW_PRESS || mc.currentScreen != null) {
            return;
        }

        for (Macro macro : macros) {
            if (macro.getKey() == event.getKey()) {
                if (mc.player != null) {
                    mc.player.networkHandler.sendChatMessage(macro.getText());
                }
            }
        }
    }

    @Getter
    public static class Macro {
        private final String name;
        private final int key;
        private final String text;

        public Macro(String name, int key, String text) {
            this.name = name;
            this.key = key;
            this.text = text;
        }
    }
}
