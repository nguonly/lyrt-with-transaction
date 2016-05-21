package net.runtime.role.unittest;

import net.runtime.role.actor.Player;
import net.runtime.role.registry.RegistryManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;

/**
 * Created by nguonly on 7/30/15.
 */
public class PlayerInitializationTest {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager.getInstance().setRelations(null);
    }

    public static class P extends Player {
        private String m_name;
        private int m_age;

        public P(){

        }

        public P(String name, int age){
            m_name = name;
            m_age = age;
        }

        public String getName() {
            return m_name;
        }

        public int getAge() {
            return m_age;
        }
    }

    @Test
    public void initializePlayerWithoutArguments(){
        P p = Player.initialize(P.class);

        Assert.assertNotNull(p);
    }

    @Test
    public void initializePlayerWithArguments(){
        P p = Player.initialize(P.class, new Class[] {String.class, int.class}, new Object[]{"lycog", 33});

        Assert.assertNotNull(p);

        Assert.assertEquals("lycog", p.getName());
    }
}
