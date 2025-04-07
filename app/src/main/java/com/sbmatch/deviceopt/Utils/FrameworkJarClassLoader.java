package com.sbmatch.deviceopt.utils;

import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;
public class FrameworkJarClassLoader {

    private static final FrameworkJarClassLoader INSTANCE = new FrameworkJarClassLoader();
    private static final Map<String, DexClassLoader> dexClassLoaders = new HashMap<>();

    private FrameworkJarClassLoader() {
    }

    public static FrameworkJarClassLoader getInstance() {
        return INSTANCE;
    }

    public Class<?> findClass(String jarPath, String name) {

        DexClassLoader loader = dexClassLoaders.get(jarPath);
        if (loader == null) {
            loader = new DexClassLoader(jarPath, null, null, ClassLoader.getSystemClassLoader());
            dexClassLoaders.put(jarPath, loader);
        }

        try {
            Class<?> clazz = loader.loadClass(name);
            dexClassLoaders.put(name, loader);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.fillInStackTrace());
        }
    }
}