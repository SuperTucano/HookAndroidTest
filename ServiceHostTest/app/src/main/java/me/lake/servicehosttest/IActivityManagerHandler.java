package me.lake.servicehosttest;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by lake on 17-4-26.
 */

public class IActivityManagerHandler implements InvocationHandler {

    Object mBase;

    public IActivityManagerHandler(Object base) {
        mBase = base;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("startService".equals(method.getName())) {
            Intent originIntent;
            int index = 0;
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Intent) {
                    index = i;
                    break;
                }
            }
            originIntent = (Intent) args[index];

            Intent newIntent = new Intent();
            String targetPackage = "me.lake.servicehosttest";
            ComponentName componentName = new ComponentName(targetPackage, ProxyService.class.getCanonicalName());
            newIntent.setComponent(componentName);
            newIntent.putExtra("extra_target_intent", originIntent);
            args[index] = newIntent;
            Log.d("aa", "hookams,startService");
            return method.invoke(mBase, args);
        }
        if ("stopService".equals(method.getName())) {
            Intent originIntent;
            int index = 0;
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Intent) {
                    index = i;
                    break;
                }
            }
            originIntent = (Intent) args[index];

            if (!"me.lake.servicehosttest".equals(originIntent.getComponent().getPackageName())) {
                //stopService
                ServiceManager.i().stopService(originIntent);
            }
        }
        return method.invoke(mBase, args);
    }
}
