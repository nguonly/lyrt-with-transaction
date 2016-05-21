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
 * Created by nguonly on 9/18/15.
 */
public class InvokeRootTest {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager.getInstance().setRelations(null);
    }

    public static class Anything extends Player {
        private int id;

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public static class Person extends Role {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Student extends Role{
        private int studentId;

        public int getStudentId() {
            return studentId;
        }

        public void setStudentId(int studentId) {
            this.studentId = studentId;
        }

        public String enroll(){
            int id = invokeCore("getId", int.class);
            String name = invokeBase("getName", String.class);
            setStudentId(11111);

            String ret = String.format("Register %s(%d) with %d", name, id, studentId);
            return ret;
        }
    }

    @Test
    public void invokeRootTest(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            Anything any = new Anything();

            any.bind(Person.class).bind(Student.class);

            any.setId(9);
            any.role(Person.class).setName("lycog");
            String str = any.role(Student.class).enroll();

            Assert.assertEquals("Register lycog(9) with 11111", str);
        }
    }
}
