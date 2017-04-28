package me.lake.loadapktest;

import dalvik.system.DexClassLoader;

/**
 * Created by lake on 17-4-26.
 */

public class MyClassLoader extends DexClassLoader {

    public MyClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }
}
