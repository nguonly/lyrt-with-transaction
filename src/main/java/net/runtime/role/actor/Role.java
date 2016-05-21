package net.runtime.role.actor;

import net.runtime.role.registry.RegistryManager;

/**
 * Created by nguonly role 7/10/15.
 */
public class Role {

    RegistryManager registryManager = RegistryManager.getInstance();

    /* Inheritance */
    public <T extends Role> T inherit(Compartment compartment, Class<T> superRole){
        return registryManager.inherit(compartment, this, superRole, null, null);
    }

    public <T extends Role> T inherit(Compartment compartment, Class<T> superRole, Class[] constructorArgumentTypes,
                                      Object[] constructorArgumentValues){
        return registryManager.inherit(compartment, this, superRole, constructorArgumentTypes, constructorArgumentValues);
    }

    public <T extends Role> T inherit(Class<T> superRole){
        return registryManager.inherit(null, this, superRole, null, null);
    }

    public <T extends Role> T inherit(Class<T> superRole, Class[] constructorArgumentTypes,
                                      Object[] constructorArgumentValues){
        return registryManager.inherit(null, this, superRole, constructorArgumentTypes, constructorArgumentValues);
    }

    /* End of Inheritance */

    /* Bind operations */

    public <T extends Role> T bind(Compartment compartment, Class<T> role){
        //return registryManager.rolePlaysRole(compartment, this, role, null, null);
        return registryManager.bind(compartment, this, role, null, null);
    }

    public <T extends Role> T bind(Compartment compartment, Class<T> role,
                                   Class[] constructorArgumentTypes,
                                   Object[] constructorArgumentValues){
        //return registryManager.rolePlaysRole(compartment, this, role, constructorArgumentTypes, constructorArgumentValues);
        return registryManager.bind(compartment, this, role, constructorArgumentTypes, constructorArgumentValues);
    }

    public <T extends Role> T bind(Class<T> role){
        return bind(null, role);
    }

    public <T extends Role> T bind(Class<T> role, Class[] constructorArgumentTypes,
                                   Object[] constructorArgumentValues){
        //return registryManager.rolePlaysRole(null, this, role, constructorArgumentTypes, constructorArgumentValues);
        return registryManager.bind(null, this, role, constructorArgumentTypes, constructorArgumentValues);
    }

    /* End of Bind operation */

    public void unbind(Class role){

        registryManager.unbind(this, role);
    }

    public void prohibit(Compartment compartment, Class role){
        registryManager.prohibit(compartment, this, role);
    }

    public void prohibit(Class role){
        prohibit(null, role);
    }

    public void invoke(String methodName){
        registryManager.invokeRole(null, this, methodName, null, null, null);
    }

    public void invoke(String methodName, Class[] argumentTypes,  Object[] argumentValus){
        registryManager.invokeRole(null, this, methodName, null, argumentTypes, argumentValus);
    }

    public <T> T invoke(String methodName, Class<T> returnType){
        return registryManager.invokeRole(null, this, methodName, returnType, null, null);
    }
    public <T> T invoke(String methodName, Class<T> returnType, Class[] argumentTypes,  Object[] argumentValues){
        return registryManager.invokeRole(null, this, methodName, returnType, argumentTypes, argumentValues);
    }

    public <T> T invokeBase(String methodName, Class<T> returnType, Class[] argumentTypes, Object[] argumentValues){
        return registryManager.invokeBase(this, methodName, returnType, argumentTypes, argumentValues);
    }

    public void invokeBase(String methodName, Class[] argumentTypes, Object[] argumentValues){
        registryManager.invokeBase(this, methodName, null, argumentTypes, argumentValues);
    }

    public <T> T invokeBase(String methodName, Class<T> returnType){
        return registryManager.invokeBase(this, methodName, returnType, null, null);
    }

    public void invokeBase(String methodName){
        registryManager.invokeBase(this, methodName, null, null, null);
    }

    /* invoke compartment method with current active compartment */
    public <T> T invokeCompartment(String methodName, Class<T> returnType, Class[] arguementTypes,  Object[] argumentValues){
        return registryManager.invokeCompartment(false, this, methodName, returnType, arguementTypes, argumentValues);
    }

    public <T> T invokeCompartment(String methodName, Class<T> returnType){
        return registryManager.invokeCompartment(false, this, methodName, returnType, null, null);
    }

    public void invokeCompartment(String methodName, Class[] argumentType, Object[] argumentValues){
        registryManager.invokeCompartment(false, this, methodName, null, argumentType, argumentValues);
    }

    public void invokeCompartment(String methodName){
        registryManager.invokeCompartment(false, this, methodName, null, null, null);
    }
    /* end of invoke compartment method with current active compartment */

    /* Invoke root */
    public <T> T invokeCore(String methodName, Class<T> returnType, Class[] arguementTypes, Object[] argumentValues){
        return registryManager.invokeCore(this, methodName, returnType, arguementTypes, argumentValues);
    }

    public <T> T invokeCore(String methodName, Class<T> returnType){
        return registryManager.invokeCore(this, methodName, returnType, null, null);
    }

    public void invokeCore(String methodName, Class[] argumentType, Object[] argumentValues){
        registryManager.invokeCore(this, methodName, null, argumentType, argumentValues);
    }

    public void invokeCore(String methodName){
        registryManager.invokeCore(this, methodName, null, null, null);
    }

    /* End of invoke root */

    /* type safe base reference */
    public <T> T base(Compartment compartment, Class<T> baseClass){
        return registryManager.base(compartment, this, baseClass);
    }

    public <T> T base(Class<T> baseClass){
        return registryManager.base(null, this, baseClass);
    }

    public <T> T compartment(Compartment compartment, Class<T> compartmentClass){
        return registryManager.compartment(compartment, compartmentClass);
    }

    public <T> T compartment(Class<T> compartmentClass){
        return registryManager.compartment(null, compartmentClass);
    }

    public Object getRootPlayer(){
        return registryManager.getRootPlayer(null, this);
    }

    public Object[] getRootPlayer(Class role){
        return registryManager.getRootPlayers(null, role);
    }

    public <T> T role(Compartment compartment, Class<T> roleClass){
        return registryManager.role(compartment, this, roleClass);
    }

    public <T> T role(Class<T> roleClass){
        return registryManager.role(null, this, roleClass);
    }

    public Object getPlayer(){
        return registryManager.getPlayer(null, this);
    }

    public Object getPlayer(Compartment compartment){
        return registryManager.getPlayer(compartment, this);
    }

    public Object getCompartment(){
        return registryManager.getCompartment(null, this);
    }


    public <T> T proceed(){
        return null;
    }
}
