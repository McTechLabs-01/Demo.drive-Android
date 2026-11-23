package com.outfitstudios.demodrive;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.FrameLayout;
import android.graphics.*;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;

/** Native Android shell, touch input, and lightweight automotive HUD. */
public final class GameActivity extends Activity {
    DriveView game; HudView hud;
    @Override public void onCreate(Bundle b) { super.onCreate(b); getWindow().getDecorView().setSystemUiVisibility(5894|1024|512); FrameLayout root=new FrameLayout(this); game=new DriveView(this); hud=new HudView(this); root.addView(game); root.addView(hud); setContentView(root); }
    @Override public void onPause(){super.onPause();if(game!=null)game.onPause();}
    @Override public void onResume(){super.onResume();if(game!=null)game.onResume();}
    final class HudView extends View {
        Paint p=new Paint(3); int blue=Color.rgb(44,153,255); float downX,downY; boolean gas,brake,left,right,hand; long lastTap; int menuPage=0,quality=1,paint=0;
        HudView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        void box(Canvas c,float x,float y,float w,float h,int col,float rad){p.setColor(col);c.drawRoundRect(x,y,x+w,y+h,rad,rad,p);}
        @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();
            if(!game.started){p.setColor(Color.argb(235,5,12,23));c.drawRect(0,0,w,h,p);p.setColor(Color.WHITE);p.setTextAlign(Paint.Align.LEFT);p.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));p.setTextSize(h*.095f);c.drawText("DEMO.DRIVE",w*.075f,h*.20f,p);p.setColor(blue);p.setTextSize(h*.031f);String sub=menuPage==0?"OUTFIT STUDIOS   /   TEST FACILITY":new String[]{"","D-01 COUPE  /  GARAGE","MAP SELECT","CHALLENGES","SETTINGS"}[Math.min(4,menuPage)];c.drawText(sub,w*.08f,h*.27f,p);
                if(menuPage==0){String[] labels={"PLAY","GARAGE","MAPS","CHALLENGES","SETTINGS","EXIT"};for(int i=0;i<labels.length;i++){float x=w*.075f+(i%3)*w*.19f,y=h*.39f+(i/3)*h*.18f;box(c,x,y,w*.16f,h*.105f,i==0?blue:0xff17283a,13);p.setColor(Color.WHITE);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(h*.032f);c.drawText(labels[i],x+w*.08f,y+h*.067f,p);}p.setTextAlign(Paint.Align.LEFT);p.setColor(0xffa8bad0);p.setTextSize(h*.023f);c.drawText("D-01 COUPE     •     REAR WHEEL DRIVE     •     TEST FACILITY",w*.08f,h*.82f,p);}
                else {String info=menuPage==1?"Metallic blue D-01 Coupe     |     Paint: "+new String[]{"Ocean Blue","Graphite","Crimson"}[paint]:menuPage==2?"TEST FACILITY     •     Open vehicle proving grounds":menuPage==3?"TIME TRIAL     •     Free drive checkpoint route":("Graphics: "+new String[]{"LOW","MEDIUM","HIGH"}[quality]+"    •    Tap to change quality");p.setColor(Color.WHITE);p.setTextSize(h*.038f);c.drawText(info,w*.08f,h*.47f,p);box(c,w*.08f,h*.60f,w*.22f,h*.11f,blue,14);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(h*.035f);c.drawText(menuPage==4?"CHANGE":"SELECT / DRIVE",w*.19f,h*.67f,p);p.setTextAlign(Paint.Align.LEFT);p.setColor(0xffa8bad0);p.setTextSize(h*.025f);c.drawText("Tap left side to return",w*.08f,h*.79f,p);}return;}
            // Instrument cluster
            box(c,22,h-91,215,67,0xcc07111d,12);p.setColor(Color.WHITE);p.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));p.setTextSize(37);c.drawText(String.format(java.util.Locale.US,"%03d",(int)Math.abs(game.car.speed*3.6)),38,h-47,p);p.setTextSize(13);p.setColor(0xffa8c2dc);c.drawText("KM/H",150,h-52,p);p.setTextSize(12);p.setColor(blue);c.drawText("D   •   "+(int)game.car.rpm+" RPM",39,h-32,p);
            box(c,22,20,185,34,0xaa07111d,8);p.setColor(Color.WHITE);p.setTextSize(14);c.drawText("D-01 COUPE",34,42,p);p.setColor(blue);c.drawRect(115,31,115+62*game.car.condition,39,p);p.setColor(0xffa8c2dc);c.drawText("CONDITION",185,42,p);
            button(c,w-85,20,64,40,"CAM",blue);button(c,w-85,70,64,40,"LIGHT",game.lights?blue:0x99405060);button(c,w-85,120,64,40,"SIGNAL",game.signal?blue:0x99405060);
            button(c,32,h-167,62,55,"◀",left?blue:0x99506070);button(c,102,h-167,62,55,"▶",right?blue:0x99506070);
            button(c,w-170,h-134,76,90,"GO",gas?blue:0x99405060);button(c,w-84,h-134,62,90,"BRAKE",brake?0xffde584e:0x99405060);button(c,w-255,h-98,54,54,"HB",hand?blue:0x99405060);button(c,210,20,64,40,"Ⅱ",0xaa172637);
            p.setColor(0xffa7bed1);p.setTextSize(12);c.drawText("TEST FACILITY  •  FREE DRIVE",w*.5f-95,28,p);
        }
        void button(Canvas c,float x,float y,float w,float h,String s,int color){box(c,x,y,w,h,color,12);p.setColor(Color.WHITE);p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));p.setTextSize(s.length()>4?11:17);c.drawText(s,x+w/2,y+h/2+6,p);p.setTextAlign(Paint.Align.LEFT);}
        @Override public boolean onTouchEvent(android.view.MotionEvent e){float x=e.getX(),y=e.getY(),w=getWidth(),h=getHeight();int a=e.getActionMasked();if(a==MotionEvent.ACTION_DOWN){downX=x;downY=y;if(!game.started){if(menuPage!=0){if(x<w*.42f&&y>h*.52f){menuPage=0;}else if(menuPage==4&&y>.48f*h){quality=(quality+1)%3;}else if(menuPage==1&&y>.48f*h){paint=(paint+1)%3;}else if(y>.48f*h){game.started=true;} }else if(y>.35f*h&&y<.56f*h&&x<w*.27f){game.started=true;game.paused=false;}else if(y>.35f*h&&y<.57f*h){int col=(int)((x-w*.075f)/(w*.19f));int row=y>h*.48f?1:0;int item=Math.min(5,Math.max(0,row*3+col));if(item==1)menuPage=1;else if(item==2)menuPage=2;else if(item==3)menuPage=3;else if(item==4)menuPage=4;else if(item==5)GameActivity.this.finish();}invalidate();return true;}if(x>w-100&&y<66)game.camera=1-game.camera;else if(x>w-100&&y<118)game.lights=!game.lights;else if(x>w-100&&y<170)game.signal=!game.signal;else if(x<95&&y>h-190)left=true;else if(x<175&&y>h-190)right=true;else if(x>w-185&&y>h-145)gas=true;else if(x>w-100&&y>h-145)brake=true;else if(x>w-270&&x<w-190&&y>h-115)hand=true;else if(x>190&&x<290&&y<75)game.paused=!game.paused;else if(x<80&&y<60)game.started=false;invalidate();return true;}if(a==MotionEvent.ACTION_UP||a==MotionEvent.ACTION_CANCEL){left=right=gas=brake=hand=false;invalidate();return true;}return true;}
    }
}
