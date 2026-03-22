package ki.rage.feature.command.impl;

import ki.rage.client.Client;
import ki.rage.feature.command.api.Command;

public class ClearRamCommand extends Command {

    public ClearRamCommand() {
        super("clearram", "Clear unused memory", "ram", "gc");
    }

    @Override
    public void execute(String[] args) {
        Runtime runtime = Runtime.getRuntime();
        
        long usedMemoryBefore = (runtime.totalMemory() - runtime.freeMemory()) / 1048576;
        
        System.gc();
        
        long usedMemoryAfter = (runtime.totalMemory() - runtime.freeMemory()) / 1048576;
        long freedMemory = usedMemoryBefore - usedMemoryAfter;
        
        Client.getInstance().getCommandManager().sendMessage(
            "Memory cleared: " + freedMemory + "MB freed (" + 
            usedMemoryAfter + "MB / " + (runtime.totalMemory() / 1048576) + "MB)"
        );
    }

    @Override
    public String getUsage() {
        return ".clearram";
    }
}
