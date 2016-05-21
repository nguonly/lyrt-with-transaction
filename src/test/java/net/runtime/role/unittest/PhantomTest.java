package net.runtime.role.unittest;

import net.runtime.role.actor.Compartment;
import net.runtime.role.actor.Player;
import net.runtime.role.helper.DumpHelper;
import net.runtime.role.registry.RegistryManager;
import net.runtime.role.registry.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by nguonly on 5/19/16.
 */
public class PhantomTest {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager.getInstance().setRelations(null);
    }

    public static class Person {

    }

    public static class Student{
        public String speak(){
            return "Student";
        }
    }

    public static class TeachingAssistant{
        public String speak(){
            return "TeachingAssistant";
        }
    }

    public static class Father{
        public String speak(){
            return "Father";
        }
    }

    /**
     * Person p binds to Father and Student @ Time T
     * Transaction is started @ Time T1 --> Student role will be invoked
     * Student is unbound from Person p @ Time T2  --> Mark as phantom
     * @throws Throwable
     */
    @Test
    public void testRoleIsStillThere() throws Throwable{
        RegistryManager reg = RegistryManager.getInstance();
        Compartment comp = Compartment.initialize(Compartment.class);
        comp.activate();

        Person p = reg.newPlayer(Person.class, null, null);

        reg.bind(comp, p, Father.class, null, null);
        reg.bind(comp, p, Student.class, null, null);

        Runnable unbinding = () -> {
            System.out.println("Unbind performs");
            reg.unbind(p, Student.class);
        };
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);
        pool.schedule(unbinding, 50, TimeUnit.MILLISECONDS);

        try(Transaction tx = new Transaction()){
            for(int i=0; i<50; i++){
                String ret = reg.invokeRole(comp, p, "speak", String.class, null, null);
                Assert.assertEquals("Student", ret);

                Thread.sleep(10);
            }
        }

//        DumpHelper.dumpRelation();

        pool.shutdown();

        comp.deActivate();
    }

    /**
     * Person binds to Father and Student
     * Run Tx1 @ T until T+10 --> return Student
     * Run Unbinding Student @ T+1 --> Phantom Student
     * Run Tx2 @ T+2 until T+8 --> return Father (Student is phantom then ignore from invocation)
     * @throws Throwable
     */
    @Test
    public void testEnsuringConsistencyWhileRoleIsRemoved() throws Throwable{
        RegistryManager reg = RegistryManager.getInstance();
        Compartment comp = Compartment.initialize(Compartment.class);
        comp.activate();

        Person p = reg.newPlayer(Person.class, null, null);

        reg.bind(comp, p, Father.class, null, null);
        reg.bind(comp, p, Student.class, null, null);

        Runnable unbinding = () -> {
            System.out.println("Unbind performs @ " + LocalDateTime.now());
            reg.unbind(p, Student.class);
//            DumpHelper.dumpRelation();
        };

        Runnable newInvoking = () -> {
            try(Transaction tx = new Transaction()){
                for(int i=0; i<30; i++){
                    String ret = reg.invokeRole(comp, p, "speak", String.class, null, null);
                    Assert.assertEquals("Father", ret);

                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);
        pool.schedule(unbinding, 50, TimeUnit.MILLISECONDS);
        pool.schedule(newInvoking, 90, TimeUnit.MILLISECONDS);

        try(Transaction tx = new Transaction()){
            for(int i=0; i<50; i++){
                String ret = reg.invokeRole(comp, p, "speak", String.class, null, null);
                Assert.assertEquals("Student", ret);

                Thread.sleep(10);
            }
        }

        DumpHelper.dumpRelation();

        pool.shutdown();

        comp.deActivate();
    }

    /**
     * Person p binds to Student and Father at Time T
     * Tx1 executes at Time T+1
     * Tx2 executes at Time T+2
     * Tx3 (unbinding Father) executes at Time T+3
     * Tx2 ends at T+4 --> Father won't be removed because Tx2 is still active
     * Tx1 ends at T+5 --> Father role is removed because it's the last transaction running before phantom.
     */
    @Test
    public void testOnlyTheLastTransactionBeforePhantomTimeCanRemoveRoles() throws Throwable{
        RegistryManager reg = RegistryManager.getInstance();
        Person p = Player.initialize(Person.class);
        Compartment comp = Compartment.initialize(Compartment.class);

        comp.activate();

        reg.bind(comp, p, Student.class, null, null);
        reg.bind(comp, p, Father.class, null, null);

        Runnable tx2 = () -> {
            try(Transaction t2 = new Transaction()){
                for(int i=0; i<100; i++) {
                    String ret = reg.invokeRole(comp, p, "speak", String.class, null, null);
                    Assert.assertEquals("Father", ret);

                    Thread.sleep(20);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            DumpHelper.dumpRelation();
        };

        Runnable tx3 = () -> {
            System.out.println("Unbind Father Role @ " + LocalDateTime.now());
            reg.unbind(p, Father.class);
        };

        ScheduledExecutorService pool = Executors.newScheduledThreadPool(3);
//        pool.schedule(tx1, 0, TimeUnit.MILLISECONDS);
        pool.schedule(tx2, 30, TimeUnit.MILLISECONDS);
        pool.schedule(tx3, 50, TimeUnit.MILLISECONDS);


        try (Transaction t1 = new Transaction()) {
            for (int i = 0; i < 200; i++) {
                String ret = reg.invokeRole(comp, p, "speak", String.class, null, null);
                Assert.assertEquals("Father", ret); //If put in thread, the assert fail but not raise an exception

                Thread.sleep(30);
            }
        }

        pool.shutdown();

        while(!pool.isTerminated()){}
    }

    /**
     * Person p binds to Student and Father at Time T
     * Tx1 executes at Time T+1
     * Tx2 executes at Time T+2
     * Tx3 (unbinding Father) executes at Time T+3
     * Tx1 ends at T+4 --> Father won't be removed because Tx2 is still active
     * Tx2 ends at T+5 --> Father role is removed because it's the last transaction running before phantom.
     */
    @Test
    public void testRemvoingPhantom() throws Throwable{
        RegistryManager reg = RegistryManager.getInstance();
        Person p = Player.initialize(Person.class);
        Compartment comp = Compartment.initialize(Compartment.class);

        comp.activate();

        reg.bind(comp, p, Student.class, null, null);
        reg.bind(comp, p, Father.class, null, null);

        Runnable tx2 = () -> {
            try(Transaction t2 = new Transaction()){
                for(int i=0; i<200; i++) {
                    String ret = reg.invokeRole(comp, p, "speak", String.class, null, null);
//                    System.out.println(ret);
                    Assert.assertEquals("Father", ret);

                    Thread.sleep(20);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            DumpHelper.dumpRelation();
        };

        Runnable tx3 = () -> {
            System.out.println("Unbind Father Role @ " + LocalDateTime.now());
            reg.unbind(p, Father.class);
        };

        ScheduledExecutorService pool = Executors.newScheduledThreadPool(3);
//        pool.schedule(tx1, 0, TimeUnit.MILLISECONDS);
        pool.schedule(tx2, 30, TimeUnit.MILLISECONDS);
        pool.schedule(tx3, 50, TimeUnit.MILLISECONDS);


        try (Transaction t1 = new Transaction()) {
            for (int i = 0; i < 100; i++) {
                String ret = reg.invokeRole(comp, p, "speak", String.class, null, null);
                Assert.assertEquals("Father", ret); //If put in thread, the assert fail but not raise an exception

                Thread.sleep(30);
            }
        }

        pool.shutdown();

        while(!pool.isTerminated()){}
    }

    /**
     * Person p binds to Father and Student and then Student binds to TeachingAssistant role.
     * Tx1 executing in main thread from time T to T+10 --> TeachingAssistant will be invoked.
     * Tx2 executing in thread to unbind Student --> Student and TeachingAssistant are phantom.
     * @throws Throwable
     */
    @Test
    public void testMarkingPhantomInRolePlaysRole() throws Throwable{
        RegistryManager reg = RegistryManager.getInstance();
        Person p = Player.initialize(Person.class);
        Compartment comp = Compartment.initialize(Compartment.class);
        comp.activate();

        reg.bind(comp, p, Father.class, null, null);
        Student stu = reg.bind(comp, p, Student.class, null, null);
        reg.bind(comp, stu, TeachingAssistant.class, null, null);

        Runnable t2 = () -> {
            System.out.println("Unbind Student @ " + LocalDateTime.now());
            reg.unbind(p, Student.class);
//            DumpHelper.dumpRelation();
        };

        ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);
        pool.schedule(t2, 50, TimeUnit.MILLISECONDS);

        try(Transaction tx1 = new Transaction()){
            for(int i=0; i<100; i++){
                String ret = reg.invokeRole(comp, p, "speak", String.class, null, null);
                Assert.assertEquals("TeachingAssistant", ret);

                Thread.sleep(20);
            }
        }

        //From this point onward, only Father role is associated.
        String ret = reg.invokeRole(comp, p, "speak", String.class, null, null);
        Assert.assertEquals("Father", ret);

        pool.shutdown();
//        DumpHelper.dumpRelation();
    }
}
