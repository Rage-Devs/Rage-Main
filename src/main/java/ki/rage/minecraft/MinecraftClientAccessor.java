package ki.rage.minecraft;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor("itemUseCooldown")
    void setItemUseCooldown(int cooldown);
    
    @Accessor("itemUseCooldown")
    int getItemUseCooldown();
    
    @Invoker("doItemUse")
    void invokeDoItemUse();
}
