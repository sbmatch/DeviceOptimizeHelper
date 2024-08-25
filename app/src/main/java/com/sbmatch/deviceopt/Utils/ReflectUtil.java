//
// Decompiled by Jadx - 540ms
//
package com.sbmatch.deviceopt.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectUtil {
    private static final Map<Class<?>, Map<String, Map<String, Field>>> prefixMatchCache = new ConcurrentHashMap<>();

    private static Map<String, Field> cacheFields(Class<?> clazz) {
        Map<String, Field> map = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            map.put(field.getName(), field);
        }
        return map;
    }
    public static Map<String, Object> getFieldsByPrefixMatch(Class<?> clz, String pattern) {
        // 获取类对应的前缀匹配缓存
        Map<String, Map<String, Field>> classFuzzyMatchCache = prefixMatchCache.computeIfAbsent(clz, k -> new HashMap<>());

        // 从缓存中获取匹配的字段
        Map<String, Field> patternFields = classFuzzyMatchCache.get(pattern);
        if (patternFields != null) {
            return getFieldValueMap(patternFields);
        }

        // 如果缓存中没有，则进行前缀匹配
        Map<String, Field> fields = cacheFields(clz);
        patternFields = new HashMap<>();
        for (Map.Entry<String, Field> entry : fields.entrySet()) {
            if (entry.getKey().startsWith(pattern)) {
                patternFields.put(entry.getKey(), entry.getValue());
            }
        }
        classFuzzyMatchCache.put(pattern, patternFields);

        // 获取字段的值并返回
        return getFieldValueMap(patternFields);
    }

    private static Map<String, Object> getFieldValueMap(Map<String, Field> fields) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Field> entry : fields.entrySet()) {
            Field field = entry.getValue();
            field.setAccessible(true);
            try {
                result.put(entry.getKey(), field.get(null)); // 假设是静态字段
            }catch (IllegalAccessException e){
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private static Object callObjectMethod(Object obj, String methodName, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method declaredMethod = Arrays.stream(obj.getClass().getDeclaredMethods())
                .filter(method -> methodName.equals(method.getName()))
                .findFirst()
                .orElse(null);

        // 检查方法是否找到
        if (declaredMethod == null) {
            throw new NoSuchMethodException("Method " + methodName + " not found in " + obj.getClass().getName());
        }

        declaredMethod.setAccessible(true);
        return declaredMethod.invoke(obj, args);
    }


    /**
     * 反射调用方法，并返回结果
     *
     * @param obj        目标类的实例对象
     * @param methodName 方法名
     * @param args       方法参数
     * @return 方法的返回值
     *
     */
    public static Object callObjectMethod2(Object obj, String methodName, Object... args)  {
        try {
            Method declaredMethod = obj.getClass().getDeclaredMethod(methodName, getParameterTypes(args));
            declaredMethod.setAccessible(true);
            return declaredMethod.invoke(obj, args);
        }catch (Throwable e){
            try {
                return callObjectMethod(obj, methodName, args);
            }catch (Exception e2){
                throw new RuntimeException(e2.getCause());
            }
        }
    }

    private static final Map<String, Class<?>> primitiveWrapperMap = new ConcurrentHashMap<String, Class<?>>(){{
        put(Boolean.class.getName(), boolean.class);
        put(Byte.class.getName(), byte.class);
        put(Character.class.getName(), char.class);
        put(Short.class.getName(), short.class);
        put(Integer.class.getName(), int.class);
        put(Long.class.getName(), long.class);
        put(Double.class.getName(), double.class);
        put(Float.class.getName(), float.class);
    }};
    public static Class<?> getPrimitiveType(Class<?> cls) {
        //if (!cls.isPrimitive()) return cls;
        return primitiveWrapperMap.getOrDefault(cls.getName(), cls);
    }


    public static Object callStaticObjectMethod(Class<?> cls, String methodName, Object... args)  {
        try {
            Method declaredMethod = cls.getDeclaredMethod(methodName, getParameterTypes(args));
            declaredMethod.setAccessible(true);
            return declaredMethod.invoke(null, args);
        }catch (Throwable e) { throw new RuntimeException(e); }
    }


    
    /**
     * 获取方法参数类型数组
     *
     * @param args 方法参数
     * @return 参数类型数组
     */
    private static Class<?>[] getParameterTypes(Object... args) {
        if (args == null || args.length == 0) {
            return new Class<?>[0];
        }
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = getPrimitiveType(args[i].getClass());
        }
        return parameterTypes;
    }

    public static Object getObjectField(Object obj, String fieldName) {
        try {
            Field declaredField = obj.getClass().getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            return declaredField.get(obj);
        } catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static Object getStaticObjectField(Class<?> cls, String fieldName) {
        try {
            Field declaredField = cls.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            return declaredField.get(null);
        } catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static void setObjectField(Object obj, Class<?> cls, String fieldName, Object newValue) {
        try {
            Field declaredField = cls.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            declaredField.set(obj, newValue);
        } catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static void setObjectField(Object obj, String fieldName, Object newValue) {
        try {
            Field declaredField = obj.getClass().getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            declaredField.set(obj, newValue);
        } catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static Method getObjectMethod(Class<?> cls, String methodName){
        for (Method method : cls.getDeclaredMethods()){
            if (method.getName().equals(methodName)){
                return method;
            }
        }
        return null;
    }
}
