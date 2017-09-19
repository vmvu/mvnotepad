package com.minhvu.proandroid.sqlite.database.services;

import android.content.Context;
import android.os.PowerManager;

/**
 * Created by vomin on 8/7/2017.
 */

public class LightedGreenRoom {
    private static final String LOGTAG = LightedGreenRoom.class.getSimpleName();

    private int count = 0;
    private Context ctx;
    PowerManager.WakeLock wl = null;
    private int clientCount = 0;

    public LightedGreenRoom(Context context){
        this.ctx = context;
        wl = this.createWakeLock(ctx);
    }

    private static LightedGreenRoom s_self = null;

    public static void setup(Context context){
        if(s_self == null){
            s_self = new LightedGreenRoom(context);
            s_self.turnOnLights();
        }
    }

    public static boolean isSetup(){
        return s_self == null ? false : true;
    }

    synchronized public int enter(){
        count++;
        return count;
    }

    synchronized public int leave(){
        if(count == 0){
            return 0;
        }
        count--;
        //last visitor
        if(count == 0){
            this.turnOffLights();
        }
        return count;
    }

    synchronized public int getCount(){
        return count;
    }

    private void turnOnLights(){
        //turn on lights
        this.wl.acquire();
    }

    private  void turnOffLights(){
        if(this.wl.isHeld()){
            wl.release();
        }
    }

    private PowerManager.WakeLock createWakeLock(Context context){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOGTAG);
        return wl;
    }

    private int registerClient(){
        clientCount++;
        return clientCount;
    }

    private int unregisterClient(){
        if(clientCount == 0){
            return clientCount;
        }
        clientCount--;
        //last visitor
        if(clientCount == 0){
            emptyTheRoom();
        }
        return clientCount;
    }

    synchronized public void emptyTheRoom(){
        count = 0;
        this.turnOffLights();
    }

    public static int s_enter(){
        assertSetup();
        return s_self.enter();
    }

    public static int s_leave(){
        assertSetup();
        return s_self.leave();
    }

    public static void ds_emptyTheRoom(){
        assertSetup();
        s_self.emptyTheRoom();
    }

    public static void s_registerClient(){
        assertSetup();
        s_self.registerClient();
    }
    public static void s_unregisterClient(){
        assertSetup();
        s_self.unregisterClient();
    }

    public static void assertSetup(){
        if(LightedGreenRoom.s_self == null){
            throw new RuntimeException("Need to setup GreenRoom first");
        }
    }


}
