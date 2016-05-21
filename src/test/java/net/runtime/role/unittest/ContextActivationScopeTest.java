package net.runtime.role.unittest;

import net.runtime.role.actor.Compartment;
import net.runtime.role.actor.Player;
import net.runtime.role.actor.Role;
import net.runtime.role.registry.RegistryManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayDeque;

/**
 * Created by nguonly role 7/22/15.
 */
public class ContextActivationScopeTest {
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
    /////// End of data preparation

    public static class RoleA extends Role {
        public String getName(){
            return this.getClass().getSimpleName();
        }

        public void abc(){
            System.out.println("abc");
        }

        public void abc(int i){
            System.out.println(i);
        }

        public String abc(int i, int j){
            return "a";
        }
    }

    public static class RoleB extends Role{
        public String getName(){
            return this.getClass().getSimpleName();
        }
    }

    static Person p = Player.initialize(Person.class);

    public static class MyCompartment extends Compartment {

        public void bind(){
            p.bind(this, RoleA.class);
        }

    }

    public static class CompartmentA extends Compartment{

        public void bind(){
            p.bind(this, RoleB.class);
        }

    }

    @Test
    public void simpleContextScopeActivation(){
        try(MyCompartment comp = Compartment.initialize(MyCompartment.class)){
            comp.bind();

            System.out.println("Entering --- " + comp);
            String retValue = p.invoke("getName", String.class);
            Assert.assertEquals(RoleA.class.getSimpleName(), retValue);
            try(CompartmentA ca = Compartment.initialize(CompartmentA.class)){
                ca.bind();

                System.out.println("Entering --- " + ca);
                retValue = p.invoke("getName", String.class);
                Assert.assertEquals(RoleB.class.getSimpleName(), retValue);
            }

            retValue = p.invoke("getName", String.class);
            Assert.assertEquals(RoleA.class.getSimpleName(), retValue);
        }

    }

    @Test
    public void checkReflectionMethodSignature(){
        RoleA r = new RoleA();

        Method[] methods = r.getClass().getDeclaredMethods();
        for(Method m : methods){
            System.out.println(m);
        }
    }

    @Test
    public void test(){
        //Class returnType = String.class;
        Class returnType = null;
        String methodName = "getName";
        //Class[] clazzes = new Class[]{int.class, int.class};
        Class[] clazzes = null;
        StringBuilder sb = new StringBuilder();
        sb.append("%").append(returnType==null?"":" " + returnType.getName());
        sb.append(" %.").append(methodName).append("(");

        if(clazzes!=null) {
            for (int i = 0; i < clazzes.length; i++) {
                sb.append(clazzes[i].getSimpleName());
                if (i < clazzes.length - 1) sb.append(",");
            }
        }
        sb.append(")");

        System.out.println(sb.toString());
    }
}
