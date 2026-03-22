package ki.rage.client;

import ki.rage.client.event.api.EventBus;
import ki.rage.client.file.ConfigManager;
import ki.rage.client.file.FriendManager;
import ki.rage.client.file.MacroManager;
import ki.rage.client.screen.hud.api.HudManager;
import ki.rage.client.util.IMinecraft;
import ki.rage.feature.command.api.CommandManager;
import ki.rage.feature.module.api.ModuleManager;
import lombok.Getter;
import net.fabricmc.api.ClientModInitializer;

// young money 2010

@Getter
public class Client implements ClientModInitializer, IMinecraft {
    @Getter
    private static Client instance;

    private final EventBus eventBus = new EventBus();
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private CommandManager commandManager;
    private FriendManager friendManager;
    private MacroManager macroManager;
    private HudManager hudManager;

    private final String clientName = "Rage";
    private final String clientVersion = "b1";

    @Override
    public void onInitializeClient() {
        instance = this;
        moduleManager = new ModuleManager();
        configManager = new ConfigManager();
        friendManager = new FriendManager();
        macroManager = new MacroManager();
        commandManager = new CommandManager();
        hudManager = new HudManager();
        
        configManager.load();
        friendManager.load();
        macroManager.load();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            configManager.save();
            friendManager.save();
            macroManager.save();
        }));
    }
}
