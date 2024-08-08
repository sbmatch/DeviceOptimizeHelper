//
// Decompiled by Jadx - 540ms
//
package ma.DeviceOptimizeHelper.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectUtil {
    public static Object callAnyObjectMethod(Class<?> cls, Object obj, String methodName, Class<?>[] parameterTypes, Object... args)  {
        try {
            Method declaredMethod = cls.getDeclaredMethod(methodName, parameterTypes);
            declaredMethod.setAccessible(true);
            return declaredMethod.invoke(obj, args);
        } catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static Object callObjectMethod(Object obj, String methodName, Class<?> cls)  {
        try {
            Method declaredMethod = cls.getDeclaredMethod(methodName);
            declaredMethod.setAccessible(true);
            return declaredMethod.invoke(obj);
        } catch (Throwable e) { throw new RuntimeException(e); }
    }
    public static Object callObjectMethod(Object obj, String methodName, Class<?> cls, Class<?>[] parameterTypes, Object... args)  {
        try {
            Method declaredMethod = cls.getDeclaredMethod(methodName, parameterTypes);
            declaredMethod.setAccessible(true);
            return declaredMethod.invoke(obj, args);
        }catch (Throwable e){ throw new RuntimeException(e); }
    }

    public static Object callObjectMethod2(Object obj, String methodName, Object... args)  {
        try {
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
        }catch (Throwable e){ throw new RuntimeException(e); }
    }

    public static Object callStaticObjectMethod(Class<?> cls, String methodName, Class<?>[] parameterTypes, Object... args)  {
        try {
            Method declaredMethod = cls.getDeclaredMethod(methodName, parameterTypes);
            declaredMethod.setAccessible(true);
            return declaredMethod.invoke(null, args);
        }catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static Object callStaticObjectMethod(Class<?> cls, String methodName)  {
        try {
            Method declaredMethod = cls.getDeclaredMethod(methodName);
            declaredMethod.setAccessible(true);
            return declaredMethod.invoke(null);
        } catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static Object getObjectField(Object obj, Class<?> cls, String fieldName) {
        try {
            Field declaredField = cls.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            return declaredField.get(obj);
        } catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static Object getObjectField(Object obj, String fieldName) {
        try {
            Field declaredField = obj.getClass().getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            return declaredField.get(obj);
        } catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static Object getValueByField(Field field, Object obj) {
        try {
            if (!field.isAccessible()) field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalAccessException e) { throw new RuntimeException(e); }
    }

    public static Field[] getFieldsByClass(Class<?> cls) {
        Field[] declaredFields = cls.getDeclaredFields();
        for (Field field : declaredFields){ field.setAccessible(true);}
        return declaredFields;
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
