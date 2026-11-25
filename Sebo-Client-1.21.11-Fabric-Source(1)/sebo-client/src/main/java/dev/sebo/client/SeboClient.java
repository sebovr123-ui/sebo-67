package dev.sebo.client;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SeboClient implements ClientModInitializer {
    static final Logger LOG=LoggerFactory.getLogger("Sebo Client");
    @Override public void onInitializeClient(){
        Config config=Config.load();Modules modules=new Modules(config);Scanner scanner=new Scanner(config,modules);
        KeyBinding menu=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.sebo.menu",GLFW.GLFW_KEY_RIGHT_SHIFT,KeyBinding.Category.MISC));
        ClientTickEvents.END_CLIENT_TICK.register(mc->{while(menu.wasPressed()){if(mc.currentScreen instanceof ClickGui)mc.setScreen(null);else if(mc.currentScreen==null)mc.setScreen(new ClickGui(config,modules,scanner));}scanner.tick(mc);});
        HudElementRegistry.addLast(Identifier.of("sebo","overlay"),(ctx,ticks)->Overlay.draw(ctx,MinecraftClient.getInstance(),config,scanner));
    }
}
