package net.runtime.role.unittest;


import net.runtime.role.actor.Compartment;
import net.runtime.role.actor.Player;
import net.runtime.role.actor.Role;
import net.runtime.role.registry.RegistryManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;

/**
 * Created by nguonly role 7/20/15.
 */
public class InvokeCompartmentMethodTest {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager.getInstance().setRelations(null);
    }

    static Person p = Player.initialize(Person.class);

    public static class Person extends Player {
    }

    public static class RoleA extends Role {
        public void setValueInCompartment(String value){
            invokeCompartment("setValue", new Class[]{String.class}, new Object[]{value});
        }
    }

    public static class RoleB extends Role{

    }

    public static class CompartmentA extends Compartment {
        public void configureBinding(){
            p.bind(this, RoleA.class);
        }


        private String value;

        //This will called by role or player
        public void setValue(String value){
            this.value = value;
        }

        public String getValue(){
            return value;
        }

    }

    @Test
    public void invokeCompartmentMethodFromRole(){
        CompartmentA comp = Compartment.initialize(CompartmentA.class);
        comp.configureBinding();
        String value = "Compartment";
        p.invoke(comp, "setValueInCompartment", new Class[]{String.class}, new Object[]{value});

        Assert.assertEquals(value, comp.getValue());

        comp.deActivate();
    }

}
