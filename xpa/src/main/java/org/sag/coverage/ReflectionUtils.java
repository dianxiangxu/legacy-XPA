package org.sag.coverage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class ReflectionUtils {
    private static Log logger = LogFactory.getLog(ReflectionUtils.class);

    static Object getField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalArgumentException | IllegalAccessException
                | NoSuchFieldException | SecurityException e) {
            logger.error(e);
            throw new RuntimeException("reflection failed");
        }
    }

    static Object invokeMethod(Object obj, String methodName,
                               Object[] args, Class<?>[] parameterTypes) {
        Method method;
        try {
            method = obj.getClass().getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (NoSuchMethodException | SecurityException
                | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            logger.error(e);
            e.printStackTrace();
            throw new RuntimeException("reflection failed");
        }

    }
}
