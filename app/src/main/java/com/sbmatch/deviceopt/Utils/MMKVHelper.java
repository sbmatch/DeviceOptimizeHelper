package com.sbmatch.deviceopt.utils;

import com.google.gson.Gson;
import com.tencent.mmkv.MMKV;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MMKVHelper {
    private final MMKV mmkv;
    private final Gson gson;
    private final Map<String, Object> cache; // 缓存

    private MMKVHelper(String mmkvId) {
        mmkv = (mmkvId == null) ? MMKV.defaultMMKV() : MMKV.mmkvWithID(mmkvId);
        gson = new Gson();
        cache = new HashMap<>(); // 初始化缓存
    }

    public static MMKVHelper get(String mmkvId) { return new MMKVHelper(mmkvId);}

    // 保存泛型对象，并更新缓存
    public <T> void saveObject(String key, T object) {
        String json = gson.toJson(object);
        mmkv.encode(key, json);
        cache.put(key, object); // 更新缓存
    }

    // 获取泛型对象，优先从缓存中获取
    public <T> T getObject(String key, Class<T> clazz) {
        // 先从缓存中获取
        if (cache.containsKey(key)) {
            return clazz.cast(cache.get(key));
        }

        // 缓存中不存在，从 MMKV 获取并存入缓存
        String json = mmkv.decodeString(key, "");
        if (json != null && !json.isEmpty()) {
            T object = gson.fromJson(json, clazz);
            cache.put(key, object); // 存入缓存
            return object;
        }
        return null; // 如果数据不存在，返回 null
    }

    // 获取复杂泛型对象（如 List<T> 或 Map<K, V>），优先从缓存中获取
    public <T> T getObject(String key, Type typeOfT) {
        // 先从缓存中获取
        if (cache.containsKey(key)) {
            return (T) cache.get(key);
        }

        // 缓存中不存在，从 MMKV 获取并存入缓存
        String json = mmkv.decodeString(key, "");
        if (json != null && !json.isEmpty()) {
            T object = gson.fromJson(json, typeOfT);
            cache.put(key, object); // 存入缓存
            return object;
        }
        return null; // 如果数据不存在，返回 null
    }

    public <T> T getObject(String key){
        // 先从缓存中获取
        if (cache.containsKey(key)) {
            return (T) cache.get(key);
        }
        // 缓存中不存在，从 MMKV 获取并存入缓存
        String json = mmkv.decodeString(key, "");
        if (json != null && !json.isEmpty()) {
            cache.put(key, json); // 存入缓存
            return (T) json;
        }
        return null;
    }

    // 清除缓存中的某个键值
    public void clearCache(String key) {
        cache.remove(key);
    }

    // 清除所有缓存
    public void clearAllCache() {
        cache.clear();
    }
}
