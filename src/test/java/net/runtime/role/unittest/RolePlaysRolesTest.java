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
public class RolePlaysRolesTest {
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
        private String name;

        public R2(){
            name = "Empty";
        }

        public R2(String name){
            this.name = name;
        }

        public String getName() { return name; }
    }

    public static class R3 extends Role{

    }

    public static class R4 extends Role{
        private String name;

        public R4(){
            name = "Empty";
        }

        public R4(String name){
            this.name = name;
        }

        public String getName(){ return name; }
    }

    @Test
    public void bindRoleWithNoArgument(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            P p = Player.initialize(P.class);

            R2 r2 = p.bind(R1.class).bind(R2.class);

            Assert.assertEquals("Empty", r2.getName());
        }
    }

    @Test
    public void bindRoleWithArguments(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            P p = Player.initialize(P.class);

            R2 r2 = p.bind(R1.class).bind(R2.class, new Class[]{String.class},
                    new Object[]{"lycog"});

            Assert.assertEquals("lycog", r2.getName());
        }
    }

    @Test(expected = BindTheSameRoleTypeException.class)
    public void bindWithTheSameRoleType(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            P p = Player.initialize(P.class);

            R1 r1 = p.bind(R1.class);

            r1.bind(R2.class);

            r1.bind(R2.class); //cause Runtime error since this role type is previously bound
        }
    }

    @Test(expected = BindTheSameRoleTypeException.class)
    public void bindWithTheSameRoleTypeInDeepLevel(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            P p = Player.initialize(P.class);

            R2 r2 = p.bind(R1.class).bind(R2.class);

            r2.bind(R3.class);

            r2.bind(R3.class);// cause Runtime exception
        }
    }

    @Test
    public void bindWithTheSameRoleTypeInDifferentLevel(){
        P p = Player.initialize(P.class);

        try(Compartment comp1 = Compartment.initialize(Compartment.class)){
            R1 r1 = p.bind(R1.class);

            r1.bind(R2.class);
            R3 r3 = r1.bind(R3.class);
            r3.bind(R2.class); //no runtime error because it's in different level

            Assert.assertEquals(4, p.rolesCount());
        }
    }

    @Test
    public void bindRoleWithNoDeclaredMethods(){
        try(Compartment comp1 = Compartment.initialize(Compartment.class)){
            P p = Player.initialize(P.class);

            p.bind(R2.class).bind(R3.class); //Empty role with no delcared methods

            Assert.assertEquals(2, p.rolesCount());
        }
    }

    @Test
    public void bindRoleWithExplicitCompartment(){
        Compartment comp1 = new Compartment();
        comp1.activate(); //active context

        P p = Player.initialize(P.class);

        //bind with no argument
        R1 r1 = p.bind(comp1, R1.class);

        //implicit binding
        R2 r2 = r1.bind(R2.class);

        Assert.assertEquals("Empty", r1.getName());

        //Bind with argument
        R4 r4 = r2.bind(comp1, R4.class, new Class[]{String.class}, new Object[]{"lycog"});

        Assert.assertEquals("lycog", r4.getName());

        comp1.deActivate();
    }
}
