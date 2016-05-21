package net.runtime.role.helper;

/**
 * Created by nguonly on 10/29/15.
 */
public class StringValueConverter {
    public static <T> T convert(String from, Class<T> to){
        Object obj = null;
        if(to.isPrimitive()){
            //return (T)from;
            if(to.equals(byte.class)) obj = Byte.parseByte(from);
            if(to.equals(int.class)) obj = Integer.parseInt(from);
            if(to.equals(short.class)) obj = Short.parseShort(from);
            if(to.equals(long.class)) obj = Long.parseLong(from);
            if(to.equals(float.class)) obj = Float.parseFloat(from);
            if(to.equals(double.class)) obj = Double.parseDouble(from);
            if(to.equals(boolean.class)) obj = Boolean.parseBoolean(from);
        }else{
            return to.cast(from);
        }
        return (T)obj;
    }
}
