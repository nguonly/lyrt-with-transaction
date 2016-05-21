package net.runtime.role.unittest;


import net.runtime.role.actor.Compartment;
import net.runtime.role.actor.Player;
import net.runtime.role.actor.Role;
import net.runtime.role.helper.StatisticsHelper;
import net.runtime.role.orm.Relation;
import net.runtime.role.registry.RegistryManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by nguonly role 7/10/15.
 */
public class RoleTransferTest {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager.getInstance().setRelations(null);
    }

    //////// Prepare data
    public static class Person extends Player {
    }

    public static class Employee extends Role {
        public String getAddress(){
            return "Employee printAddress";
        }
    }

    public static class Student extends Role {
        int matriculationNo = this.hashCode();

        public String takeCourse(String course){
            return "This student takes " + course;
        }
    }

    public static class SysAdmin extends Role {
    }

    @Test
    public void transferRoleInAnonymousCompartment(){
        try(Compartment comp = Compartment.initialize(Compartment.class)) {
            Person alice = Player.initialize(Person.class);
            Person bob = Player.initialize(Person.class);

            alice.bind(Employee.class);
            Role sysAdmin = alice.bind(SysAdmin.class);

            bob.bind(Employee.class);

            RegistryManager registryManager = RegistryManager.getInstance();
            Optional<Relation> sysAdminRel = registryManager.getRelations().stream()
                    .filter(r -> r.getObjectId() == alice.hashCode() && r.getRoleName().equals(sysAdmin.getClass().getName()))
                    .findFirst();
            assertEquals(sysAdminRel.get().getRoleId(), sysAdmin.hashCode());

            //Transfer role
            alice.transfer(SysAdmin.class, bob);

            sysAdminRel = registryManager.getRelations().stream()
                    .filter(r -> r.getObjectId() == bob.hashCode() && r.getRoleName().equals(sysAdmin.getClass().getName()))
                    .findFirst();
            assertEquals(sysAdminRel.get().getRoleId(), sysAdmin.hashCode());

            sysAdminRel = registryManager.getRelations().stream()
                    .filter(r -> r.getObjectId() == alice.hashCode() && r.getRoleName().equals(sysAdmin.getClass().getName()))
                    .findFirst();
            assertTrue(!sysAdminRel.isPresent());
        }
    }


    /**
     * Test role transferring in the same compartment
     */

    class Company extends Compartment {
        public Person alice = new Person();
        public Person bob = new Person();

        Company(){
            alice.bind(this, Employee.class);
            alice.bind(this, SysAdmin.class);

            bob.bind(this, Employee.class);
        }

        public void transfer(){
            alice.transfer(SysAdmin.class, this, bob, this);
        }
    }


    @Test
    public void transferRoleWithInACompartment(){
        Company company = new Company();

        company.transfer();

        RegistryManager registryManager = RegistryManager.getInstance();

        Optional<Relation> sysAdminRel = registryManager.getRelations().stream()
                .filter(r -> r.getPlayerId() == company.bob.hashCode()
                        && r.getRoleName().matches(".*SysAdmin"))
                .findFirst();
        assertTrue(sysAdminRel.isPresent());

        //Assert that SysAdmin role instance has been transferred

    }




    /**
     * Test role transferring in different compartment
     */

    Person m_alice = new Person();

    class Faculty extends Compartment{

        public void configureBinding(){
            m_alice.bind(this, Employee.class);
            m_alice.bind(this, Student.class);
        }
    }

    public static class Mensa extends Compartment{

    }

    @Test
    public void transferRoleInDifferentCompartments() {
        Faculty faculty = new Faculty();
        faculty.configureBinding();

        try (Mensa alteMensa = Compartment.initialize(Mensa.class)) {
            m_alice.transfer(Student.class, faculty, m_alice, alteMensa);

            RegistryManager registryManager = RegistryManager.getInstance();
            Optional<Relation> studentRel = registryManager.getRelations().stream()
                    .filter(r -> r.getPlayerId() == m_alice.hashCode() && r.getCompartmentId() == alteMensa.hashCode()
                            && r.getRoleName().matches(".*Student"))
                    .findFirst();

            assertTrue(studentRel.isPresent());
        }

    }

    public static class A extends Role{
        public String whatName(){
            //invoke method role other roles being in the play line
            return invoke("getName", String.class);
        }

        public String getName(){
            return "A";
        }

        public String callBase(){
            return invokeBase("getName", String.class);
        }
    }

    public static class B extends Role{
        public String getName(){
            return "B";
        }

        //Invoke Base for roleInvokeBaseWhichIsRole
        public String callBase(){
            return invokeBase("getName", String.class);
        }
    }

    public static class C extends Role{
        public String getName(){
            return "C";
        }
    }

    public static class D extends Role{
        public String getName(){
            return "D";
        }
    }

    public static class E extends Role{
        public String me(){
            return "E";
        }

//        public String getName(){
//            return "E";
//        }
    }

    public static class F extends Role{
        public String getName() { return "F";}
    }

    @Test
    public void checkSequenceAndLevelAfterTransferring(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            /*
            alice--->A---->B----->E
                      \--->D----->C
             */
            Person alice = Player.initialize(Person.class);

            Role a = alice.bind(A.class);
            a.bind(B.class).bind(E.class);
            Role d = a.bind(D.class);
            d.bind(C.class);

            RegistryManager registryManager = RegistryManager.getInstance();
            //DumpHelper.dumpRelation(registryManager.getRelations());

            Person bob = Player.initialize(Person.class);
            bob.bind(F.class);
            //bob.bind(A.class).bind(B.class).bind(C.class);
            alice.transfer(A.class, bob);

//            System.out.println("After transfer");
//            DumpHelper.dumpRelation(registryManager.getRelations());

            //DumpHelper.printTree(registryManager.getRelations(), comp);

            //Assert that no role on previous player alice
            Assert.assertEquals(0, StatisticsHelper.rolesCount(alice.hashCode()));

            //Assert that there 6 roles (including F.class) on bob
            Assert.assertEquals(6, StatisticsHelper.rolesCount(bob.hashCode()));

            //Assert A.class has 4 roles under its relation
            Assert.assertEquals(4, StatisticsHelper.rolesCount(a.hashCode()));

        }
    }
}


