package ki.rage.feature.module.impl.misc;

import ki.rage.client.event.api.EventTarget;
import ki.rage.client.event.impl.TickEvent;
import ki.rage.feature.module.api.Category;
import ki.rage.feature.module.api.Module;
import ki.rage.feature.module.api.setting.ModeSetting;
import ki.rage.feature.module.api.setting.MultiModeSetting;
import ki.rage.feature.module.api.setting.SliderSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.Arrays;

public class AutoLeave extends Module {
    private final MultiModeSetting triggers = register(new MultiModeSetting("Triggers", Arrays.asList("Players", "Health"), "Players"));
    private final SliderSetting playerDistance = register(new SliderSetting("Player Distance", 10.0, 1.0, 50.0, 1.0)).setVisible(() -> triggers.isEnabled("Players"));
    private final SliderSetting health = register(new SliderSetting("Health", 10.0, 1.0, 20.0, 0.5)).setVisible(() -> triggers.isEnabled("Health"));
    private final ModeSetting leaveMode = register(new ModeSetting("Leave Mode", "Disconnect", Arrays.asList("Disconnect", "/spawn", "/hub")));

    public AutoLeave() {
        super("AutoLeave", Category.Misc);
    }

    @EventTarget
    public void onTick(TickEvent.Pre e) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        boolean shouldLeave = false;

        if (triggers.isEnabled("Players")) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player) {
                    continue;
                }
                if (mc.player.distanceTo(player) <= playerDistance.get()) {
                    shouldLeave = true;
                    break;
                }
            }
        }

        if (triggers.isEnabled("Health")) {
            if (mc.player.getHealth() <= health.get()) {
                shouldLeave = true;
            }
        }

        if (shouldLeave) {
            switch (leaveMode.value()) {
                case "Disconnect":
                    if (mc.world != null) {
                        mc.world.disconnect(Text.literal("AutoLeave"));
                    }
                    break;
                case "/spawn":
                    if (mc.player != null) {
                        mc.player.networkHandler.sendChatCommand("spawn");
                    }
                    break;
                case "/hub":
                    if (mc.player != null) {
                        mc.player.networkHandler.sendChatCommand("hub");
                    }
                    break;
            }

            setEnabled(false);
        }
    }
}
