package net.runtime.role.actor;


import net.runtime.role.helper.StatisticsHelper;
import net.runtime.role.registry.RegistryManager;

/**
 * Created by nguonly role 7/9/15.
 */
public class Player {
    protected final RegistryManager m_registryManager = RegistryManager.getInstance();

    //public int Id;

    /* Bind */
    public <T extends Role> T bind(Compartment compartment, Class<T> role){

        return m_registryManager.bind(compartment, this, role, null, null);
    }

    public <T extends Role> T bind(Compartment compartment, Class<T> role,
                                   Class[] constructorArgumentTypes, Object[] constructorArgumentValues){
        return m_registryManager.bind(compartment, this, role, constructorArgumentTypes, constructorArgumentValues);

    }

    public <T extends Role> T bind(Class<T> role){
        return bind(null, role);
    }

    public <T extends Role> T bind(Class<T> role, Class[] constructorArgumentTypes, Object[] constructorArgumentValues){
        return m_registryManager.bind(null, this, role, constructorArgumentTypes, constructorArgumentValues);
    }

    public Object bind(String role){
        return m_registryManager.bind(null, this, role, null, null);
    }
    /* End of Binding */

    /* Invoke with implicit compartment */
    public void invoke(String methodName){
        m_registryManager.invokeRole(null, this, methodName, null, null, null);
    }

    public <T> T invoke(String methodName, Class<T> returnType){
        return m_registryManager.invokeRole(null, this, methodName, returnType, null, null);
    }

    public <T> T invoke(String methodName, Class<T> returnType, Class[] argumentTypes,  Object[] argumentValues){
        return m_registryManager.invokeRole(null, this, methodName, returnType, argumentTypes, argumentValues);
    }

    public void invoke(String methodName, Class[] argumentTypes,  Object[] argumentValues){
        m_registryManager.invokeRole(null, this, methodName, null, argumentTypes, argumentValues);
    }
    /* End of Invoke with implicit compartment */

    /* Invoke with compartment explicit */
    public void invoke(Compartment compartment, String methodName){
        m_registryManager.invokeRole(compartment, this, methodName, null, null, null);
    }

    public <T> T invoke(Compartment compartment, String methodName, Class<T> returnType){
        return m_registryManager.invokeRole(compartment, this, methodName, returnType, null, null);
    }

    public <T> T invoke(Compartment compartment, String methodName, Class<T> returnType, Class[] argumentTypes,  Object[] argumentValues){
        return m_registryManager.invokeRole(compartment, this, methodName, returnType, argumentTypes, argumentValues);
    }

    public void invoke(Compartment compartment, String methodName, Class[] argumentTypes,  Object[] argumentValues){
        m_registryManager.invokeRole(compartment, this, methodName, null, argumentTypes, argumentValues);
    }
    /* End of Invoke with compartment explicit */


    /* invoke compartment method with current active compartment */
    public <T> T invokeCompartment(String methodName, Class<T> returnType, Class[] arguementTypes,  Object[] argumentValues){
        return m_registryManager.invokeCompartment(true, this, methodName, returnType, arguementTypes, argumentValues);
    }

    public <T> T invokeCompartment(String methodName, Class<T> returnType){
        return m_registryManager.invokeCompartment(true, this, methodName, returnType, null, null);
    }

    public void invokeCompartment(String methodName, Class[] argumentType, Object[] argumentValues){
        m_registryManager.invokeCompartment(true, this, methodName, null, argumentType, argumentValues);
    }

    public void invokeCompartment(String methodName){
        m_registryManager.invokeCompartment(true, this, methodName, null, null, null);
    }
    /* end of invoke compartment method with current active compartment */


    public void unbind(Class role) {
        m_registryManager.unbind(this, role);
    }

    public void unbindAll(){
        m_registryManager.unbindAll(this);
    }

    public void prohibit(Compartment compartment, Class role){
        m_registryManager.prohibit(compartment, this, role);
    }

    public void prohibit(Class role){
        prohibit(null, role);
    }

    /**
     * Transfer
     */
    public void transfer(Class role, Object to){
        m_registryManager.transfer(role, this, null, to, null);
    }

    public void transfer(Class role, Compartment fromCompartment, Object to, Compartment toCompartment){
        m_registryManager.transfer(role, this, fromCompartment, to, toCompartment);
    }

    public static <T> T initialize(Class<T> player){

        return RegistryManager.getInstance().newPlayer(player, null, null);
    }

    public static <T> T initialize(Class<T> player, Class[] constructorArgumentTypes, Object[] constructorArgumentValues){
        return RegistryManager.getInstance().newPlayer(player, constructorArgumentTypes, constructorArgumentValues);
    }

    public <T> T role(Compartment compartment, Class<T> roleClass){
        return m_registryManager.role(compartment, this, roleClass);
    }

    public <T> T role(Class<T> roleClass){
        return m_registryManager.role(null, this, roleClass);
    }

    public Object getCompartment(){
        return m_registryManager.getCompartment(null, this);
    }

    /* Statistics */

    public int rolesCount(){
        return StatisticsHelper.rolesCount(this.hashCode());
    }

    /* End of Statistics */
}
