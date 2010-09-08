package com.kg6sed.eggshell.jline;

import jline.Completor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 */
public class WrappingCompletor implements Completor {
    private Object instance;
    private Method method;

    public WrappingCompletor(Object instance, Method method) {
        this.instance = instance;
        this.method = method;
    }

    public int complete(String s, int i, List list) {
        try {
            Completor completor = (Completor) this.method.invoke(this.instance);
            if (completor == null) {
                return -1;
            }
            return completor.complete(s, i, list);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
