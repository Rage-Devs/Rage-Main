package ki.rage.feature.command.impl;

import ki.rage.client.Client;
import ki.rage.client.file.MacroManager;
import ki.rage.feature.command.api.Command;
import org.lwjgl.glfw.GLFW;

public class MacroCommand extends Command {

    public MacroCommand() {
        super("macro", "Manage macros", "m");
    }

    @Override
    public void execute(String[] args) {
        MacroManager macroManager = Client.getInstance().getMacroManager();

        if (args.length == 0) {
            sendMessage("Usage: .macro <add|del|list|clear> [args]");
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "add" -> {
                if (args.length < 3) {
                    sendMessage("Usage: .macro add <name> <key> <text>");
                    return;
                }
                String name = args[1];
                int key = parseKey(args[2]);
                if (key == -1) {
                    sendMessage("Invalid key: " + args[2]);
                    return;
                }
                StringBuilder text = new StringBuilder();
                for (int i = 3; i < args.length; i++) {
                    if (i > 3) text.append(" ");
                    text.append(args[i]);
                }
                macroManager.add(name, key, text.toString());
                sendMessage("Added macro: " + name + " [" + GLFW.glfwGetKeyName(key, 0) + "]");
            }
            case "del", "delete", "remove" -> {
                if (args.length < 2) {
                    sendMessage("Usage: .macro del <name>");
                    return;
                }
                String name = args[1];
                macroManager.remove(name);
                sendMessage("Removed macro: " + name);
            }
            case "list" -> {
                if (macroManager.getAll().isEmpty()) {
                    sendMessage("No macros");
                } else {
                    sendMessage("Macros (" + macroManager.getAll().size() + "):");
                    for (MacroManager.Macro macro : macroManager.getAll()) {
                        String keyName = GLFW.glfwGetKeyName(macro.getKey(), 0);
                        sendMessage("  " + macro.getName() + " [" + keyName + "] -> " + macro.getText());
                    }
                }
            }
            case "clear" -> {
                macroManager.clear();
                sendMessage("Cleared all macros");
            }
            default -> sendMessage("Unknown action. Use .help macro for more info");
        }
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
        return ".macro <add|del|list|clear> [args]";
    }
}
