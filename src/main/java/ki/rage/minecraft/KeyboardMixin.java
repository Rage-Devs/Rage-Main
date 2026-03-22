package ki.rage.minecraft;

import ki.rage.client.Client;
import ki.rage.client.event.impl.KeyEvent;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int action, KeyInput keyInput, CallbackInfo ci) {
        int key = keyInput.key();
        int scancode = keyInput.scancode();
        int modifiers = keyInput.modifiers();
        KeyEvent event = new KeyEvent(key, scancode, action, modifiers);
        Client.getInstance().getEventBus().post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}