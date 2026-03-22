package ki.rage.feature.module.impl.movement;

import ki.rage.client.event.api.EventTarget;
import ki.rage.client.event.impl.TickEvent;
import ki.rage.feature.module.api.Category;
import ki.rage.feature.module.api.Module;
import ki.rage.feature.module.api.setting.SliderSetting;
import net.minecraft.entity.attribute.EntityAttributes;

public class Step extends Module {
    public final SliderSetting stepHeight = register(new SliderSetting("Step Height", 1f, 0.6f, 10f, 0.1f));

    public Step() {
        super("Step", Category.Movement);
    }

    @EventTarget
    public void onTick(TickEvent.Pre e) {
        if (mc.player.horizontalCollision && mc.player.isOnGround()) mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT).setBaseValue(stepHeight.get());
        else mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT).setBaseValue(0.6);
    }

    @Override
    protected void onEnable() {
        mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT).setBaseValue(0.6);
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT).setBaseValue(0.6);
        super.onDisable();
    }
}
