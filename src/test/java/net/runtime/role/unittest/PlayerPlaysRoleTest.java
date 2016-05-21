package net.runtime.role.unittest;


import net.runtime.role.actor.Compartment;
import net.runtime.role.actor.Player;
import net.runtime.role.actor.Role;
import net.runtime.role.exception.BindTheSameRoleTypeException;
import net.runtime.role.registry.RegistryManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;

/**
 * Created by nguonly on 8/4/15.
 */
public class PlayerPlaysRoleTest {

    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager.getInstance().setRelations(null);
    }

    public static class P extends Player {
        private String name;

        public P(){

        }

        public P(String name){
            this.name = name;
        }

        public String getName(){
            return name;
        }
    }

    public static class R1 extends Role {
        private String name;

        public R1(){
            name = "Empty";
        }

        public R1(String name){
            this.name = name;
        }

        public String getName(){
            return name;
        }
    }

    public static class R2 extends Role{

    }

    public static class R3 extends Role{
        private String name;

        public R3(){
            name = "Empty";
        }

        public R3(String name){
            this.name = name;
        }

        public String getName(){ return name;}
    }

    @Test
    public void bindWithNoArgument(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            P p = Player.initialize(P.class, new Class[]{String.class}, new Object[] { "lycog"});

            Assert.assertEquals("lycog", p.getName());

            //Binding with not argument
            R1 r1 = p.bind(R1.class);

            Assert.assertEquals("Empty", r1.getName());
        }
    }

    @Test
    public void bindWithArgument(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            P p = Player.initialize(P.class);

            //bind with arguments of constructor of role R1
            R1 r1 = p.bind(R1.class, new Class[] {String.class}, new Object[]{"lycog"});

            Assert.assertEquals("lycog", r1.getName());
        }
    }

    @Test(expected = BindTheSameRoleTypeException.class)
    public void bindWithTheSameRoleType(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            P p = Player.initialize(P.class);

            p.bind(R1.class);

            p.bind(R1.class); //cause Runtime error since this role type is previously bound
        }
    }

    @Test
    public void bindWithTheSameRoleTypeInDifferentCompartment(){
        P p = Player.initialize(P.class);

        try(Compartment comp1 = Compartment.initialize(Compartment.class)){
            R1 r1 = p.bind(R1.class);

            Assert.assertEquals("Empty", r1.getName());

            try(Compartment comp2 = Compartment.initialize(Compartment.class)){
                //No runtime exception because bind in different compartment
                R1 rr1 = p.bind(R1.class);

                Assert.assertEquals("Empty", rr1.getName());
            }
        }
    }

    @Test
    public void bindRoleWithNoDeclaredMethods(){
        try(Compartment comp1 = Compartment.initialize(Compartment.class)){
            P p = Player.initialize(P.class);

            p.bind(R2.class); //Empty role with no delcared methods

            Assert.assertEquals(1, p.rolesCount());
        }
    }

    @Test
    public void bindRoleWithExplicitCompartment(){
        Compartment comp1 = new Compartment();
        comp1.activate();

        P p = Player.initialize(P.class);

        //bind with no argument
        R1 r1 = p.bind(comp1, R1.class);

        Assert.assertEquals("Empty", r1.getName());

        //Bind with argument
        R3 r3 = p.bind(comp1, R3.class, new Class[]{String.class}, new Object[]{"lycog"});

        Assert.assertEquals("lycog", r3.getName());

        comp1.deActivate();
    }
}
