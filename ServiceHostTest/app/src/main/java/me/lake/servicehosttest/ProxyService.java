package me.lake.servicehosttest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by lake on 17-4-28.
 */

public class ProxyService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.e("aa","ProxyService,onStart");
        ServiceManager.i().onStart(intent,startId);
        super.onStart(intent, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("aa","ProxyService,onCreate");
    }

    @Override
    public void onDestroy() {
        Log.e("aa","ProxyService,onDestroy");
        super.onDestroy();
    }
}
