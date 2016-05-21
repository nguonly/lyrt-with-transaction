package net.runtime.role.actor;


import net.runtime.role.registry.RegistryManager;

/**
 * Created by nguonly role 7/10/15.
 */
public class Compartment extends Player implements AutoCloseable{

    public static <T> T initialize(Class<T> compartment){
        return RegistryManager.getInstance().newCompartment(compartment, null, null);
    }

    public static <T> T initialize(Class<T> compartment, Class[] constructorArgumentTypes, Object[] constructorArgumentValues){
        return RegistryManager.getInstance().newCompartment(compartment, constructorArgumentTypes, constructorArgumentValues);
    }

    final public void activate(){
        m_registryManager.activateCompartment(this);
    }

    final public void deActivate(){
        close();
    }

    @Override
    public void close() {
        try {
            m_registryManager.deactivateCompartment(this);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
