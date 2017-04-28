package me.lake.loadapktest;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by lake on 17-4-27.
 */

public class ActivityThreadHandlerCallback implements Handler.Callback {
    Handler mBase;

    public ActivityThreadHandlerCallback(Handler base) {
        mBase = base;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 100:
                Log.e("aa", "ActivityThreadHandlerCallback,100");
                handleLaunchActivity(msg);
                break;
        }
        mBase.handleMessage(msg);
        return true;
    }

    private void handleLaunchActivity(Message msg) {
        Object obj = msg.obj;
        try {
            Field intentField = obj.getClass().getDeclaredField("intent");
            intentField.setAccessible(true);
            Intent raw = (Intent) intentField.get(obj);

            Intent target = raw.getParcelableExtra("extra_target_intent");
            if (target != null) {
                Log.e("aa", "target");
                raw.setComponent(target.getComponent());

                Field activityInfoField = obj.getClass().getDeclaredField("activityInfo");
                activityInfoField.setAccessible(true);

                ActivityInfo activityInfo = (ActivityInfo) activityInfoField.get(obj);
                activityInfo.applicationInfo.packageName = target.getPackage() == null ?
                        target.getComponent().getPackageName() : target.getPackage();
                hookPackageManager();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void hookPackageManager() throws Exception {
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
        sPackageManagerField.setAccessible(true);
        Object sPackageManager = sPackageManagerField.get(currentActivityThread);
        Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
        Object proxy = Proxy.newProxyInstance(iPackageManagerInterface.getClassLoader(),
                new Class<?>[]{iPackageManagerInterface},
                new IPackageManagerHookHandler(sPackageManager));

        sPackageManagerField.set(currentActivityThread, proxy);
    }
}
