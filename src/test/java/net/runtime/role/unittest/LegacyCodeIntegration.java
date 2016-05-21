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
import java.util.ArrayList;

/**
 * This is to show how roles can be integrated to legacy code. Good for fixing bug.
 * Or adding new functionality with the same interface.
 * Created by nguonly on 10/19/15.
 */


public class LegacyCodeIntegration {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager.getInstance().setRelations(null);
    }

    public static class BrokenObject extends Player {
        ArrayList<String> list = new ArrayList<>();

        public void add(String item){
            list.add(item);
        }

        public void printList(){
            list.forEach(System.out::println);
        }

        public ArrayList<String> getList(){
            return list;
        }
    }

    public static class FixObject extends Role {
        public void add(String item){
            ArrayList<String> obj = invokeCore("getList", ArrayList.class);
            obj.add("Fix::" + item);
        }
    }

    @Test
    public void rolePatching(){
        BrokenObject bo = new BrokenObject();
        bo.add("Hello");
        bo.add("Welcome");
        bo.add("Kingdom");

        //bo.printList();

        Compartment comp = new Compartment();
        comp.activate();
        bo.bind(FixObject.class);
        bo.role(FixObject.class).add("AA");
        bo.printList();

        Assert.assertEquals("Fix::AA", bo.getList().get(3));

        comp.deActivate();
    }
}
