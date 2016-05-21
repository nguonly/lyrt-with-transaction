package net.runtime.role.unittest;

import net.runtime.role.actor.Compartment;
import net.runtime.role.actor.Player;
import net.runtime.role.registry.RegistryManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;

/**
 * Created by nguonly on 8/10/15.
 */
public class CompartmentInitializationTest {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager.getInstance().setRelations(null);
    }

    public static class Faculty extends Compartment {
        Player p;
        public Faculty(Player p){
            this.p = p;
        }

        public Player getPlayer(){
            return p;
        }
    }

    @Test
    public void initializeCompartmentWithoutArgument(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            Assert.assertNotNull(comp);
        }
    }

    @Test
    public void initializeCompartmentWithArgument(){
        Player p = Player.initialize(Player.class);
        try(Faculty fac = Compartment.initialize(Faculty.class,
                new Class[]{Player.class}, new Object[] {p})){

            Assert.assertNotNull(fac);

            Assert.assertEquals(p, fac.getPlayer());
        }
    }
}
