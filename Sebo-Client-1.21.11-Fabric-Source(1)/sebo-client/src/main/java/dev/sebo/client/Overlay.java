package dev.sebo.client;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import java.util.*;

public final class Overlay {
    private static Matrix4f matrix;
    private static Vec3d camera;
    public static void capture(Vec3d origin,Matrix4f view,Matrix4f projection){camera=origin;matrix=new Matrix4f(projection).mul(view);}
    static void draw(DrawContext ctx,MinecraftClient mc,Config cfg,Scanner scanner){
        if(mc.world==null||mc.player==null||mc.currentScreen!=null||mc.options.hudHidden)return;
        int w=mc.getWindow().getScaledWidth(),h=mc.getWindow().getScaledHeight();
        if(cfg.enabled.contains("coords"))ctx.drawText(mc.textRenderer,"XYZ: "+mc.player.getBlockX()+" / "+mc.player.getBlockY()+" / "+mc.player.getBlockZ(),8,20,0xFFE5E7EB,true);
        if(cfg.enabled.contains("fps"))ctx.drawText(mc.textRenderer,"FPS: "+mc.getCurrentFps(),8,32,0xFFE5E7EB,true);
        if(scanner.colors.isEmpty())return;
        List<Scanner.Display> hits=scanner.display(mc);
        String status=cfg.mode==Config.Mode.LIVE?scanner.loaded+" loaded chunks":cfg.seed.isBlank()?"Enter a seed / capture reference":"reference seed "+cfg.seed;
        ctx.drawText(mc.textRenderer,"SEBO  |  "+cfg.mode+"  |  "+hits.size()+" shown",8,8,0xFFBCA8FF,true);
        ctx.drawText(mc.textRenderer,status,8,44,0xFFBCA8FF,true);
        if(matrix==null||camera==null)return;
        int labels=0,tracers=0;
        for(var hit:hits){BlockPos p=BlockPos.fromLong(hit.hit().pos());
            float left=Float.POSITIVE_INFINITY,right=Float.NEGATIVE_INFINITY,top=Float.POSITIVE_INFINITY,bottom=Float.NEGATIVE_INFINITY;boolean valid=true;
            for(int i=0;i<8;i++){Vector4f v=new Vector4f((float)(p.getX()+(i&1)-camera.x),(float)(p.getY()+((i>>1)&1)-camera.y),(float)(p.getZ()+((i>>2)&1)-camera.z),1);matrix.transform(v);if(v.w<=0.05f){valid=false;break;}float sx=(v.x/v.w+1)*w/2,sy=(1-v.y/v.w)*h/2;left=Math.min(left,sx);right=Math.max(right,sx);top=Math.min(top,sy);bottom=Math.max(bottom,sy);}
            if(!valid||right<0||left>=w||bottom<0||top>=h)continue;
            int x1=Math.clamp((int)left,0,w-1),y1=Math.clamp((int)top,0,h-1),x2=Math.clamp((int)Math.ceil(right),x1+1,w),y2=Math.clamp((int)Math.ceil(bottom),y1+1,h);
            int c=hit.color();ctx.fill(x1,y1,x2,y1+1,c);ctx.fill(x1,y2-1,x2,y2,c);ctx.fill(x1,y1,x1+1,y2,c);ctx.fill(x2-1,y1,x2,y2,c);
            if(cfg.enabled.contains("tracers")&&tracers++<16)line(ctx,w/2,h-1,(x1+x2)/2,(y1+y2)/2,c);
            if(cfg.labels&&labels++<35){int d=(int)Math.sqrt(p.getSquaredDistanceFromCenter(mc.player.getX(),mc.player.getY(),mc.player.getZ()));String name=hit.hit().id().replace("minecraft:","");ctx.drawText(mc.textRenderer,(hit.predicted()?"~ ":"")+name+" "+d+"m",x1,Math.max(0,y1-10),c,true);}
        }
    }
    private static void line(DrawContext ctx,int x,int y,int tx,int ty,int color){int n=Math.max(Math.abs(tx-x),Math.abs(ty-y));if(n==0)return;for(int i=0;i<=n;i+=2){int px=x+(tx-x)*i/n,py=y+(ty-y)*i/n;ctx.fill(px,py,px+1,py+1,color);}}
}
