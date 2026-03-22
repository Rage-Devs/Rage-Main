package ki.rage.feature.command.api;

import ki.rage.client.Client;
import ki.rage.client.event.api.EventTarget;
import ki.rage.client.event.impl.ChatEvent;
import ki.rage.feature.command.impl.BindCommand;
import ki.rage.feature.command.impl.ClearRamCommand;
import ki.rage.feature.command.impl.ConfigCommand;
import ki.rage.feature.command.impl.FriendCommand;
import ki.rage.feature.command.impl.HelpCommand;
import ki.rage.feature.command.impl.MacroCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private static final String PREFIX = ".";
    private final List<Command> commands = new ArrayList<>();
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public CommandManager() {
        Client.getInstance().getEventBus().register(this);
        register(
                new HelpCommand(this),
                new ConfigCommand(),
                new FriendCommand(),
                new MacroCommand(),
                new BindCommand(),
                new ClearRamCommand()
        );
    }

    private void register(Command... commands) {
        for (Command command : commands) {
            this.commands.add(command);
        }
    }

    @EventTarget
    public void onChat(ChatEvent event) {
        String message = event.getMessage();
        if (!message.startsWith(PREFIX)) {
            return;
        }

        event.setCancelled(true);

        String[] parts = message.substring(PREFIX.length()).split(" ");
        String commandName = parts[0].toLowerCase();

        Command command = findCommand(commandName);
        if (command == null) {
            sendMessage("Unknown command. Use .help for list of commands");
            return;
        }

        try {
            String[] args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, args.length);
            command.execute(args);
        } catch (Exception e) {
            sendMessage("Error executing command: " + e.getMessage());
        }
    }

    private Command findCommand(String name) {
        for (Command command : commands) {
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

    public void sendMessage(String message) {
        if (mc.player == null) return;

        MutableText text = Text.literal("[").formatted(Formatting.GRAY)
                .append(Text.literal("Rage").formatted(Formatting.RED))
                .append(Text.literal("] ").formatted(Formatting.GRAY))
                .append(Text.literal("-> ").formatted(Formatting.GRAY))
                .append(Text.literal(message).formatted(Formatting.WHITE));

        mc.player.sendMessage(text, false);
    }

    public List<Command> getCommands() {
        return commands;
    }
}
