package net.runtime.role.unittest;

import net.runtime.role.actor.Compartment;
import net.runtime.role.helper.DumpHelper;
import net.runtime.role.orm.Relation;
import net.runtime.role.registry.RegistryManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Optional;

/**
 * Created by nguonly on 3/24/16.
 */
public class BoundTimeTest {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        RegistryManager.getInstance().setRelations(null);
    }

    //Prepare data
    public static class R1{
        public void invokeMe(){
            System.out.println("invokeMe: R1");
        }
    }

    public static class R2{
        public void invokeMe(){
            System.out.println("invokeMe: R2");
        }
    }

    public static class Person{
        public void invokeMe(){
            System.out.println("invokeMe: Person");
        }
    }

    @Test
    public void check_Bound_Time_Is_Present(){
        RegistryManager reg = RegistryManager.getInstance();
        Person p = reg.newPlayer(Person.class, null, null);

        LocalDateTime startTime = LocalDateTime.now();

        try(Compartment comp = Compartment.initialize(Compartment.class)){
            reg.bind(comp, p, R1.class, null, null);

            reg.bind(comp, p, R2.class, null, null);

            ArrayDeque<Relation> relations = reg.getRelations();

            Optional<Relation> rel = relations.stream().filter(
                    c -> c.getRoleName().contains("R1")
            ).findFirst();
            Assert.assertTrue(rel.isPresent());
            Assert.assertTrue(rel.get().getBoundTime().isAfter(startTime));
        }
    }
}
