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
 * Created by nguonly role 7/17/15.
 */
public class InvokeTest {
    //private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        //DBManager.close();
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

    public static class TeachingAssistant extends Role {
        public String takeCourse(String course){
            return TeachingAssistant.class.getSimpleName() + " takes " + course;
        }

        public boolean isBusyOn(String date){
            boolean obj = invoke("getSchedule", boolean.class, new Class[]{String.class}, new Object[]{date});
            return obj;
        }
    }

    public static class Employee extends Role{
        public String joinMeeting(String meeting){
            return "Join Meeting role " + meeting;
        }

        public boolean getSchedule(String date){
            System.out.println("Has a meeting on " + date.toString());
            return true;
        }
    }

    @Test
    public void roleInvokeAnotherRoleMethod(){
        try(Compartment comp = Compartment.initialize(Compartment.class)) {
            String course = "Computer Networks";
            String date = "17-07-2015";
            Person p = Player.initialize(Person.class);

            Role student = p.bind(Student.class);
            student.bind(TeachingAssistant.class).bind(Employee.class);
            boolean b = p.invoke("isBusyOn", boolean.class, new Class[]{String.class}, new Object[]{date});
            Assert.assertTrue(b);
            //String retValue = (String)p.invoke("takeCourse", new Class[]{String.class}, new Object[]{course});

            //Assert.assertEquals("This student takes " + course, retValue);
        }
    }

    /**
     * Test Role Method Dispatch
     */

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

    public static class CompartmentA extends Compartment{

    }

    @Test
    public void invokeDynamicMethodInsideRole(){
        try(CompartmentA comp = Compartment.initialize(CompartmentA.class)) {
            Person p = Person.initialize(Person.class);

            Role a = p.bind(comp, A.class);
            a.bind(comp, B.class).bind(comp, C.class).bind(comp, D.class);
            a.bind(E.class);

            String retValue = p.invoke("whatName", String.class);

            Assert.assertEquals("D", retValue);
        }
    }

    @Test
    public void invokeDynamicMethodPolymorphism(){
        try(CompartmentA comp = Compartment.initialize(CompartmentA.class)) {
            Person p = Player.initialize(Person.class);

            Role a = p.bind(A.class);
            Role b = a.bind(B.class);
            //b.bind(C.class);
            Role d = a.bind(D.class);
            d.bind(C.class);

            String retValue = p.invoke("whatName", String.class);
            Assert.assertEquals("C", retValue);

            d.unbind(C.class);

            retValue = p.invoke("whatName", String.class);
            Assert.assertEquals("D", retValue);
        }
    }

    @Test
    public void roleInvokeBaseWhichIsRole(){
        try(Compartment comp = Compartment.initialize(Compartment.class)) {
            Person p = Player.initialize(Person.class);

            p.bind(A.class).bind(B.class);

            //Call method callBase in B.class and that method will invoke base which is A.class
            String retValue = p.invoke("callBase", String.class);

            Assert.assertEquals("A", retValue);
        }
    }

    public static class P extends Player{
        public String getName(){
            return "P";
        }
    }

    @Test
    public void roleInvokeBaseWhichIsPlayer(){
        try(Compartment comp = Compartment.initialize(Compartment.class)) {
            P p = Player.initialize(P.class);
            p.bind(A.class);

            //call callBase in role A.class. callBase method will invokeBase in P.class
            String retValue = p.invoke("callBase", String.class);

            Assert.assertEquals("P", retValue);
        }
    }

}
