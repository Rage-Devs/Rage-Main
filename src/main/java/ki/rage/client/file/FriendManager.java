package ki.rage.client.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FriendManager {
    private static final Path FILES_DIR = Paths.get("rage", "files");
    private static final Path FRIENDS_FILE = FILES_DIR.resolve("friends.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final List<String> friends = new ArrayList<>();

    public FriendManager() {
        try {
            Files.createDirectories(FILES_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        File file = FRIENDS_FILE.toFile();
        if (!file.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            List<String> loaded = GSON.fromJson(reader, new TypeToken<List<String>>(){}.getType());
            if (loaded != null) {
                friends.clear();
                friends.addAll(loaded);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(FRIENDS_FILE.toFile())) {
            GSON.toJson(friends, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(String name) {
        if (!friends.contains(name)) {
            friends.add(name);
            save();
        }
    }

    public void remove(String name) {
        if (friends.remove(name)) {
            save();
        }
    }

    public void clear() {
        friends.clear();
        save();
    }

    public boolean isFriend(String name) {
        return friends.contains(name);
    }

    public List<String> getAll() {
        return new ArrayList<>(friends);
    }
}
