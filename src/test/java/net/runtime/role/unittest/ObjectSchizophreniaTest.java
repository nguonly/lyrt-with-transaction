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
 * Created by nguonly on 8/4/15.
 */
public class ObjectSchizophreniaTest {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager.getInstance().setRelations(null);
    }

    public static class P extends Player {
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        private String name;

        public P(){
            name = "P";
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
    public void whereIsMyRootPlayer(){
        Compartment comp = new Compartment();
        comp.activate();

        P p = Player.initialize(P.class);

        R1 r1 = p.bind(R1.class);
        R2 r2 = p.bind(R2.class);

        R3 r3 = r2.bind(R3.class);
        R4 r4 = r3.bind(R4.class);

        Assert.assertEquals(p, r1.getRootPlayer());
        Assert.assertEquals(p, r2.getRootPlayer());
        Assert.assertEquals(p, r3.getRootPlayer());
        Assert.assertEquals(p, r4.getRootPlayer());

        R1 rr1 = r3.bind(R1.class); //R1 role in deeper level

        Assert.assertEquals(p, rr1.getRootPlayer());

        Assert.assertNotEquals(r1, rr1);//Same R1 role type but different instance

        //different player
        P p2 = Player.initialize(P.class);
        r1 = p2.bind(R1.class);
        r2 = p2.bind(R2.class);
        r3 = r2.bind(R3.class);
        r4 = r3.bind(R4.class);

        Assert.assertEquals(p2, r1.getRootPlayer());
        Assert.assertEquals(p2, r2.getRootPlayer());
        Assert.assertEquals(p2, r3.getRootPlayer());
        Assert.assertEquals(p2, r4.getRootPlayer());

        //Destroy relation and compartment/context
        comp.deActivate();
    }

    @Test
    public void whereIsMyRole(){
        Compartment comp = new Compartment();
        comp.activate();

        P p = Player.initialize(P.class);

        R1 r1 = p.bind(R1.class);
        R2 r2 = p.bind(R2.class);

        R3 r3 = r2.bind(R3.class);
        R4 r4 = r3.bind(R4.class);

        Assert.assertEquals(r1, p.role(R1.class));
        Assert.assertEquals(r2, p.role(R2.class));
        Assert.assertEquals(r3, p.role(R3.class));
        Assert.assertEquals(r4, p.role(R4.class));

        R1 rr1 = r3.bind(R1.class); //R1 role in deeper level

        /**
         * From root player we cannot find R3.class because it appears two times in the relation
         */
        Assert.assertEquals(rr1, r3.role(R1.class));

        Assert.assertNotEquals(r1, rr1);//Same R1 role type but different instance

        //different player
        P p2 = Player.initialize(P.class);
        r1 = p2.bind(R1.class);
        r2 = p2.bind(R2.class);
        r3 = r2.bind(R3.class);
        r4 = r3.bind(R4.class);

        Assert.assertEquals(r1, p2.role(R1.class));
        Assert.assertEquals(r2, p2.role(R2.class));
        Assert.assertEquals(r3, p2.role(R3.class));
        Assert.assertEquals(r4, p2.role(R4.class));

        //Destroy relation and compartment/context
        comp.deActivate();
    }

    @Test
    public void whereIsMyPlayer(){
        Compartment comp = new Compartment();
        comp.activate();

        P p = Player.initialize(P.class);

        R1 r1 = p.bind(R1.class);
        R2 r2 = p.bind(R2.class);

        R3 r3 = r2.bind(R3.class);
        R4 r4 = r3.bind(R4.class);

        Assert.assertEquals(p, r1.getPlayer());
        Assert.assertEquals(p, r2.getPlayer());
        Assert.assertEquals(r2, r3.getPlayer());
        Assert.assertEquals(r3, r4.getPlayer());

        R1 rr1 = r3.bind(R1.class); //R1 role in deeper level

        /**
         * From root player we cannot find R3.class because it appears two times in the relation
         */
        Assert.assertEquals(r3, rr1.getPlayer());

        Assert.assertNotEquals(r1, rr1);//Same R1 role type but different instance

        //different player
        P p2 = Player.initialize(P.class);
        r1 = p2.bind(R1.class);
        r2 = p2.bind(R2.class);
        r3 = r2.bind(R3.class);
        r4 = r3.bind(R4.class);

        Assert.assertEquals(p2, r1.getPlayer());
        Assert.assertEquals(p2, r2.getPlayer());
        Assert.assertEquals(r2, r3.getPlayer());
        Assert.assertEquals(r3, r4.getPlayer());

        comp.deActivate();
    }

    @Test
    public void whereIsMyCompartment(){
        Compartment comp = new Compartment();
        comp.activate();

        P p = Player.initialize(P.class);

        R1 r1 = p.bind(R1.class);
        R2 r2 = p.bind(R2.class);

        R3 r3 = r2.bind(R3.class);
        R4 r4 = r3.bind(R4.class);

        Assert.assertEquals(comp, p.getCompartment());
        Assert.assertEquals(comp, r1.getCompartment());
        Assert.assertEquals(comp, r2.getCompartment());
        Assert.assertEquals(comp, r3.getCompartment());
        Assert.assertEquals(comp, r4.getCompartment());

        R1 rr1 = r3.bind(R1.class); //R1 role in deeper level

        /**
         * From root player we cannot find R3.class because it appears two times in the relation
         */
        Assert.assertEquals(comp, rr1.getCompartment());

        comp.deActivate();
    }

    @Test
    public void whereIsMyCompartmentInNestedCompartment(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            P p = Player.initialize(P.class);

            R1 r1 = p.bind(R1.class);

            Assert.assertEquals(comp, p.getCompartment());
            Assert.assertEquals(comp, r1.getCompartment());

            try(Compartment comp2 = Compartment.initialize(Compartment.class)){
                R3 r3 = p.bind(R3.class);

                Assert.assertEquals(comp2, p.getCompartment());
                Assert.assertEquals(comp2, r3.getCompartment());
            }

            Assert.assertEquals(comp, p.getCompartment());
            Assert.assertEquals(comp, r1.getCompartment());
        }
    }
}
