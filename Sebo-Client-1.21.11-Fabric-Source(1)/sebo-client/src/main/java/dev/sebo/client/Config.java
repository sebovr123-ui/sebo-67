package dev.sebo.client;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.util.*;

final class Config {
    enum Mode { LIVE, SEED, HYBRID }
    static final Path DIR=FabricLoader.getInstance().getConfigDir().resolve("sebo-client");
    static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    Set<String> enabled=new HashSet<>(Set.of("block","coords","fps"));
    Set<String> blocks=new HashSet<>(Modules.ids("diamond_ore","deepslate_diamond_ore"));
    Map<String,Integer> colors=new HashMap<>();
    Mode mode=Mode.LIVE;
    int radius=4, sectionsPerTick=24;
    boolean labels=true;
    String seed="";
    static Config load() {
        try { Config c=JSON.fromJson(Files.readString(DIR.resolve("settings.json")),Config.class);
            if(c==null) return new Config();
            c.radius=Math.clamp(c.radius,1,4); c.sectionsPerTick=Math.clamp(c.sectionsPerTick,4,48);
            if(c.enabled==null)c.enabled=new HashSet<>(); if(c.blocks==null)c.blocks=new HashSet<>();
            if(c.colors==null)c.colors=new HashMap<>(); if(c.mode==null)c.mode=Mode.LIVE; if(c.seed==null)c.seed="";
            return c;
        } catch(Exception e){ return new Config(); }
    }
    void save(){ try { Files.createDirectories(DIR); Files.writeString(DIR.resolve("settings.tmp"),JSON.toJson(this)); Files.move(DIR.resolve("settings.tmp"),DIR.resolve("settings.json"),StandardCopyOption.REPLACE_EXISTING); } catch(Exception e){ SeboClient.LOG.warn("Could not save settings",e); } }
}
