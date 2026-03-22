package ki.rage.feature.module.impl.player;

import ki.rage.client.event.api.EventTarget;
import ki.rage.client.event.impl.TickEvent;
import ki.rage.feature.module.api.Category;
import ki.rage.feature.module.api.Module;
import net.minecraft.client.gui.screen.DeathScreen;

public class AutoRespawn extends Module {
    public AutoRespawn() {
        super("AutoRespawn", Category.Player);
    }

    @EventTarget
    public void onTick(TickEvent.Pre e) {
        if (mc.currentScreen instanceof DeathScreen) {
            mc.setScreen(null);
            mc.player.requestRespawn();
        }
    }
}
