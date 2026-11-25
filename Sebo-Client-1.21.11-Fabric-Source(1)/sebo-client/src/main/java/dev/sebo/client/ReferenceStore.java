package dev.sebo.client;
import java.nio.file.*;
import java.util.*;
import com.google.gson.reflect.TypeToken;

/** Captured vanilla-generated reference chunks, not a fabricated seed predictor. */
final class ReferenceStore {
    record Snapshot(Set<String> targets,List<Scanner.Hit> hits,boolean capped) {}
    String active=""; Map<String,Snapshot> data=new HashMap<>();
    void select(String seed,String dim){
        String key=seed+"_"+dim.replaceAll("[^A-Za-z0-9_-]","_");
        if(key.equals(active))return;active=key;data=new HashMap<>();
        if(!seed.matches("-?\\d{1,19}"))return;
        try {Map<String,Snapshot> d=Config.JSON.fromJson(Files.readString(path()),new TypeToken<Map<String,Snapshot>>(){}.getType());if(d!=null)data=d;}catch(Exception ignored){}
    }
    Path path(){return Config.DIR.resolve("references").resolve(active+".json");}
    void capture(String seed,String dim,Map<Scanner.Cell,List<Scanner.Hit>> hits,Set<String> targets,Set<Scanner.Cell> capped){
        select(seed,dim);
        for(var e:hits.entrySet()){String k=e.getKey().x()+","+e.getKey().z();Snapshot old=data.get(k);Set<String> all=new HashSet<>(targets);List<Scanner.Hit> merged=new ArrayList<>(e.getValue());
            if(old!=null){all.addAll(old.targets());for(var h:old.hits())if(!targets.contains(h.id()))merged.add(h);}
            data.put(k,new Snapshot(all,merged,capped.contains(e.getKey())||(old!=null&&old.capped())));}
        try{Files.createDirectories(path().getParent());Files.writeString(path(),Config.JSON.toJson(data));}catch(Exception e){SeboClient.LOG.warn("Reference save failed",e);}
    }
    List<Scanner.Hit> lookup(String seed,String dim,int cx,int cz,int radius){select(seed,dim);List<Scanner.Hit> out=new ArrayList<>();for(int x=cx-radius;x<=cx+radius;x++)for(int z=cz-radius;z<=cz+radius;z++){Snapshot s=data.get(x+","+z);if(s!=null)out.addAll(s.hits());}return out;}
}
