package me.lake.loadapktest;

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
        if ("startActivity".equals(method.getName())) {
            Intent raw;
            int index = 0;
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Intent) {
                    index = i;
                    break;
                }
            }
            raw = (Intent) args[index];

            Intent newIntent = new Intent();
            String targetPackage = "me.lake.loadapktest";
            ComponentName componentName = new ComponentName(targetPackage, StubActivity.class.getCanonicalName());
            newIntent.setComponent(componentName);
            newIntent.putExtra("extra_target_intent", raw);
            args[index] = newIntent;
            Log.d("aa", "hookams,startActivity");
            return method.invoke(mBase, args);
        }
        return method.invoke(mBase, args);
    }
}
