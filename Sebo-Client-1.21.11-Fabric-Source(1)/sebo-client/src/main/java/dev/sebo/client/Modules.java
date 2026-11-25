package dev.sebo.client;
import java.util.*;
import java.util.function.Supplier;

/** Register new modules here; scanners share a single scan pass. */
final class Modules {
    record Module(String id, String name, String category, Supplier<Set<String>> targets, int color) {}
    final List<Module> entries = new ArrayList<>();
    Modules(Config cfg) {
        add("block", "Block ESP", "Render", () -> cfg.blocks, 0xFF50DDF5);
        add("amethyst", "Amethyst Scanner", "World", () -> ids("amethyst_block", "budding_amethyst", "amethyst_cluster", "small_amethyst_bud", "medium_amethyst_bud", "large_amethyst_bud"), 0xFFC084FC);
        add("base", "Base Scanner", "World", () -> ids("crafting_table", "furnace", "blast_furnace", "smoker", "chest", "barrel", "hopper", "redstone_block", "enchanting_table", "beacon", "shulker_box"), 0xFFFFA75D);
        add("chest", "Chest ESP", "Render", () -> ids("chest", "trapped_chest", "ender_chest", "barrel", "shulker_box"), 0xFFFBBF24);
        add("spawner", "Spawner ESP", "Render", () -> ids("spawner", "trial_spawner"), 0xFFF472B6);
        add("ore", "Ore ESP", "Render", () -> ids("diamond_ore", "deepslate_diamond_ore", "ancient_debris", "emerald_ore", "deepslate_emerald_ore", "gold_ore", "deepslate_gold_ore", "iron_ore", "deepslate_iron_ore"), 0xFF34D399);
        add("portal", "Portal Scanner", "World", () -> ids("nether_portal", "end_portal", "end_gateway"), 0xFFA78BFA);
        add("tracers", "Tracers", "Render", Set::of, 0);
        add("coords", "Coordinates HUD", "Utility", Set::of, 0);
        add("fps", "FPS HUD", "Utility", Set::of, 0);
    }
    void add(String id,String name,String category,Supplier<Set<String>> targets,int color) { entries.add(new Module(id,name,category,targets,color)); }
    static Set<String> ids(String... names) { Set<String> s=new HashSet<>(); for(String n:names)s.add("minecraft:"+n); return s; }
    Map<String,Integer> targets(Config c) {
        Map<String,Integer> result=new LinkedHashMap<>();
        for(Module m:entries) if(c.enabled.contains(m.id())) for(String id:m.targets().get()) result.putIfAbsent(id,c.colors.getOrDefault(id,m.color()) | 0xFF000000);
        return result;
    }
}
