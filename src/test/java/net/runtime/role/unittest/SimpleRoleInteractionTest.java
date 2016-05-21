package net.runtime.role.unittest;


import net.runtime.role.actor.Compartment;
import net.runtime.role.actor.Player;
import net.runtime.role.actor.Role;
import net.runtime.role.registry.RegistryManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;

/**
 * Created by nguonly on 9/7/15.
 */
public class SimpleRoleInteractionTest {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager.getInstance().setRelations(null);
    }


    public static class Guest extends Role {
        public void escape(){
            Player root = (Player)getRootPlayer();
            String rootName = root.invoke("getName", String.class);
            System.out.println(rootName + " Runs");
        }
    }

    public static class Security extends Role{
        public void notifyGuestToEscape(){
            Object[] roots = getRootPlayer(Guest.class);
            for(Object root: roots){
                Player p = (Player)root;
                p.invoke("escape");
            }
        }
    }

    public static class Person extends Player{
        private String name;

        public void setName(String name){
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Test
    public void securityNotifiesAllGuest(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            Person alice = Player.initialize(Person.class);
            alice.setName("Alice");
            Person bob = Player.initialize(Person.class);
            bob.setName("Bob");

            Person yarin = Player.initialize(Person.class);
            yarin.setName("Yarin Security Guard");

            alice.bind(Guest.class);
            bob.bind(Guest.class);

            yarin.bind(Security.class);

            yarin.invoke("notifyGuestToEscape");
        }
    }
}
