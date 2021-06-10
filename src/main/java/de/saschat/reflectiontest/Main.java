package de.saschat.reflectiontest;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        HTTPClassLoader test = new HTTPClassLoader("https://web.sascha-t.de/test/java/Gaming.jar");
        Class<?> clazz = test.loadClass("de.saschat.reflectiontest.Gaming");
        Method method = clazz.getMethod("getString");
        Object ret = method.invoke(null);
        System.out.println((String) ret);
    }
}
