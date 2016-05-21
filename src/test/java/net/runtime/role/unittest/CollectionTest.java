package net.runtime.role.unittest;

import net.runtime.role.actor.Compartment;
import net.runtime.role.actor.Player;
import net.runtime.role.actor.Role;
import net.runtime.role.helper.RelationSortHelper;
import net.runtime.role.orm.Relation;
import net.runtime.role.registry.RegistryManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by nguonly on 7/28/15.
 */
public class CollectionTest {
    @Before
    public void setupSchema(){
        RegistryManager.getInstance().setRelations(new ArrayDeque<>());
    }

    @After
    public void destroyDBConnection(){
        //DBManager.close();
        RegistryManager.getInstance().setRelations(null);
    }

    public static class Person extends Player {
    }

    public static class RoleA extends Role {
        public String getName(){
            return this.getClass().getName();
        }

        public void setName(String value){

        }
    }

    public static class RoleB extends Role{
        public String getName(){
            return this.getClass().getName();
        }

        public void setName(String value){

        }
    }

    public static class RoleC extends Role{
        public String getName(){
            return this.getClass().getName();
            //return 0;
        }

        public void setName(String value){

        }
    }

    public static class RoleD extends Role{
        public String getName(){
            return this.getClass().getName();
        }

        public void setName(String value){

        }
    }

    public static class RoleE extends Role{
        public String getName(){
            return this.getClass().getName();
        }

        public void setName(String value){

        }
    }

    @Test
    public void simpleBind(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            Person p = Player.initialize(Person.class);
            Role a = p.bind(RoleA.class);
            Role b = a.bind(RoleB.class);
            b.bind(RoleD.class);
            a.bind(RoleC.class);
            b.bind(RoleE.class);
            //p.bind(RoleB.class);

            String retStr = p.invoke("getName", String.class);
            Assert.assertEquals(RoleC.class.getName(), retStr);

            RegistryManager registryManager = RegistryManager.getInstance();

            String methodName = ".* java.lang.String .*.getName\\(\\)";
            int compartmentId = comp.hashCode();
            int objId = p.hashCode();

            Optional<Relation> rel = registryManager.getRelations().stream()
                    .filter(c -> c.getCompartmentId() == compartmentId
                            && c.getObjectId() == objId
                            && c.getMethodName().matches(methodName))
                    .sorted(RelationSortHelper.SEQUENCE_DESC.thenComparing(RelationSortHelper.TYPE_DESC))
                    .findFirst();

            Assert.assertTrue(rel.isPresent());

            Assert.assertTrue(rel.get().getRoleName().equals(RoleC.class.getName()));

        }
    }

    @Test
    public void roleInvokeRole(){
        try(Compartment comp = Compartment.initialize(Compartment.class)) {
            Person p = Player.initialize(Person.class);
            Role a = p.bind(RoleA.class);
            Role b = a.bind(RoleB.class);
            b.bind(RoleD.class);
            a.bind(RoleC.class);
            b.bind(RoleE.class);

            RegistryManager registryManager = RegistryManager.getInstance();

            int compartmentId = comp.hashCode();
            int rolePlayerId = b.hashCode();
            String methodSignature = ".* java.lang.String .*.getName\\(\\)";

            List<Relation> rel = registryManager.getRelations().stream()
                    .filter(c -> c.getCompartmentId() == compartmentId && (c.getPlayerId() == rolePlayerId || c.getRoleId() == rolePlayerId)
                            && c.getMethodName().matches(methodSignature))
                    .sorted((s1, s2) -> s1.getSequence() > s2.getSequence() ? -1 : s1.getSequence() == s2.getSequence() ? 0 : +1)
                    .sorted((s1, s2) -> s1.getType() > s2.getType() ? -1 : s1.getType() == s2.getType() ? 0 : +1)
                    .collect(Collectors.toList());

            System.out.println("==============");
            for(Relation r : rel){
                System.out.format("%d %d %d %d %d %d %s %s\n", r.getCompartmentId(), r.getObjectId(),
                    r.getPlayerId(), r.getRoleId(), r.getSequence(), r.getType(),
                    r.getRoleName(), r.getMethodName());
            }
        }
    }

    @Test
    public void unbindAll(){
        try(Compartment comp = Compartment.initialize(Compartment.class)){
            Person p = Player.initialize(Person.class);

            Role a = p.bind(RoleA.class);
            Role b = a.bind(RoleB.class);
            b.bind(RoleD.class);
            a.bind(RoleC.class);
            b.bind(RoleE.class);

            p.unbindAll();

            Assert.assertTrue(RegistryManager.getInstance().getRelations().isEmpty());
        }
    }
}
