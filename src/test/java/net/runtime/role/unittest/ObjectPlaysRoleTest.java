package net.runtime.role.unittest;

import net.runtime.role.actor.Compartment;
import net.runtime.role.actor.Player;
import net.runtime.role.actor.Role;
import net.runtime.role.helper.DumpHelper;
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
public class ObjectPlaysRoleTest {
    @Before
    public void setupSchema(){
        RegistryManager registryManager = RegistryManager.getInstance();
        registryManager.setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager registryManager = RegistryManager.getInstance();
        registryManager.setRelations(null);
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

    public static class SalePerson extends Role {
        public String sale(String item, int n){
            return String.format("Sale %s with %d quantities", item, n);
        }
    }

    public static class Teacher extends Role {
    }

    public static class TeamLeader extends Role {
    }

    public static class CompartmentA extends Compartment {
        public void bind(){

        }
    }

    @Test
    public void objectPlaysRole(){
        try(CompartmentA comp = Compartment.initialize(CompartmentA.class)){
            Person p = Player.initialize(Person.class);
            Role emp = p.bind(comp, Employee.class);
            emp.bind(comp, SalePerson.class);

            String item = "Coffee";
            int quantity = 40;
            String retStr = p.invoke("sale", String.class, new Class[]{String.class, int.class}, new Object[]{item, quantity});
            Assert.assertEquals(String.format("Sale %s with %d quantities", item, quantity), retStr);

            String ret = p.invoke("getAddress", String.class);
            Assert.assertEquals("Employee printAddress", ret);
        }
    }

    @Test
    public void unbindRoleFromObject(){
        try(Compartment comp = Compartment.initialize(Compartment.class)) {
            Person p = new Person();
            p.bind(Employee.class).bind(SysAdmin.class);
            p.bind(Student.class);
            p.bind(Teacher.class);

            p.unbind(Employee.class);

            RegistryManager registryManager = RegistryManager.getInstance();
            Iterator<Relation> iterator = registryManager.getRelations().iterator();

            Assert.assertTrue(iterator.next().getRoleName().contains("Student"));
            Assert.assertTrue(iterator.next().getRoleName().contains("Teacher"));
        }
    }

    @Test
    public void unbindAll(){
        try(Compartment comp = Compartment.initialize(Compartment.class)) {
            Person p = new Person();
            p.bind(Employee.class).bind(SysAdmin.class);
            p.bind(Student.class);
            p.bind(Teacher.class);

            p.unbindAll();

            RegistryManager registryManager = RegistryManager.getInstance();

            Assert.assertTrue(registryManager.getRelations().isEmpty());
        }
    }

    @Test
    public void rebind(){
        try(Compartment comp = Compartment.initialize(Compartment.class)) {
            Person p = new Person();
            p.bind(Employee.class).bind(TeamLeader.class);
            p.bind(Student.class);
            DumpHelper.dumpRelation();
            p.unbind(Employee.class);

            p.bind(Employee.class);

            RegistryManager registryManager = RegistryManager.getInstance();

            Iterator<Relation> iterator = registryManager.getRelations().iterator();

            Assert.assertTrue(iterator.next().getRoleName().contains("Student"));
            Assert.assertTrue(iterator.next().getRoleName().contains("Employee"));
        }
    }

    @Test
    public void prohibitConstraint(){
        try(Compartment comp = Compartment.initialize(Compartment.class)) {
            Person p = new Person();
            p.bind(Employee.class).prohibit(Student.class);
            //p.prohibit(Student.class); //This is not a case

            RegistryManager registryManager = RegistryManager.getInstance();

            //DumpHelper.dumpRelation(registryManager.m_relations);
        }
    }

}
