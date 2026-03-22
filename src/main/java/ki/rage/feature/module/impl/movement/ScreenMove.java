package ki.rage.feature.module.impl.movement;

import ki.rage.client.event.api.EventTarget;
import ki.rage.client.event.impl.TickEvent;
import ki.rage.feature.module.api.Category;
import ki.rage.feature.module.api.Module;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class ScreenMove extends Module {
    public ScreenMove() {
        super("ScreenMove", Category.Movement);
    }

    @EventTarget
    public void onTick(TickEvent.Pre e) {
        if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen)) {
            for (KeyBinding k : new KeyBinding[]{
                    mc.options.forwardKey,
                    mc.options.backKey,
                    mc.options.leftKey,
                    mc.options.rightKey,
                    mc.options.jumpKey,
                    mc.options.sprintKey
            }) {
                k.setPressed(InputUtil.isKeyPressed(mc.getWindow(), InputUtil.fromTranslationKey(k.getBoundKeyTranslationKey()).getCode()));
            }
        }
    }
}
