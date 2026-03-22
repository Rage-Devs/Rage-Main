package ki.rage.feature.module.impl;

import ki.rage.client.event.api.EventTarget;
import ki.rage.client.event.impl.TickEvent;
import ki.rage.feature.module.api.Category;
import ki.rage.feature.module.api.Module;
import ki.rage.feature.module.api.setting.ModeSetting;
import ki.rage.feature.module.api.setting.SliderSetting;
import ki.rage.minecraft.MinecraftClientAccessor;
import net.minecraft.item.BlockItem;

import java.util.List;

public class FastPlace extends Module {
    public final ModeSetting mode = register(new ModeSetting("Mode", "Normal", List.of("Normal", "Ultra")));
    public final SliderSetting times = register(new SliderSetting("Times", 30, 5, 64, 1)).setVisible(() -> mode.value().equals("Ultra"));

    public FastPlace() {
        super("FastPlace", Category.Player);
    }

    @EventTarget
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.getMainHandStack().getItem() instanceof BlockItem) {
            switch (mode.value()) {
                case "Normal" -> ((MinecraftClientAccessor) mc).setItemUseCooldown(0);
                case "Ultra" -> {
                    if (mc.options.useKey.isPressed()) {
                        for (int i = 0; i < (int) times.get(); i++) {
                            ((MinecraftClientAccessor) mc).invokeDoItemUse();
                            ((MinecraftClientAccessor) mc).setItemUseCooldown(0);
                        }
                    }
                }
            }
        }
    }
}
