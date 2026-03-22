package ki.rage.feature.module.impl;

import ki.rage.client.event.api.EventTarget;
import ki.rage.client.event.impl.TickEvent;
import ki.rage.feature.module.api.Category;
import ki.rage.feature.module.api.Module;

public class AutoSprint extends Module {
    public AutoSprint() {
        super("AutoSprint", Category.Movement);
    }

    @EventTarget
    public void onTick(TickEvent.Pre e) {
        if (mc.player != null && mc.player.forwardSpeed > .0 && !mc.player.horizontalCollision) mc.player.setSprinting(true);
    }
}
