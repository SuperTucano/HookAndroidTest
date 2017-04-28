package me.lake.servicehosttest;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Created by lake on 17-4-28.
 */

public class MyApplicatioon extends Application {
    private static String ApkPath = "/sdcard/RemoteApp.apk";
    private static Context context;
    Object loadapks;

    public static Context getContext() {
        return context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        context = base;
        super.attachBaseContext(base);
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = new FileInputStream(ApkPath);
            File extractFile = base.getFileStreamPath("RemoteApp.apk");
            fos = new FileOutputStream(extractFile);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.closeSilently(is);
            Utils.closeSilently(fos);
        }

        try {
            hookActivityManagerNative();
            File apkFile = base.getFileStreamPath("RemoteApp.apk");
            //获取mPackages
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);
            Field mPackagesField = activityThreadClass.getDeclaredField("mPackages");
            mPackagesField.setAccessible(true);
            Map mPackages = (Map) mPackagesField.get(currentActivityThread);

            //获取LoadedApk
            Class<?> compatibilityInfoClass = Class.forName("android.content.res.CompatibilityInfo");
            Method getPackageInfoNoCheckMethod = activityThreadClass.getDeclaredMethod("getPackageInfoNoCheck",
                    ApplicationInfo.class,
                    compatibilityInfoClass);
            Field defaultCompatibilityInfoField = compatibilityInfoClass.getDeclaredField("DEFAULT_COMPATIBILITY_INFO");
            defaultCompatibilityInfoField.setAccessible(true);
            Object defaultCompatibilityInfo = defaultCompatibilityInfoField.get(null);

            //获取generateApplicationInfo
            Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
            Class<?> packageParser$PackageClass = Class.forName("android.content.pm.PackageParser$Package");
            Class<?> packageUserStateClass = Class.forName("android.content.pm.PackageUserState");
            Method generateApplicationInfoMethod = packageParserClass.getDeclaredMethod("generateApplicationInfo",
                    packageParser$PackageClass,
                    int.class,
                    packageUserStateClass);
            //获取Package
            Object packageParser = packageParserClass.newInstance();
            Method parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File.class, int.class);
            Object packageObj = parsePackageMethod.invoke(packageParser, apkFile, 0);
            //获取PackageUserState
            Object defaultPackageUserState = packageUserStateClass.newInstance();
            //构造ApplicationInfo
            ApplicationInfo applicationInfo = (ApplicationInfo) generateApplicationInfoMethod.invoke(packageParser,
                    packageObj, 0, defaultPackageUserState);
            applicationInfo.sourceDir = apkFile.getPath();
            applicationInfo.publicSourceDir = apkFile.getPath();

            Object loadedApk = getPackageInfoNoCheckMethod.invoke(currentActivityThread, applicationInfo, defaultCompatibilityInfo);

            String odexPath = Utils.getPluginOptDexDir(base, applicationInfo.packageName).getPath();
            String libDir = Utils.getPluginLibDir(base, applicationInfo.packageName).getPath();
            ClassLoader classLoader = new MyClassLoader(apkFile.getPath(), odexPath, libDir, ClassLoader.getSystemClassLoader());
            //将LoadedApk放到mPackages
            Field mClassLoaderField = loadedApk.getClass().getDeclaredField("mClassLoader");
            mClassLoaderField.setAccessible(true);
            mClassLoaderField.set(loadedApk, classLoader);
            loadapks = loadedApk;
            WeakReference weakReference = new WeakReference(loadedApk);
            mPackages.put(applicationInfo.packageName, weakReference);

            ServiceManager.i().preLoadServices(apkFile);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void hookActivityManagerNative() throws Exception {
        Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
        Field gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
        gDefaultField.setAccessible(true);
        Object gDefault = gDefaultField.get(null);

        Class<?> singleton = Class.forName("android.util.Singleton");
        Field mInstanceField = singleton.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);

        Object rawIActivityManager = mInstanceField.get(gDefault);

        Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{iActivityManagerInterface},
                new IActivityManagerHandler(rawIActivityManager));
        mInstanceField.set(gDefault, proxy);
        Log.e("aa", "hookActivityManagerNative");
    }
}
