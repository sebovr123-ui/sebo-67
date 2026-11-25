package dev.sebo.client.mixin;
import dev.sebo.client.Overlay;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(method="render",at=@At("HEAD"))
    private void sebo$projection(ObjectAllocator allocator, RenderTickCounter ticks, boolean outline, Camera camera, Matrix4f view, Matrix4f basicProjection, Matrix4f projection, GpuBufferSlice fog, Vector4f fogColor, boolean sky, CallbackInfo ci){
        Overlay.capture(camera.getCameraPos(),view,basicProjection);
    }
}
