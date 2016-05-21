package net.runtime.role.unittest;


import net.runtime.role.actor.Compartment;
import net.runtime.role.actor.Player;
import net.runtime.role.actor.Role;
import net.runtime.role.orm.Relation;
import net.runtime.role.registry.RegistryManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * Created by nguonly role 7/27/15.
 */
public class TypeSafeRoleInvocationTest {
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

        public int getMatriculationNo(){return matriculationNo;}
    }

    @Test
    public void simpleInvocation(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            Person p = Player.initialize(Person.class);
            p.bind(Employee.class);
            p.bind(Student.class);

            String course = "Networking";
            String retCourse = "This student takes " + course;
            String retAddress = "Employee printAddress";

            Assert.assertEquals(retCourse, p.role(Student.class).takeCourse("Networking"));
            Assert.assertEquals(retAddress, p.role(Employee.class).getAddress());
        }
    }

    @Test
    public void ensureInvocationHappenedAtInstanceTest(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            Person ana = Player.initialize(Person.class);
            Person ely = Player.initialize(Person.class);

            ana.bind(Student.class);
            ely.bind(Student.class);

            Assert.assertNotEquals(ana.role(Student.class).getMatriculationNo(), ely.role(Student.class).getMatriculationNo());
        }
    }


    @Test
    public void testCollection(){
        Collection<Relation> relations = new ArrayList<>();
        Deque<Relation> deque = new ArrayDeque<>();

        //relations.stream().filter()
        HashMap<Integer, Relation> map = new HashMap<>();

    }
}
