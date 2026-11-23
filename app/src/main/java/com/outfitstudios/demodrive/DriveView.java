package com.outfitstudios.demodrive;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLES20;
import android.opengl.Matrix;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/** Small custom OpenGL ES renderer and four-corner spring/tire vehicle model. */
final class DriveView extends GLSurfaceView implements GLSurfaceView.Renderer {
    final GameActivity activity; final Car car=new Car(); volatile boolean started=false,paused=false,lights=false,signal=false; volatile int camera=0;
    final float[] proj=new float[16],view=new float[16],model=new float[16],vp=new float[16]; int program,pos,color; FloatBuffer cube;
    long last=System.nanoTime(); float time=0;
    DriveView(Context c){super(c);activity=(GameActivity)c;setEGLContextClientVersion(2);setRenderer(this);setRenderMode(RENDERMODE_CONTINUOUSLY);}
    @Override public void onSurfaceCreated(GL10 gl,EGLConfig cfg){String vs="attribute vec3 a;uniform mat4 mvp;varying vec3 n;void main(){gl_Position=mvp*vec4(a,1.0);n=a;}";String fs="precision mediump float;uniform vec4 tint;varying vec3 n;void main(){float l=.65+.35*max(0.0,dot(normalize(n),normalize(vec3(.3,.8,.4))));gl_FragColor=vec4(tint.rgb*l,tint.a);}";int v=shader(GLES20.GL_VERTEX_SHADER,vs),f=shader(GLES20.GL_FRAGMENT_SHADER,fs);program=GLES20.glCreateProgram();GLES20.glAttachShader(program,v);GLES20.glAttachShader(program,f);GLES20.glLinkProgram(program);pos=GLES20.glGetAttribLocation(program,"a");color=GLES20.glGetUniformLocation(program,"tint");float[] q={-1,-1,-1,1,-1,-1,1,1,-1,-1,1,-1,-1,-1,1,1,-1,1,1,1,1,-1,1,1,-1,-1,-1,-1,1,-1,-1,1,1,-1,-1,1,1,-1,-1,1,-1,1,1,1,1,1,1,-1,1,-1,-1,-1,1,-1,-1,1,1,-1,1,-1,-1,-1,1,1,-1,1,1,1,-1,1,-1,-1,1,-1,1,1,-1,1,1,1,1,-1,1};cube=ByteBuffer.allocateDirect(q.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();cube.put(q).position(0);GLES20.glEnable(GLES20.GL_DEPTH_TEST);GLES20.glClearColor(.52f,.70f,.82f,1);}
    int shader(int type,String src){int s=GLES20.glCreateShader(type);GLES20.glShaderSource(s,src);GLES20.glCompileShader(s);return s;}
    @Override public void onSurfaceChanged(GL10 gl,int w,int h){GLES20.glViewport(0,0,w,h);Matrix.perspectiveM(proj,0,58,(float)w/h,.1f,500f);}
    @Override public void onDrawFrame(GL10 gl){long now=System.nanoTime();float dt=Math.min(.04f,(now-last)/1e9f);last=now;time+=dt;if(started&&!paused){GameActivity.HudView hud=activity.hud;car.update(dt,hud.gas,hud.brake,hud.left,hud.right,hud.hand);}GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
        float sn=(float)Math.sin(car.yaw),cs=(float)Math.cos(car.yaw);float cx=car.x,cz=car.z;
        if(camera==0)Matrix.setLookAtM(view,0,cx-sn*8,2.8f,cz-cs*8,cx,0.8f,cz+2*cs,0,1,0);
        else Matrix.setLookAtM(view,0,cx+sn*.15f,1.48f,cz+cs*.2f,cx+sn*20,1.4f,cz+22*cs,0,1,0);
        Matrix.multiplyMM(vp,0,proj,0,view,0);
        // Test facility base, infield, and marked asphalt loop.
        draw(0,-.32f,0,230,.25f,230,0xff738252);draw(0,-.18f,0,90,.12f,90,0xff4d5259);
        draw(0,-.10f,0,72,.10f,72,0xff272d34);
        // lane markings form a broad rounded test circuit
        for(int i=0;i<48;i++){double a=i*Math.PI/24;float x=(float)Math.cos(a)*32,z=(float)Math.sin(a)*32;draw(x,-.075f,z,.22f,.018f,.8f,0xffd5d8c6);}
        // facility aprons, ramps, garages, and sparse stylized vegetation
        draw(-53,-.03f,0,13,.10f,42,0xff30363d);draw(53,-.03f,0,13,.10f,42,0xff30363d);
        draw(-14,.35f,-36,8,.9f,7,0xff727b86);draw(-14,1.35f,-36,8,1.1f,7,0xff527d99);
        draw(18,.55f,-42,7,1.5f,13,0xff303d4a);draw(18,2.15f,-42,7,.12f,13,0xff4993be);
        for(int i=0;i<14;i++){float x=((i*37)%130)-65,z=((i*53)%130)-65;if(Math.abs(x)<40&&Math.abs(z)<40)continue;draw(x,.85f,z,.35f,1.7f,.35f,0xff564735);draw(x,2.1f,z,1.2f,1.4f,1.2f,0xff3e7445);}
        // car body and cabin rotate as one; wheels retain independent spring travel and front steer.
        float lift=car.suspension;drawRot(cx,.58f+lift,cz,2.0f,.48f,4.15f,0xff1979c6,car.yaw);
        drawRot(cx,1.0f+lift,cz+.18f,1.65f,.48f,1.95f,0xff101820,car.yaw);
        drawRot(cx,1.0f+lift,cz-.05f,.72f,.37f,1.30f,0xff56768a,car.yaw);
        for(int k=0;k<4;k++){float wx=(k%2==0?-1:1)*.89f,wz=(k<2?1.32f:-1.38f);float wob=car.wheelY[k];drawRot(cx+wx*cs+wz*sn,.32f+wob,cz-wx*sn+wz*cs,.30f,.52f,.20f,k<2?0xff101318:0xff16191d,car.yaw+(k<2?car.steer*.62f:0));drawRot(cx+wx*cs+wz*sn,.32f+wob,cz-wx*sn+wz*cs,.20f,.24f,.22f,0xff9aa8b4,car.yaw);}
        drawRot(cx-.73f*cs+1.8f*sn,.58f,cz+.73f*sn+1.8f*cs,.2f,.12f,.05f,0xffeef1e6,car.yaw);drawRot(cx+.73f*cs+1.8f*sn,.58f,cz-.73f*sn+1.8f*cs,.2f,.12f,.05f,lights?0xffffffdd:0xffa84942,car.yaw);
    }
    void draw(float x,float y,float z,float sx,float sy,float sz,int rgb){drawRot(x,y,z,sx,sy,sz,rgb,0);}
    void drawRot(float x,float y,float z,float sx,float sy,float sz,int rgb,float ry){Matrix.setIdentityM(model,0);Matrix.translateM(model,0,x,y,z);Matrix.rotateM(model,0,(float)Math.toDegrees(ry),0,1,0);Matrix.scaleM(model,0,sx,sy,sz);float[] m=new float[16];Matrix.multiplyMM(m,0,vp,0,model,0);GLES20.glUseProgram(program);GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program,"mvp"),1,false,m,0);GLES20.glUniform4f(color,((rgb>>16)&255)/255f,((rgb>>8)&255)/255f,(rgb&255)/255f,1);GLES20.glEnableVertexAttribArray(pos);GLES20.glVertexAttribPointer(pos,3,GLES20.GL_FLOAT,false,0,cube);GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,24);}
    static final class Car {
        float x=0,z=0,yaw=0,speed=0,rpm=900,condition=1,steer=0,suspension=0,vx=0,vz=0,yawRate=0;
        final float[] wheelY=new float[4],springV=new float[4],springTravel=new float[]{.16f,.16f,.16f,.16f},omega=new float[4];
        void update(float dt,boolean gas,boolean brake,boolean left,boolean right,boolean hand){
            float target=(left?-1:0)+(right?1:0);steer+=(target-steer)*Math.min(1,dt*4.2f);
            float sn=(float)Math.sin(yaw),cs=(float)Math.cos(yaw),forwardX=sn,forwardZ=cs;
            // Four independent spring/damper contacts and friction-limited tire forces.
            float forceX=0,forceZ=0,torque=0,loadSum=0;float throttle=gas?1:(brake&&Math.abs(speed)<1f?-.45f:0);float brakeInput=brake&&Math.abs(speed)>=1f?1:0;
            for(int i=0;i<4;i++){
                float lx=(i%2==0?-.88f:.88f), lz=(i<2?1.30f:-1.35f);
                float rx=lx*cs+lz*sn, rz=-lx*sn+lz*cs;
                float cornerX=x+rx,cornerZ=z+rz;
                float road=(float)Math.sin(cornerX*.10+cornerZ*.08)*.045f;
                float targetCompression=.16f-road;
                float spring=21000f*(targetCompression-springTravel[i])-1750f*springV[i];
                springV[i]+=spring/342.5f*dt;springTravel[i]+=springV[i]*dt;
                if(springTravel[i]<.035f){springTravel[i]=.035f;springV[i]=Math.max(0,springV[i]);}if(springTravel[i]>.30f){springTravel[i]=.30f;springV[i]=Math.min(0,springV[i]);}
                wheelY[i]=(.16f-springTravel[i])*.45f;
                float load=Math.max(120,342.5f*9.81f+spring);loadSum+=load;
                float cornerVX=vx-yawRate*rz,cornerVZ=vz+yawRate*rx;
                float wheelYaw=yaw+(i<2?steer*.50f:0);float wfX=(float)Math.sin(wheelYaw),wfZ=(float)Math.cos(wheelYaw),wrX=(float)Math.cos(wheelYaw),wrZ=-(float)Math.sin(wheelYaw);
                float longitudinal=cornerVX*wfX+cornerVZ*wfZ, lateral=cornerVX*wrX+cornerVZ*wrZ;
                omega[i]+=(i>=2?throttle*24:0)-omega[i]*.08f-(brakeInput?Math.signum(omega[i])*30:0);
                float fx=(i>=2?Math.max(-2800,Math.min(6100,throttle*6100)):0)+(brakeInput?-Math.signum(longitudinal)*Math.min(7600,Math.abs(longitudinal)*1450):0);
                float fy=-lateral*1250f*(hand?.42f:1f);
                float cap=load*(hand?.48f:1.12f),mag=(float)Math.sqrt(fx*fx+fy*fy);if(mag>cap){fx*=cap/mag;fy*=cap/mag;}
                float ax=wfX*fx+wrX*fy,az=wfZ*fx+wrZ*fy;forceX+=ax;forceZ+=az;torque+=rx*az-rz*ax;
            }
            // Rear-wheel engine drag, aerodynamic drag, and chassis yaw inertia.
            forceX-=vx*(Math.abs(speed)*5+135);forceZ-=vz*(Math.abs(speed)*5+135);
            vx+=forceX/1370f*dt;vz+=forceZ/1370f*dt;yawRate+=torque/2450f*dt;
            yawRate*=Math.max(0,1-dt*(hand?1.5f:2.8f));x+=vx*dt;z+=vz*dt;yaw+=yawRate*dt;
            speed=vx*forwardX+vz*forwardZ;
            if(speed < -2.6f) { // reverse is deliberately speed-limited
                float f= -2.6f/speed;vx*=f;vz*=f;speed=-2.6f;
            }
            // Simple rigid barriers at the test grounds perimeter and a ramp apron.
            if(Math.abs(x)>82){x=Math.signum(x)*82;vx*=-.32f;damage(.018f);}
            if(Math.abs(z)>82){z=Math.signum(z)*82;vz*=-.32f;damage(.018f);}
            if(Math.abs(x+14)<4.5f&&z < -32&&z>-40){vx*=.62f;damage(.0008f);}
            suspension=wheelY[0]+wheelY[1]+wheelY[2]+wheelY[3];suspension*=.25f;
            rpm=900+Math.min(6500,Math.abs(speed)*130+throttle*1100);
        }
        void damage(float d){condition=Math.max(.05f,condition-d);}
    }
}
