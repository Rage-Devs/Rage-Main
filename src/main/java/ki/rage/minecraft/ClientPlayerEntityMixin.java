package ki.rage.minecraft;

import com.mojang.authlib.GameProfile;
import ki.rage.client.Client;
import ki.rage.client.event.impl.UsingItemEvent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Redirect(method = "applyMovementSpeedFactors", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec2f;multiply(F)Lnet/minecraft/util/math/Vec2f;", ordinal = 1))
    private Vec2f cancelItemSlowdown(Vec2f vec2f, float multiplier) {
        UsingItemEvent event = new UsingItemEvent();
        Client.getInstance().getEventBus().post(event);

        if (event.isCancelled() && this.isUsingItem() && !this.hasVehicle()) {
            return vec2f.multiply(1.0F);
        }

        return vec2f.multiply(multiplier);
    }
}
