package net.runtime.role.helper;

/**
 * Created by nguonly on 10/29/15.
 */
public class ClassHelper {
    public static Class<?> forName(String clazz) {
        try {
            return getPrimitiveType(clazz);
        }catch (IllegalArgumentException ie){
            try {
                return Class.forName(clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static Class<?> getPrimitiveType(String name){
        if (name.equals("byte")) return byte.class;
        if (name.equals("short")) return short.class;
        if (name.equals("int")) return int.class;
        if (name.equals("long")) return long.class;
        if (name.equals("char")) return char.class;
        if (name.equals("float")) return float.class;
        if (name.equals("double")) return double.class;
        if (name.equals("boolean")) return boolean.class;
        if (name.equals("void")) return void.class;

        throw new IllegalArgumentException();
    }
}
