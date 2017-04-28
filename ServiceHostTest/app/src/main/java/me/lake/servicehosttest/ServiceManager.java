package me.lake.servicehosttest;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lake on 17-4-28.
 */

public class ServiceManager {
    private static volatile ServiceManager instance;
    private Map<ComponentName, ServiceInfo> mServiceInfoMap = new HashMap<ComponentName, ServiceInfo>();

    private Map<String, Service> mServiceMap = new HashMap<String, Service>();

    public synchronized static ServiceManager i() {
        return instance == null ? instance = new ServiceManager() : instance;
    }

    public void onStart(Intent proxyIntent, int startId) {
        Log.e("aa","onStart"+android.os.Process.myPid());
        Intent targetIntent = proxyIntent.getParcelableExtra("extra_target_intent");
        ServiceInfo serviceInfo = selectPluginService(targetIntent);
        if (serviceInfo == null) {
            Log.e("aa", "can not found service:" + targetIntent.getComponent());
            return;
        }
        try {
            if (!mServiceMap.containsKey(serviceInfo.name)) {
                proxyCreateService(serviceInfo);
            }
            Service service = mServiceMap.get(serviceInfo.name);
            service.onStart(targetIntent, startId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public int stopService(Intent targetIntent) {
        Log.e("aa","stopService"+android.os.Process.myPid());
        ServiceInfo serviceInfo = selectPluginService(targetIntent);
        if (serviceInfo == null) {
            Log.e("aa", "stopService,can`t find service:" + targetIntent.getComponent());
            return 0;
        }
        Service service = mServiceMap.get(serviceInfo.name);
        if (service == null) {
            Log.e("aa", "mServciceMap can`t find " + serviceInfo.name);
            return 0;
        }
        service.onDestroy();

        mServiceMap.remove(serviceInfo.name);
        if (mServiceMap.isEmpty()) {
            Log.e("aa", "service all stop,stop proxy");
            Context appContext = MyApplicatioon.getContext();
            appContext.stopService(new Intent().setComponent(
                    new ComponentName(appContext.getPackageName(),
                            ProxyService.class.getCanonicalName())));
        }
        return 1;
    }

    public void preLoadServices(File apkFile) throws Exception {
        Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
        Method parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File.class, int.class);
        Object packageParser = packageParserClass.newInstance();
        Object packageObj = parsePackageMethod.invoke(packageParser, apkFile, PackageManager.GET_SERVICES);

        Field servicesField = packageObj.getClass().getDeclaredField("services");
        List services = (List) servicesField.get(packageObj);

        Class<?> packageParser$ServiceClass = Class.forName("android.content.pm.PackageParser$Service");
        Class<?> packageUserStateClass = Class.forName("android.content.pm.PackageUserState");
        Class<?> userHandler = Class.forName("android.os.UserHandle");
        Method getCallingUserIdMethod = userHandler.getDeclaredMethod("getCallingUserId");
        int userId = (int) getCallingUserIdMethod.invoke(null);
        Object defaultUserState = packageUserStateClass.newInstance();

        Method generateReceiverInfo = packageParserClass.getDeclaredMethod("generateServiceInfo",
                packageParser$ServiceClass, int.class, packageUserStateClass, int.class);

        for (Object service : services) {
            ServiceInfo info = (ServiceInfo) generateReceiverInfo.invoke(packageParser, service, 0, defaultUserState, userId);
            mServiceInfoMap.put(new ComponentName(info.packageName, info.name), info);
        }
    }

    private ServiceInfo selectPluginService(Intent pluginIntent) {
        for (ComponentName componentName : mServiceInfoMap.keySet()) {
            if (componentName.equals(pluginIntent.getComponent())) {
                Log.e("aa", "component=" + mServiceInfoMap.get(pluginIntent.getComponent()));
                return mServiceInfoMap.get(componentName);
            }
        }
        return null;
    }

    private void proxyCreateService(ServiceInfo serviceInfo) throws Exception {
        hookPackageManager();
        IBinder token = new Binder();

        Class<?> createServiceDataClass = Class.forName("android.app.ActivityThread$CreateServiceData");
        Constructor<?> constructor = createServiceDataClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object createServiceData = constructor.newInstance();

        Field tokenField = createServiceDataClass.getDeclaredField("token");
        tokenField.setAccessible(true);
        tokenField.set(createServiceData, token);

        Field infoField = createServiceDataClass.getDeclaredField("info");
        infoField.setAccessible(true);
        infoField.set(createServiceData, serviceInfo);

        Class<?> compatibilityClass = Class.forName("android.content.res.CompatibilityInfo");
        Field defaultCompatibilityField = compatibilityClass.getDeclaredField("DEFAULT_COMPATIBILITY_INFO");
        Object defaultCompatibility = defaultCompatibilityField.get(null);
        Field compatInfoField = createServiceDataClass.getDeclaredField("compatInfo");
        compatInfoField.setAccessible(true);
        compatInfoField.set(createServiceData, defaultCompatibility);

        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        Method handleCreateServiceMethod = activityThreadClass.getDeclaredMethod("handleCreateService", createServiceDataClass);
        handleCreateServiceMethod.setAccessible(true);

        handleCreateServiceMethod.invoke(currentActivityThread, createServiceData);

        Field mServicesFiled = activityThreadClass.getDeclaredField("mServices");
        mServicesFiled.setAccessible(true);
        Map mServices = (Map) mServicesFiled.get(currentActivityThread);
        Service service = (Service) mServices.get(token);

        mServices.remove(token);

        mServiceMap.put(serviceInfo.name, service);

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
