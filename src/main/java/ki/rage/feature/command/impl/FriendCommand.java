package ki.rage.feature.command.impl;

import ki.rage.client.Client;
import ki.rage.client.file.FriendManager;
import ki.rage.feature.command.api.Command;

public class FriendCommand extends Command {

    public FriendCommand() {
        super("friend", "Manage friends", "f");
    }

    @Override
    public void execute(String[] args) {
        FriendManager friendManager = Client.getInstance().getFriendManager();

        if (args.length == 0) {
            sendMessage("Usage: .friend <add|del|list|clear> [name]");
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "add" -> {
                if (args.length < 2) {
                    sendMessage("Usage: .friend add <name>");
                    return;
                }
                String name = args[1];
                if (friendManager.isFriend(name)) {
                    sendMessage(name + " is already your friend");
                } else {
                    friendManager.add(name);
                    sendMessage("Added friend: " + name);
                }
            }
            case "del", "delete", "remove" -> {
                if (args.length < 2) {
                    sendMessage("Usage: .friend del <name>");
                    return;
                }
                String name = args[1];
                if (!friendManager.isFriend(name)) {
                    sendMessage(name + " is not your friend");
                } else {
                    friendManager.remove(name);
                    sendMessage("Removed friend: " + name);
                }
            }
            case "list" -> {
                if (friendManager.getAll().isEmpty()) {
                    sendMessage("You have no friends");
                } else {
                    sendMessage("Friends (" + friendManager.getAll().size() + "):");
                    for (String friend : friendManager.getAll()) {
                        sendMessage("  " + friend);
                    }
                }
            }
            case "clear" -> {
                friendManager.clear();
                sendMessage("Cleared all friends");
            }
            default -> sendMessage("Unknown action. Use .help friend for more info");
        }
    }

    private void sendMessage(String message) {
        Client.getInstance().getCommandManager().sendMessage(message);
    }

    @Override
    public String getUsage() {
        return ".friend <add|del|list|clear> [name]";
    }
}
