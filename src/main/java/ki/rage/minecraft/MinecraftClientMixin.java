package ki.rage.minecraft;

import ki.rage.client.Client;
import ki.rage.client.event.impl.Render2DEvent;
import ki.rage.client.event.impl.TickEvent;
import ki.rage.client.screen.gui.ClickGui;
import ki.rage.client.util.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;swapBuffers(Lnet/minecraft/client/util/tracy/TracyFrameCapturer;)V", shift = At.Shift.BEFORE))
    private void renderAfterBlit(boolean tick, CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;

        Window window = client.getWindow();
        int framebufferWidth = window.getFramebufferWidth();
        int framebufferHeight = window.getFramebufferHeight();
        int scaleFactor = window.scaleFactor;

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(0, 0, framebufferWidth, framebufferHeight);
        GL11.glDrawBuffer(GL11.GL_BACK);
        GL11.glReadBuffer(GL11.GL_BACK);

        Render2D.beginFrame(framebufferWidth, framebufferHeight, scaleFactor);
        Client.getInstance().getEventBus().post(new Render2DEvent(framebufferWidth, framebufferHeight, scaleFactor));
        if (client.currentScreen instanceof ClickGui clickGui) {
            clickGui.renderGui(client.mouse.getX(), client.mouse.getY());
        }
        Render2D.endFrame();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickPre(CallbackInfo ci) {
        Client.getInstance().getEventBus().post(new TickEvent.Pre());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickPost(CallbackInfo ci) {
        Client.getInstance().getEventBus().post(new TickEvent.Post());
    }
}
