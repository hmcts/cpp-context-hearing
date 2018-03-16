package uk.gov.moj.cpp.hearing.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class TestUtils {

    private TestUtils() {
    }
    
    public static boolean hasInnerStaticClass(final Class<?> clazz, final String className) {
        return null != clazz && Arrays.asList(clazz.getDeclaredClasses()).stream().filter(c -> c.getSimpleName().equals(className)).collect(Collectors.toList()).size() > 0;
    }
    
    public static boolean hasParameterizedConstructor(final Class<?> clazz, final Class<?>... parameterTypes) {
        try {
            return null != clazz && Arrays.asList(clazz.getDeclaredConstructor(parameterTypes)).stream().count() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}