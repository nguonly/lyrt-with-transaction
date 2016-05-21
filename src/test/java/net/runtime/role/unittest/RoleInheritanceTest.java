package net.runtime.role.unittest;

import net.runtime.role.actor.Compartment;
import net.runtime.role.actor.Player;
import net.runtime.role.actor.Role;
import net.runtime.role.exception.InheritItselfException;
import net.runtime.role.exception.SingleInheritanceException;
import net.runtime.role.helper.DumpHelper;
import net.runtime.role.registry.RegistryManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;

/**
 * Created by nguonly on 8/5/15.
 */
public class RoleInheritanceTest {
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
            name = "R1";
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
            name = "R2";
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
            name = "R4";
        }

        public R4(String name){
            this.name = name;
        }

        public String getName(){ return name; }
    }

    @Test
    public void simpleInheritance(){
        Compartment comp = new Compartment();
        comp.activate();

        P p = Player.initialize(P.class);

        R1 r1 = p.bind(R1.class);

        R2 r2 = r1.inherit(R2.class);

        DumpHelper.dumpRelation();

        comp.deActivate();
    }

    @Test
    public void deeperInheritance(){
        Compartment comp = new Compartment();
        comp.activate();

        P p = Player.initialize(P.class);

        R1 r1 = p.bind(R1.class);
        R2 r2 = r1.bind(R2.class);
        R3 r3 = r1.bind(R3.class);
        R4 r4 = r3.inherit(R4.class);

        DumpHelper.dumpRelation();

        Assert.assertEquals("R4", p.invoke("getName", String.class));

        comp.deActivate();
    }

    @Test(expected = InheritItselfException.class)
    public void inheritTheSameSubRoleTypeIsNotAllow(){
        Compartment comp = new Compartment();
        comp.activate();

        P p = Player.initialize(P.class);

        R1 r1 = p.bind(R1.class);

        r1.inherit(R1.class); //Cause runtime exception

        //Below code is never executed

        comp.deActivate();
    }

    @Test(expected = SingleInheritanceException.class)
    public void inheritanceWithTheSameType(){
        Compartment comp = new Compartment();
        comp.activate();

        P p = Player.initialize(P.class);

        R1 r1 = p.bind(R1.class);

        R2 r2 = r1.inherit(R2.class);

        r1.inherit(R2.class); //Cause runtime exception

        //Below code is never executed

        comp.deActivate();
    }

    @Test(expected = SingleInheritanceException.class)
    public void singleInheritance(){
        Compartment comp = new Compartment();
        comp.activate();

        P p = Player.initialize(P.class);

        R1 r1 = p.bind(R1.class);
        R2 r2 = r1.inherit(R2.class);

        //Cause runtime exception
        R3 r3 = r1.inherit(R3.class);

        comp.deActivate();
    }


}
