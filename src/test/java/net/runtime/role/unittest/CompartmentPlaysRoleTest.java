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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by nguonly role 7/10/15.
 */
public class CompartmentPlaysRoleTest {
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

    public static class Student extends Role {
        int matriculationNo = this.hashCode();

        public String takeCourse(String course){
            return "This student takes " + course;
        }
    }

    public static class Employee extends Role {
        public String getAddress(){
            return "Employee printAddress";
        }
    }

    public static class TeachingAssistant extends Role{
        public void collectHomework(){
            System.out.println("Collect Homework");
        }
    }

    public static class Sponsor extends Role {
    }

    public static class Competitor extends Role {
    }

    /////// End of Prepared Data

    /**
     * Prepare data for testing. It's all about compartment.
     */
    public static class Faculty extends Compartment {
        public void configureBinding(){
            Person p = new Person();
            p.bind(this, Student.class).bind(this, TeachingAssistant.class);
            p.bind(this, Employee.class);
        }
    }

     @Test
    public void compartmentPlaysRoles(){
         RegistryManager registryManager = RegistryManager.getInstance();

         //compartment as a context
         try (Faculty faculty = Compartment.initialize(Faculty.class)) {
             faculty.configureBinding();

             Map<Integer, List<Relation>> maps = registryManager.getRelations().stream()
                     .filter(r->r.getCompartmentId() == faculty.hashCode())
                     .collect(Collectors.groupingBy(r->r.getRoleId()));

             Assert.assertEquals(3, maps.keySet().size());

         }

         //Compartment plays role
         try (Compartment comp = Compartment.initialize(Compartment.class)) {
             Faculty faculty = Player.initialize(Faculty.class);
             faculty.bind(Sponsor.class);

             Optional<Relation> rel = registryManager.getRelations().stream()
                     .filter(r -> r.getCompartmentId() == comp.hashCode())
                     .findFirst();

             Assert.assertTrue(rel.get().getRoleName().contains("Sponsor"));
         }

    }

    public static class University extends Compartment{
        public void configureBinding(){
            Faculty faculty = new Faculty();
            faculty.configureBinding();

            faculty.bind(this, Sponsor.class);
        }
    }

    public static class Germany extends Compartment{
        public void configureBinding(){
            University tuDresden = new University();
            tuDresden.configureBinding();

            tuDresden.bind(this, Competitor.class);
        }
    }

    @Test
    public void multiLevelCoarseGrained(){
        try(Germany germany = Compartment.initialize(Germany.class)) {
            germany.configureBinding();

            RegistryManager registryManager = RegistryManager.getInstance();
            Map<Integer, List<Relation>> maps = registryManager.getRelations().stream()
                    .collect(Collectors.groupingBy(p -> p.getCompartmentId()));

            //DumpHelper.dumpRelation(registryManager.m_relations);

            Assert.assertEquals(3, maps.keySet().size());
        }
    }
}
