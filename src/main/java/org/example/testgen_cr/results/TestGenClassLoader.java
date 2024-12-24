package org.example.testgen_cr.results;

import java.net.URL;
import java.net.URLClassLoader;

public class TestGenClassLoader extends URLClassLoader {
    private ClassLoader extensionClassLoader;

    public TestGenClassLoader(URL[] urls) {
        super(urls);

        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        extensionClassLoader = systemClassLoader.getParent();
    }

    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = findLoadedClass(name);
            if (clazz != null) {
                return clazz;
            }

            try {
                clazz = extensionClassLoader.loadClass(name);
                if (clazz != null) {
                    return clazz;
                }
            } catch (ClassNotFoundException e) { /* empty */ }

            clazz = super.loadClass(name);
            return clazz;
        }
    }
}
