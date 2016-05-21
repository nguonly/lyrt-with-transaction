package net.runtime.role.unittest;

import net.runtime.role.actor.Compartment;
import net.runtime.role.orm.Relation;
import net.runtime.role.registry.Transaction;
import net.runtime.role.registry.RegistryManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by nguonly on 3/25/16.
 */
public class TransactionTest {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager.getInstance().setRelations(null);
    }

    public static class R1{
        public String invokeMe(){
            return this.getClass().getName();
        }
    }

    public static class R2{
        public String invokeMe(){
            return this.getClass().getName();
        }
    }

    public static class Person{
        public String invokeMe(){
            return this.getClass().getName();
        }
    }

    @Test
    public void validate_transaction(){
        RegistryManager reg = RegistryManager.getInstance();
        Compartment comp = Compartment.initialize(Compartment.class);
        comp.activate();

        Person p = reg.newPlayer(Person.class, null, null);

        //Executing method thread
        Runnable r1 = () -> {
            try(Transaction tx=new Transaction()){
                for(int i=0; i<50; i++) {
                    String ret = reg.invokeRole(comp, p, "invokeMe", String.class, null, null);
                    //System.out.println(ret);
                    Assert.assertTrue(ret.contains("Person"));
                    //System.out.println("Executing in " + Thread.currentThread());
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        //Binding in different thread
        Runnable r2 = () -> {
            reg.bind(comp, p, R1.class, null, null);
            ArrayDeque<Relation> relations = reg.getRelations();
            Relation rel = relations.stream().filter(
                    c->c.getRoleName().contains("R1")
            ).findFirst().get();
            Assert.assertTrue(rel.getRoleName().contains("R1"));
            //System.out.println("Binding finished");
        };

        //invoke in different thread
        Runnable r3 = () -> {
            //wait until r2 Thread finished
            try {
                for(int i=0; i<50; i++) {
                    String ret = reg.invokeRole(comp, p, "invokeMe", String.class, null, null);
                    //System.out.println("R3 ::: " + ret);
                    Assert.assertTrue(ret.contains("R1"));
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
        executor.execute(r1);
        executor.schedule(r2, 100, TimeUnit.MILLISECONDS);
        executor.schedule(r3, 200, TimeUnit.MILLISECONDS);

        executor.shutdown();

        while(!executor.isTerminated()){}

        comp.deActivate();
    }
}
