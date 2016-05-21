package net.runtime.role.registry;

/**
 * Created by nguonly on 1/25/16.
 */
public interface IEvolution {
    public Object newPlayer(String player, Class[] argTypes, Object[] argValues);
    public Object newCompartment(String compartment, Class[] argTypes, Object[] argValues);
    public Object newRole(String role, Class[] argTypes, Object[] argValues);

    public Object bind(int compartment, int playerId, String role,
                       Class[] argTypes, Object[] argValues);
    public void rebind(int compartmentId, int coreId, String roleClass, Class[] argTypes, Object[] argValues);

    public void unbind(int coreId, String roleClass);

    public void evolve(String xmlPath);

    /*
    public Object newCompartment(String ct, Object... args);
    public Object newCore(String ct, Object... args);
    public Object newRole(String rt, Object... args);
    */
}
