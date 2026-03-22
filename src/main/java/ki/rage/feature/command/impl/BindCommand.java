package ki.rage.feature.command.impl;

import ki.rage.client.Client;
import ki.rage.feature.command.api.Command;
import ki.rage.feature.module.api.Module;
import org.lwjgl.glfw.GLFW;

public class BindCommand extends Command {

    public BindCommand() {
        super("bind", "Manage module keybinds", "b");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            sendMessage("Usage: .bind <module> <key> | .bind del <module> | .bind list | .bind clear");
            return;
        }

        String action = args[0].toLowerCase();

        if (action.equals("list")) {
            listBinds();
            return;
        }

        if (action.equals("clear")) {
            clearBinds();
            return;
        }

        if (action.equals("del") || action.equals("delete") || action.equals("remove")) {
            if (args.length < 2) {
                sendMessage("Usage: .bind del <module>");
                return;
            }
            Module module = findModule(args[1]);
            if (module == null) {
                sendMessage("Module not found: " + args[1]);
                return;
            }
            module.setKey(0);
            sendMessage("Removed bind from " + module.name());
            return;
        }

        if (args.length < 2) {
            sendMessage("Usage: .bind <module> <key>");
            return;
        }

        Module module = findModule(args[0]);
        if (module == null) {
            sendMessage("Module not found: " + args[0]);
            return;
        }

        int key = parseKey(args[1]);
        if (key == -1) {
            sendMessage("Invalid key: " + args[1]);
            return;
        }

        module.setKey(key);
        String keyName = GLFW.glfwGetKeyName(key, 0);
        sendMessage("Bound " + module.name() + " to " + (keyName != null ? keyName : "key " + key));
    }

    private void listBinds() {
        sendMessage("Module binds:");
        int count = 0;
        for (Module module : Client.getInstance().getModuleManager().all()) {
            if (module.key() != 0) {
                String keyName = GLFW.glfwGetKeyName(module.key(), 0);
                sendMessage("  " + module.name() + " -> " + (keyName != null ? keyName : "key " + module.key()));
                count++;
            }
        }
        if (count == 0) {
            sendMessage("  No binds set");
        }
    }

    private void clearBinds() {
        for (Module module : Client.getInstance().getModuleManager().all()) {
            module.setKey(0);
        }
        sendMessage("Cleared all binds");
    }

    private Module findModule(String name) {
        for (Module module : Client.getInstance().getModuleManager().all()) {
            if (module.name().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    private int parseKey(String keyStr) {
        try {
            return Integer.parseInt(keyStr);
        } catch (NumberFormatException e) {
            String upper = keyStr.toUpperCase();
            try {
                return (int) GLFW.class.getField("GLFW_KEY_" + upper).get(null);
            } catch (Exception ex) {
                return -1;
            }
        }
    }

    private void sendMessage(String message) {
        Client.getInstance().getCommandManager().sendMessage(message);
    }

    @Override
    public String getUsage() {
        return ".bind <module> <key> | .bind del <module> | .bind list | .bind clear";
    }
}
