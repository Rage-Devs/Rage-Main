package ki.rage.minecraft;

import ki.rage.client.Client;
import ki.rage.client.event.impl.ChatEvent;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(String message, boolean addToHistory, CallbackInfo ci) {
        ChatEvent event = new ChatEvent(message);
        Client.getInstance().getEventBus().post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
