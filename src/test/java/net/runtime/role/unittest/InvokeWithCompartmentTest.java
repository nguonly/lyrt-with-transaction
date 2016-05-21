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
public class InvokeWithCompartmentTest {
    @Before
    public void setupSchema(){
//        SchemaManager.drop();
//        SchemaManager.create();
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        //DBManager.close();
        RegistryManager.getInstance().setRelations(null);
    }

    //////// Prepare data
    public static class Person extends Player {
    }

    Person p = Player.initialize(Person.class);

    public static class RoleA extends Role {
        public String getName(){
            return this.getClass().getSimpleName();
        }
    }

    public static class RoleB extends Role{
        public String getName(){
            return this.getClass().getSimpleName();
        }
    }

    public static class CompartmentA extends Compartment {
        public CompartmentA(){

        }
    }

    @Test
    public void basicInvokeWithNullCompartment(){
        try(CompartmentA compA = Compartment.initialize(CompartmentA.class)) {
            p.bind(compA, RoleA.class);
            String retValue = p.invoke("getName", String.class);

            Assert.assertEquals(RoleA.class.getSimpleName(), retValue);
        }
    }
}
