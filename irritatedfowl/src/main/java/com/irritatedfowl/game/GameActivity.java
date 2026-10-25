package com.irritatedfowl.game;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class GameActivity extends Activity {
    @Override public void onCreate(Bundle b) { super.onCreate(b); getWindow().getDecorView().setSystemUiVisibility(5894 | 1024 | 512); setContentView(new GameView()); }

    static class GameView extends View {
        final Paint p = new Paint(3);
        final Random random = new Random();
        final ArrayList<Body> bodies = new ArrayList<>();
        float scale=1, offX=0, offY=0;
        final float slingX=155, slingY=340;
        float birdX=slingX, birdY=slingY, vx=0, vy=0, dragX=slingX, dragY=slingY;
        boolean dragging=false, flying=false, won=false;
        int level=1, score=0, birds=3;
        long last=System.currentTimeMillis();
        final float ground=435;
        static class Body { float x,y,w,h,vx,vy; int kind; boolean alive=true; Body(float X,float Y,float W,float H,int K){x=X;y=Y;w=W;h=H;kind=K;} }

        GameView() { super(GameActivity.this); setLayerType(View.LAYER_TYPE_SOFTWARE,null); setupLevel(); }
        void setupLevel() {
            bodies.clear(); won=false; flying=false; birds=3; birdX=slingX; birdY=slingY;
            int base=level%3;
            float tx=620 + base*20;
            bodies.add(new Body(tx,ground-36,22,72,1));
            bodies.add(new Body(tx+66,ground-36,22,72,1));
            bodies.add(new Body(tx+33,ground-95,112,18,1));
            bodies.add(new Body(tx+8,ground-143,22,78,1));
            bodies.add(new Body(tx+58,ground-143,22,78,1));
            bodies.add(new Body(tx+33,ground-191,112,18,1));
            for(int i=0;i<1+(level%2);i++) bodies.add(new Body(tx+33,ground-222-i*27,38,27,2));
            if(level>1) bodies.add(new Body(tx+95,ground-35,20,70,1));
            invalidate();
        }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c); float sx=getWidth()/900f, sy=getHeight()/500f; scale=Math.min(sx,sy); offX=(getWidth()-900*scale)/2; offY=(getHeight()-500*scale)/2;
            c.save(); c.translate(offX,offY); c.scale(scale,scale);
            p.setShader(new LinearGradient(0,0,0,500,Color.rgb(97,190,238),Color.rgb(224,244,255),Shader.TileMode.CLAMP)); c.drawRect(0,0,900,500,p); p.setShader(null);
            p.setColor(Color.WHITE); for(int i=0;i<5;i++){float x=(i*187+40)%900; c.drawOval(x,70+(i%3)*32,x+70,94+(i%3)*32,p);}
            p.setColor(Color.rgb(100,184,86)); c.drawRect(0,ground,900,500,p); p.setColor(Color.rgb(63,135,65)); c.drawRect(0,ground,900,ground+9,p);
            // Distant hills
            p.setColor(Color.rgb(127,204,127)); Path hill=new Path(); hill.moveTo(0,ground); hill.quadTo(180,320,330,ground); hill.quadTo(520,340,700,ground); hill.lineTo(900,ground); hill.close(); c.drawPath(hill,p);
            // Ground and slingshot
            p.setColor(Color.rgb(115,73,39)); c.drawRoundRect(128,ground-90,143,ground+4,6,6,p); c.drawRoundRect(164,ground-90,179,ground+4,6,6,p);
            p.setColor(Color.rgb(83,49,27)); c.drawRoundRect(130,ground-96,145,ground-40,7,7,p); c.drawRoundRect(162,ground-96,177,ground-40,7,7,p);
            if(dragging){p.setColor(Color.rgb(87,53,31));p.setStrokeWidth(5);c.drawLine(137,ground-80,dragX,dragY,p);c.drawLine(170,ground-80,dragX,dragY,p); drawTrajectory(c);}
            else if(!flying){p.setColor(Color.rgb(87,53,31));p.setStrokeWidth(5);c.drawLine(137,ground-80,birdX,birdY,p);c.drawLine(170,ground-80,birdX,birdY,p);}
            for(Body b:bodies) if(b.alive) drawBody(c,b);
            if(!won || flying || dragging) drawBird(c,birdX,birdY,20);
            // HUD
            p.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD)); p.setTextSize(26); p.setColor(Color.rgb(25,43,59)); c.drawText("IRRITATED FOWL",24,38,p);
            p.setTextSize(18); c.drawText("LEVEL "+level+"    SCORE "+score+"    BIRDS "+birds,24,64,p);
            p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD)); p.setTextSize(15); p.setColor(Color.rgb(29,53,67));
            if(!flying&&!won) c.drawText("PULL BACK, AIM, RELEASE!",330,42,p);
            if(won){panel(c,"LEVEL CLEARED!","Tap to launch the next level");}
            else if(birds<=0&&!flying){panel(c,"OUT OF BIRDS!","Tap to try this level again");}
            p.setTextSize(13);p.setColor(Color.rgb(40,70,76));c.drawText("Tap anywhere after a shot to reset • Drag bird to aim",490,478,p);
            c.restore();
            if(flying) { long now=System.currentTimeMillis(); float dt=Math.min(.035f,(now-last)/1000f); last=now; update(dt); postInvalidateDelayed(16); }
        }
        void panel(Canvas c,String a,String b){p.setColor(0xDDFFFFFF);c.drawRoundRect(260,170,640,280,22,22,p);p.setColor(Color.rgb(32,53,68));p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));p.setTextSize(30);c.drawText(a,450,215,p);p.setTextSize(17);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText(b,450,246,p);p.setTextAlign(Paint.Align.LEFT);}
        void drawTrajectory(Canvas c){p.setColor(0xAAFFFFFF);for(int i=1;i<=10;i++){float t=i*.16f;float x=dragX+(slingX-dragX)*3.2f*t;float y=dragY+(slingY-dragY)*3.2f*t+0.5f*430*t*t;p.setColor(0xAAFFFFFF);c.drawCircle(x,y,3,p);}}
        void drawBird(Canvas c,float x,float y,float r){p.setColor(Color.rgb(226,64,50));c.drawCircle(x,y,r,p);p.setColor(Color.rgb(255,225,198));c.drawOval(x-10,y+4,x+11,y+16,p);p.setColor(Color.WHITE);c.drawCircle(x+5,y-5,7,p);c.drawCircle(x-7,y-5,6,p);p.setColor(Color.rgb(30,35,40));c.drawCircle(x+7,y-5,2.7f,p);c.drawCircle(x-5,y-5,2.5f,p);p.setColor(Color.rgb(255,191,48));Path beak=new Path();beak.moveTo(x+8,y+1);beak.lineTo(x+24,y+5);beak.lineTo(x+8,y+9);beak.close();c.drawPath(beak,p);p.setColor(Color.rgb(65,36,34));p.setStrokeWidth(3);c.drawLine(x-12,y-15,x-1,y-12,p);}
        void drawBody(Canvas c,Body b){if(b.kind==2){p.setColor(Color.rgb(89,170,69));c.drawRoundRect(b.x-b.w/2,b.y-b.h/2,b.x+b.w/2,b.y+b.h/2,10,10,p);p.setColor(Color.WHITE);c.drawCircle(b.x-7,b.y-3,5,p);c.drawCircle(b.x+7,b.y-3,5,p);p.setColor(Color.BLACK);c.drawCircle(b.x-6,b.y-2,2,p);c.drawCircle(b.x+8,b.y-2,2,p);p.setColor(Color.rgb(60,105,48));c.drawOval(b.x-8,b.y+5,b.x+8,b.y+9,p);}else{p.setColor(Color.rgb(192,135,76));c.drawRoundRect(b.x-b.w/2,b.y-b.h/2,b.x+b.w/2,b.y+b.h/2,4,4,p);p.setColor(Color.rgb(234,194,132));p.setStrokeWidth(2);c.drawLine(b.x-b.w/2+3,b.y,b.x+b.w/2-3,b.y,p);}}
        void update(float dt){birdX+=vx*dt;birdY+=vy*dt;vy+=430*dt;
            if(birdY>ground-20){birdY=ground-20;finishShot();}
            for(Body b:bodies)if(b.alive){float dx=birdX-b.x,dy=birdY-b.y;if(Math.abs(dx)<b.w/2+17&&Math.abs(dy)<b.h/2+17){b.alive=false;score+=b.kind==2?500:100;vx*=.68f;vy=-Math.abs(vy)*.35f;}}
            if(birdX>940||birdY>530||birdX< -40)finishShot();
        }
        void finishShot(){flying=false;birdX=slingX;birdY=slingY;dragX=slingX;dragY=slingY;int targets=0;for(Body b:bodies)if(b.alive&&b.kind==2)targets++;if(targets==0)won=true;else birds--;invalidate();}
        @Override public boolean onTouchEvent(android.view.MotionEvent e){float x=(e.getX()-offX)/scale,y=(e.getY()-offY)/scale;
            switch(e.getAction()){
                case MotionEvent.ACTION_DOWN:
                    if(won){level++;setupLevel();return true;} if(birds<=0&&!flying){setupLevel();return true;}
                    if(!flying&&Math.hypot(x-birdX,y-birdY)<65){dragging=true;dragX=x;dragY=y;invalidate();} else if(!flying&&birds<3){setupLevel();}
                    return true;
                case MotionEvent.ACTION_MOVE: if(dragging){dragX=Math.max(85,Math.min(slingX+18,x));dragY=Math.max(ground-135,Math.min(ground-35,y));birdX=dragX;birdY=dragY;invalidate();}return true;
                case MotionEvent.ACTION_UP: if(dragging){dragging=false;float dx=slingX-dragX,dy=slingY-dragY;if(Math.hypot(dx,dy)>12){vx=dx*3.25f;vy=dy*3.25f;flying=true;last=System.currentTimeMillis();postInvalidateOnAnimation();}else{birdX=slingX;birdY=slingY;}invalidate();}return true;
            }return true;
        }
    }
}
