package dev.sebo.client;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.registry.Registries;
import net.minecraft.block.Block;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.*;

final class ClickGui extends Screen {
    final Config cfg;final Modules modules;final Scanner scanner;
    String tab="Modules",query="",status="";int page=0;
    int left,top,panelW,panelH,contentX,contentW;
    TextFieldWidget search;List<String> blocks=new ArrayList<>();
    static final int[] PALETTE={0xFF50DDF5,0xFFC084FC,0xFF34D399,0xFFFBBF24,0xFFF472B6,0xFFF9FAFB};
    ClickGui(Config cfg,Modules modules,Scanner scanner){super(Text.literal("Sebo Client"));this.cfg=cfg;this.modules=modules;this.scanner=scanner;for(Block b:Registries.BLOCK)blocks.add(Registries.BLOCK.getId(b).toString());Collections.sort(blocks);}
    @Override public boolean shouldPause(){return false;}
    @Override public boolean keyPressed(KeyInput input){if(input.key()==GLFW.GLFW_KEY_RIGHT_SHIFT){close();return true;}return super.keyPressed(input);}
    @Override public void close(){cfg.save();super.close();}
    void button(int x,int y,int w,String label,Runnable action){addDrawableChild(ButtonWidget.builder(Text.literal(label),b->action.run()).dimensions(x,y,w,20).build());}
    void refresh(){clearChildren();init();}
    @Override protected void init(){
        panelW=Math.min(560,width-12);panelH=Math.min(330,height-12);left=(width-panelW)/2;top=(height-panelH)/2;contentX=left+104;contentW=panelW-112;
        search=null;
        String[] tabs={"Modules","Blocks","Scanner","Settings"};for(int i=0;i<tabs.length;i++){String t=tabs[i];button(left+8,top+34+i*25,88,(tab.equals(t)?"» ":"")+t,()->{tab=t;page=0;refresh();});}
        button(left+8,top+panelH-28,88,"Close",this::close);
        int y=top+34;
        switch(tab){
            case "Modules" -> {int cols=contentW>=280?2:1, bw=(contentW-4*(cols-1))/cols;int rows=Math.max(1,(panelH-70)/23);int per=rows*cols;page=Math.min(page,Math.max(0,(modules.entries.size()-1)/per));int start=page*per;for(int i=start;i<Math.min(start+per,modules.entries.size());i++){var m=modules.entries.get(i);int j=i-start;button(contentX+(j%cols)*(bw+4),y+(j/cols)*23,bw,(cfg.enabled.contains(m.id())?"§aON  ":"§7OFF  ")+m.name(),()->{if(!cfg.enabled.add(m.id()))cfg.enabled.remove(m.id());cfg.save();refresh();});}nav(per,modules.entries.size());}
            case "Blocks" -> {
                search=addDrawableChild(new TextFieldWidget(textRenderer,contentX,y,contentW,20,Text.literal("Search block IDs")));search.setMaxLength(150);search.setText(query);search.setChangedListener(s->{query=s;page=0;});
                button(contentX,y+24,contentW/2-2,"Apply search",()->{refresh();});button(contentX+contentW/2+2,y+24,contentW/2-2,"Clear selection",()->{cfg.blocks.clear();cfg.save();refresh();});
                List<String> filtered=blocks.stream().filter(b->b.contains(query.toLowerCase(Locale.ROOT).replace(' ','_'))).toList();int per=Math.max(1,(panelH-118)/22);page=Math.min(page,Math.max(0,(filtered.size()-1)/per));int start=page*per;
                for(int i=start;i<Math.min(start+per,filtered.size());i++){String id=filtered.get(i);int yy=y+49+(i-start)*22;String name=id.startsWith("minecraft:")?id.substring(10):id;button(contentX,yy,contentW-57,(cfg.blocks.contains(id)?"§a✓ ":"§7+ ")+name,()->{if(!cfg.blocks.add(id))cfg.blocks.remove(id);cfg.save();refresh();});button(contentX+contentW-53,yy,53,"Color",()->{int c=cfg.colors.getOrDefault(id,PALETTE[0]);int index=0;for(int j=0;j<PALETTE.length;j++)if(PALETTE[j]==c)index=j;cfg.colors.put(id,PALETTE[(index+1)%PALETTE.length]);cfg.save();status="Color changed: "+name;refresh();});}nav(per,filtered.size());
            }
            case "Scanner" -> {
                button(contentX,y,contentW,"Mode: "+cfg.mode,()->{cfg.mode=Config.Mode.values()[(cfg.mode.ordinal()+1)%3];cfg.save();refresh();});
                button(contentX,y+24,contentW,"Area: "+((cfg.radius*2+1)*(cfg.radius*2+1))+" chunks",()->{cfg.radius=cfg.radius%4+1;cfg.save();refresh();});
                TextFieldWidget seed=addDrawableChild(new TextFieldWidget(textRenderer,contentX,y+48,contentW,20,Text.literal("Numeric seed")));seed.setMaxLength(20);seed.setText(cfg.seed);seed.setChangedListener(s->{cfg.seed=s.trim();cfg.save();});
                button(contentX,y+72,contentW,"Capture same-seed reference",()->{status=scanner.capture(client);refresh();});
                button(contentX,y+96,contentW,"Rescan",()->{scanner.invalidate();status="Rescan queued";refresh();});
            }
            case "Settings" -> {
                button(contentX,y,contentW,"Sections / tick: "+cfg.sectionsPerTick,()->{cfg.sectionsPerTick=cfg.sectionsPerTick>=48?4:Math.min(48,cfg.sectionsPerTick+4);cfg.save();refresh();});
                button(contentX,y+24,contentW,"Distance labels: "+(cfg.labels?"ON":"OFF"),()->{cfg.labels=!cfg.labels;cfg.save();refresh();});
            }
        }
    }
    void nav(int per,int count){int y=top+panelH-28;button(contentX,y,45,"<",()->{page=Math.max(0,page-1);refresh();});button(contentX+contentW-45,y,45,">",()->{page=Math.min(Math.max(0,(count-1)/per),page+1);refresh();});}
    @Override public void render(DrawContext ctx,int mx,int my,float delta){
        ctx.fill(0,0,width,height,0xAA080911);ctx.fill(left,top,left+panelW,top+panelH,0xFF151521);ctx.fill(left,top,left+panelW,top+25,0xFF30244B);ctx.fill(left+98,top+25,left+99,top+panelH,0xFF493B63);
        ctx.drawText(textRenderer,"SEBO / CLIENT",left+9,top+8,0xFFD1BBFF,true);ctx.drawText(textRenderer,"v0.1  /  "+tab,left+panelW-105,top+8,0xFFE5E7EB,false);
        super.render(ctx,mx,my,delta);
        if(tab.equals("Modules")||tab.equals("Blocks"))ctx.drawText(textRenderer,"Page "+(page+1)+(tab.equals("Blocks")?" | "+cfg.blocks.size()+" selected":""),contentX+48,top+panelH-22,0xFFCCCCDD,false);
        if(tab.equals("Scanner")){ctx.drawText(textRenderer,"Reference capture: fresh local world only",contentX,top+158,0xFFFFCA77,false);ctx.drawText(textRenderer,"Gold boxes = reference / not confirmed",contentX,top+170,0xFFFFCA77,false);ctx.drawText(textRenderer,"Loaded: "+scanner.loaded+" | jobs: "+scanner.queue.size(),contentX,top+182,0xFFDDDDDD,false);}
        if(!status.isEmpty()&&panelH>230)ctx.drawText(textRenderer,textRenderer.trimToWidth(status,contentW),contentX,top+panelH-42,0xFFD1BBFF,false);
    }
}
