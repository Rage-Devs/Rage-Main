package ki.rage.minecraft;

import com.mojang.blaze3d.systems.RenderSystem;
import ki.rage.client.Client;
import ki.rage.client.event.impl.Render3DEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(at = @At(value = "RETURN"), method = "renderWorld")
    void render3dHook(RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        Camera camera = mc.gameRenderer.getCamera();
        MatrixStack matrixStack = new MatrixStack();

        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));

        Matrix4f modelViewMatrix = new Matrix4f(RenderSystem.getModelViewStack());
        RenderSystem.getModelViewStack().mul(matrixStack.peek().getPositionMatrix());

        Client.getInstance().getEventBus().post(new Render3DEvent(matrixStack, tickCounter.getTickProgress(true)));

        RenderSystem.getModelViewStack().set(modelViewMatrix);
    }
}