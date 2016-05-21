package net.runtime.role.transfersystem.encryption;

/**
 * Created by nguonly on 8/19/15.
 */
public class Encryption {
    private static final float key = 42.4711f;
    private static final float key2 = 45047028;

    public static float encrypt(float val) {
        return (val * key) / key2;
    }

    public static float decrypt(float val) {
        return (val * key2) / key;
    }
}
