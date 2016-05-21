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

import java.util.ArrayDeque;
import java.util.Iterator;

/**
 * Created by nguonly role 7/10/15.
 */
public class RoleDependOnCompartmentTest {
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

    class Faculty extends Compartment {
        Faculty(){
            Person p = new Person();
            p.bind(this, Student.class);
            p.bind(this, Employee.class);
        }
    }

    @Test
    public void roleDependsOnCompartment(){
        //try(Compartment comp = Compartment.initialize(Compartment.class)) {
            Faculty faculty = new Faculty();

            RegistryManager registryManager = RegistryManager.getInstance();
            Iterator<Relation> iterator = registryManager.getRelations().iterator();
            Assert.assertTrue(iterator.next().getCompartmentId()>0);
            Assert.assertTrue(iterator.next().getRoleName().contains("Employee"));
        //}
    }
}
