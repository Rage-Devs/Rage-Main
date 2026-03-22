package ki.rage.feature.command.impl;

import ki.rage.client.Client;
import ki.rage.feature.command.api.Command;
import ki.rage.feature.command.api.CommandManager;

public class HelpCommand extends Command {

    public HelpCommand(CommandManager commandManager) {
        super("help", "Show information about commands", "h", "?");
    }

    @Override
    public void execute(String[] args) {
        CommandManager commandManager = Client.getInstance().getCommandManager();
        
        if (args.length == 0) {
            commandManager.sendMessage("Available commands:");
            for (Command command : commandManager.getCommands()) {
                commandManager.sendMessage("  ." + command.getName() + " - " + command.getDescription());
            }
        } else {
            String commandName = args[0].toLowerCase();
            Command command = findCommand(commandName);
            
            if (command == null) {
                commandManager.sendMessage("Command not found: " + commandName);
                return;
            }

            commandManager.sendMessage("Command: ." + command.getName());
            commandManager.sendMessage("Description: " + command.getDescription());
            commandManager.sendMessage("Usage: " + command.getUsage());
            
            if (!command.getAliases().isEmpty()) {
                commandManager.sendMessage("Aliases: " + String.join(", ", command.getAliases()));
            }
        }
    }

    private Command findCommand(String name) {
        CommandManager commandManager = Client.getInstance().getCommandManager();
        
        for (Command command : commandManager.getCommands()) {
            if (command.getName().equalsIgnoreCase(name)) {
                return command;
            }
            for (String alias : command.getAliases()) {
                if (alias.equalsIgnoreCase(name)) {
                    return command;
                }
            }
        }
        return null;
    }

    @Override
    public String getUsage() {
        return ".help [command]";
    }
}
