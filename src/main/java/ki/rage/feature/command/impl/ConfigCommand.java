package ki.rage.feature.command.impl;

import ki.rage.client.Client;
import ki.rage.client.file.ConfigManager;
import ki.rage.feature.command.api.Command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ConfigCommand extends Command {
    private static final Path CONFIG_DIR = Paths.get("rage", "configs");

    public ConfigCommand() {
        super("config", "Manage configurations", "cfg", "c");
    }

    @Override
    public void execute(String[] args) {
        ConfigManager configManager = Client.getInstance().getConfigManager();
        
        if (args.length == 0) {
            sendMessage("Current config: " + configManager.getCurrentConfig());
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "save" -> {
                if (args.length < 2) {
                    sendMessage("Usage: .config save <name>");
                    return;
                }
                configManager.saveConfig(args[1]);
                sendMessage("Config saved: " + args[1]);
            }
            case "load" -> {
                if (args.length < 2) {
                    sendMessage("Usage: .config load <name>");
                    return;
                }
                configManager.loadConfig(args[1]);
                sendMessage("Config loaded: " + args[1]);
            }
            case "del", "delete" -> {
                if (args.length < 2) {
                    sendMessage("Usage: .config del <name>");
                    return;
                }
                deleteConfig(args[1]);
            }
            case "folder" -> openFolder();
            case "list" -> listConfigs();
            case "clear" -> clearConfigs();
            case "reset" -> resetConfig();
            default -> sendMessage("Unknown action. Use .help config for more info");
        }
    }

    private void deleteConfig(String name) {
        File file = CONFIG_DIR.resolve(name + ".json").toFile();
        if (!file.exists()) {
            sendMessage("Config not found: " + name);
            return;
        }
        if (file.delete()) {
            sendMessage("Config deleted: " + name);
        } else {
            sendMessage("Failed to delete config: " + name);
        }
    }

    private void openFolder() {
        try {
            Files.createDirectories(CONFIG_DIR);
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("explorer " + CONFIG_DIR.toAbsolutePath());
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open " + CONFIG_DIR.toAbsolutePath());
            } else {
                Runtime.getRuntime().exec("xdg-open " + CONFIG_DIR.toAbsolutePath());
            }
            sendMessage("Opening config folder...");
        } catch (IOException e) {
            sendMessage("Failed to open folder: " + e.getMessage());
        }
    }

    private void listConfigs() {
        ConfigManager configManager = Client.getInstance().getConfigManager();
        
        try {
            if (!Files.exists(CONFIG_DIR)) {
                sendMessage("No configs found");
                return;
            }

            try (Stream<Path> paths = Files.list(CONFIG_DIR)) {
                String[] configs = paths
                        .filter(p -> p.toString().endsWith(".json"))
                        .map(p -> p.getFileName().toString().replace(".json", ""))
                        .toArray(String[]::new);

                if (configs.length == 0) {
                    sendMessage("No configs found");
                } else {
                    sendMessage("Available configs:");
                    for (String config : configs) {
                        String marker = config.equals(configManager.getCurrentConfig()) ? " (current)" : "";
                        sendMessage("  " + config + marker);
                    }
                }
            }
        } catch (IOException e) {
            sendMessage("Failed to list configs: " + e.getMessage());
        }
    }

    private void clearConfigs() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                sendMessage("No configs to clear");
                return;
            }

            try (Stream<Path> paths = Files.list(CONFIG_DIR)) {
                long count = paths
                        .filter(p -> p.toString().endsWith(".json"))
                        .peek(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        })
                        .count();

                sendMessage("Cleared " + count + " config(s)");
            }
        } catch (IOException e) {
            sendMessage("Failed to clear configs: " + e.getMessage());
        }
    }

    private void resetConfig() {
        for (var module : Client.getInstance().getModuleManager().all()) {
            module.setEnabled(false);
            module.setKey(0);
        }
        sendMessage("Config reset to defaults");
    }

    private void sendMessage(String message) {
        Client.getInstance().getCommandManager().sendMessage(message);
    }

    @Override
    public String getUsage() {
        return ".config <save|load|del|folder|list|clear|reset> [name]";
    }
}
