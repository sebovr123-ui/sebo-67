package dev.sebo.client;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.ChunkStatus;
import java.util.*;

final class Scanner {
    record Cell(int x,int z) {}
    record Hit(long pos,String id) {}
    record Job(Cell cell,int section) {}
    record Display(Hit hit,int color,boolean predicted) {}
    static final int CAP=256, RENDER_CAP=300;
    final Config cfg; final Modules modules;
    final Map<Cell,List<Hit>> live=new HashMap<>();
    final Map<Cell,List<Hit>> building=new HashMap<>();
    final ArrayDeque<Job> queue=new ArrayDeque<>();
    final Map<Cell,Integer> counts=new HashMap<>();
    final Set<Cell> capped=new HashSet<>();
    Map<String,Integer> colors=Map.of();
    final Map<Block,String> ids=new HashMap<>();
    ClientWorld world; int cx=Integer.MIN_VALUE,cz,radius; int tick; int loaded; boolean recording=false; String recordSeed=""; long revision; long shownRevision=-1; List<Display> shown=List.of();
    final ReferenceStore refs=new ReferenceStore();
    Scanner(Config cfg,Modules modules){this.cfg=cfg;this.modules=modules;}
    String dimension(){return world==null?"":world.getRegistryKey().getValue().toString();}
    void reset(){world=null; live.clear();building.clear();queue.clear();counts.clear();capped.clear(); loaded=0;recording=false;}
    void invalidate(){ cx=Integer.MIN_VALUE; queue.clear(); building.clear(); counts.clear(); live.clear(); capped.clear(); }
    void tick(MinecraftClient mc){
        revision++;
        if(mc.world==null||mc.player==null){if(world!=null)reset();return;}
        if(world!=mc.world){reset();world=mc.world;cx=Integer.MIN_VALUE;}
        Map<String,Integer> now=modules.targets(cfg);
        if(!now.equals(colors)){colors=now;ids.clear();for(Block b:Registries.BLOCK){String id=Registries.BLOCK.getId(b).toString();if(colors.containsKey(id))ids.put(b,id);}invalidate();}
        int x=mc.player.getBlockX()>>4,z=mc.player.getBlockZ()>>4;
        if(x!=cx||z!=cz||radius!=cfg.radius){cx=x;cz=z;radius=cfg.radius;queue.clear();building.clear();counts.clear();live.keySet().removeIf(c->!inRange(c));capped.removeIf(c->!inRange(c));start();}
        if(colors.isEmpty()) {live.clear();return;}
        if(cfg.mode==Config.Mode.SEED&&!recording)return;
        if(queue.isEmpty() && ++tick%10==0)start();
        long deadline=System.nanoTime()+3_000_000L;
        for(int i=0;i<cfg.sectionsPerTick&&!queue.isEmpty()&&(i==0||System.nanoTime()<deadline);i++){
            Job j=queue.remove(); Cell c=j.cell(); WorldChunk chunk=chunk(c);
            if(chunk==null){live.remove(c);building.remove(c);counts.remove(c);queue.removeIf(k->k.cell().equals(c));continue;}
            ChunkSection[] sections=chunk.getSectionArray();
            List<Hit> hits=building.computeIfAbsent(c,k->new ArrayList<>());
            if(j.section()<sections.length){ChunkSection s=sections[j.section()];
                if(hits.size()<CAP && s.hasAny(st->ids.containsKey(st.getBlock()))) {
                    int by=chunk.getBottomY()+j.section()*16;
                    outer: for(int y=0;y<16;y++)for(int dz=0;dz<16;dz++)for(int dx=0;dx<16;dx++){
                        String id=ids.get(s.getBlockState(dx,y,dz).getBlock()); if(id!=null){
                            if(hits.size()==CAP){capped.add(c);break outer;}
                            hits.add(new Hit(BlockPos.asLong(c.x()*16+dx,by+y,c.z()*16+dz),id));
                        }
                    }
                } else if(hits.size()>=CAP && s.hasAny(st->ids.containsKey(st.getBlock()))) capped.add(c);
            }
            int left=counts.merge(c,-1,Integer::sum);
            if(left==0){live.put(c,List.copyOf(hits));building.remove(c);counts.remove(c);}
        }
        if(recording&&queue.isEmpty()) {recording=false;refs.capture(recordSeed,dimension(),live,colors.keySet(),capped);}
    }
    boolean inRange(Cell c){return Math.abs(c.x()-cx)<=radius&&Math.abs(c.z()-cz)<=radius;}
    WorldChunk chunk(Cell c){return world.getChunkManager().getChunk(c.x(),c.z(),ChunkStatus.FULL,false);}
    void start(){
        if(world==null||colors.isEmpty())return;
        loaded=0; capped.clear();
        List<Cell> cells=new ArrayList<>();for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++)cells.add(new Cell(cx+x,cz+z));
        cells.sort(Comparator.comparingInt(c->Math.abs(c.x()-cx)+Math.abs(c.z()-cz)));
        for(Cell c:cells){WorldChunk ch=chunk(c);if(ch==null){live.remove(c);continue;}loaded++;int n=ch.getSectionArray().length;counts.put(c,n);for(int i=0;i<n;i++)queue.add(new Job(c,i));}
    }
    List<Display> display(MinecraftClient mc){
        if(world==null||world!=mc.world||mc.player==null||colors.isEmpty())return List.of();
        if(shownRevision==revision)return shown;
        Map<Long,Display> out=new HashMap<>();
        if(cfg.mode!=Config.Mode.LIVE)for(Hit h:refs.lookup(cfg.seed,dimension(),cx,cz,radius))if(colors.containsKey(h.id()))out.put(h.pos(),new Display(h,0xFFF4B942,true));
        if(cfg.mode!=Config.Mode.SEED)for(var e:live.entrySet())if(inRange(e.getKey())&&chunk(e.getKey())!=null)for(Hit h:e.getValue())if(colors.containsKey(h.id()))out.put(h.pos(),new Display(h,colors.get(h.id()),false));
        double x=mc.player.getX(),y=mc.player.getY(),z=mc.player.getZ();
        shownRevision=revision; return shown=out.values().stream().sorted(Comparator.comparingDouble(d->BlockPos.fromLong(d.hit().pos()).getSquaredDistanceFromCenter(x,y,z))).limit(RENDER_CAP).toList();
    }
    String capture(MinecraftClient mc){
        if(mc.getServer()==null||world==null)return "Capture requires a fresh single-player world.";
        if(colors.isEmpty())return "Enable a scanner and choose targets first.";
        String actual=Long.toString(mc.getServer().getOverworld().getSeed());
        cfg.seed=actual;recordSeed=actual;cfg.save();live.clear();queue.clear();building.clear();counts.clear();start();recording=true;
        return "Capturing reference for seed "+actual;
    }
}
