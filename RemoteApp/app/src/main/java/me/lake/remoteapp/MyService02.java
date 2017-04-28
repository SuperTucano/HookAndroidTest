package me.lake.remoteapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MyService02 extends Service {
    public MyService02() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("aa","MyService02,onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("aa","MyService02,onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("aa","MyService02,onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }
}
