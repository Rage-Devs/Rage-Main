package ki.rage.feature.module.impl;

import ki.rage.client.event.api.EventTarget;
import ki.rage.client.event.impl.TickEvent;
import ki.rage.client.event.impl.UsingItemEvent;
import ki.rage.feature.module.api.Category;
import ki.rage.feature.module.api.Module;
import ki.rage.feature.module.api.setting.BooleanSetting;
import ki.rage.feature.module.api.setting.ModeSetting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

import java.util.List;

public class NoSlow extends Module {
    public final ModeSetting mode = register(new ModeSetting("Mode", "Vanilla", List.of("Vanilla", "Grim", "GrimV3")));
    public final BooleanSetting onlyOnGround = register(new BooleanSetting("OnlyOnGround", true));

    public NoSlow() {
        super("NoSlow", Category.Movement);
    }

    @EventTarget
    public void onUsingItem(UsingItemEvent event) {
        if (mc.player == null || mc.world == null || !mc.player.isUsingItem() || mc.player.isGliding() || mc.player.isSneaking()) {
            return;
        }

        if (onlyOnGround.enabled() && !mc.player.isOnGround()) {
            return;
        }

        if (mode.value().equals("Vanilla") || mode.value().equals("Grim")) {
            event.setCancelled(true);
            return;
        }

        if (mode.value().equals("GrimV3")) {
            boolean boost = mc.player.age % 3 == 0 || mc.player.age % 4 == 0;
            if (boost) {
                event.setCancelled(true);
            }
        }
    }

    @EventTarget
    public void onPreTick(TickEvent.Pre event) {
        if (!mode.value().equals("Grim") || mc.player == null || !mc.player.isUsingItem() || mc.player.isSneaking()) {
            return;
        }

        if (isFood(mc.player.getActiveItem())) {
            float yaw = mc.player.getYaw();
            float pitch = mc.player.getPitch();

            if (mc.player.getActiveHand() == Hand.MAIN_HAND) {
                mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, 0, yaw, pitch));
            } else {
                mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, yaw, pitch));
            }
        }
    }

    private boolean isFood(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getComponents().contains(DataComponentTypes.FOOD);
    }
}
