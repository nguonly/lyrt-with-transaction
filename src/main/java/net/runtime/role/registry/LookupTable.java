package net.runtime.role.registry;

import net.runtime.role.helper.DumpHelper;
import net.runtime.role.helper.RelationSortHelper;
import net.runtime.role.orm.PlayRelationEnum;
import net.runtime.role.orm.Relation;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadFactory;

/**
 * Created by nguonly on 1/26/16.
 */
public class LookupTable {
    private static int m_number_level = 4*4 + 1;

    /**
     * Register Core/Root/Natural object playing role relation
     * @param compartment
     * @param player is the core object
     * @param role is the role to be played
     */
    public void registerObjectPlayingRoleRelation(Object compartment, Object player, Object role){
        ArrayDeque<Relation> m_relations = RegistryManager.getInstance().getRelations();
        int compartmentId = compartment.hashCode();

        //find sequence for each level
        Optional<Relation> distinct = m_relations.stream()
                .filter(c -> c.getCompartmentId() == compartmentId
                        && c.getPlayerId() == player.hashCode())
                .sorted(RelationSortHelper.SEQUENCE_DESC)
                .findFirst();

        long seq = 0;
        if(distinct.isPresent()) seq = distinct.get().getSequence();

        //seq++;
        long c = (long)Math.pow(10, m_number_level);
        if(seq ==0) {
            seq = c;
        }else {
            seq = ((seq/c) +1)*c;
        }

        //Register Role's methods
        String playerName = player.getClass().getName();
        Method[] methods = role.getClass().getDeclaredMethods();

        Relation relation = new Relation();
        relation.setCompartmentId(compartmentId);
        relation.setCompartmentName(compartment.getClass().getName());
        relation.setObjectId(player.hashCode());
        relation.setObjectName(playerName);
        relation.setPlayerId(player.hashCode());
        relation.setPlayerName(playerName);
        relation.setRoleId(role.hashCode());
        relation.setRoleName(role.getClass().getName());
        relation.setLevel(1);
        relation.setType(PlayRelationEnum.OBJECT_PLAYS_ROLE.getCode());
        relation.setSequence(seq);

        //added by lycog on 24.03.2016 to address inconsistency on black-box transaction
        relation.setBoundTime(LocalDateTime.now());

        //Register Role's methods
        if(methods.length>0) {
            for (Method m : methods) {
                relation.setMethodName(m.toString());

                m_relations.add(new Relation(relation));
            }
        }else{
            relation.setMethodName(""); //if null then, the match (RegEx) won't work on method invocation
            m_relations.add(new Relation(relation));
        }
    }

    /**
     * Register role playing role relation
     * @param compartment
     * @param rolePlayer is the role instance acting as player
     * @param role is the role to be played/bound
     */
    public void registerRolePlayingRoleRelation(Object compartment, Object rolePlayer, Object role){
        ArrayDeque<Relation> m_relations = RegistryManager.getInstance().getRelations();
        int compartmentId = compartment.hashCode();

        //get root object or natural type
        Optional<Relation> coreObjRelation = m_relations.stream()
                .filter(c -> c.getRoleId() == rolePlayer.hashCode()
                        && c.getCompartmentId() == compartmentId)
                .sorted(RelationSortHelper.SEQUENCE_DESC)
                .findFirst();

        long seq = 0;
        int lvl = 0;
        long c;
        int objId=0;
        String objName = "";
        if(coreObjRelation.isPresent()){
            objId = coreObjRelation.get().getObjectId();
            objName = coreObjRelation.get().getObjectName();
        }

//            log.debug("Core Id {}", objId);

        //check if core has previous bound role
        Optional<Relation> latestRole = m_relations.stream()
                .filter(x -> x.getCompartmentId() == compartmentId
                        && x.getPlayerId() == rolePlayer.hashCode()
                        && x.getType() != PlayRelationEnum.INHERITANCE.getCode())
                .sorted(RelationSortHelper.SEQUENCE_DESC)
                .findFirst();

        if(latestRole.isPresent()){
            lvl = latestRole.get().getLevel();
            seq = latestRole.get().getSequence();
            c = (long)Math.pow(10, m_number_level - 2*(lvl-1));
            seq = ((seq/c)+1)*c;
        }else {
            //find sequence for each level
            if(coreObjRelation.isPresent()){
                seq = coreObjRelation.get().getSequence();
                lvl = coreObjRelation.get().getLevel();
            }

            lvl++;
            c =(long)Math.pow(10, m_number_level-2*(lvl-1));
            seq = ((seq/c) + 1)*c;
        }

        //Register Role's methods
        Relation relation = new Relation();
        relation.setCompartmentId(compartmentId);
        relation.setCompartmentName(compartment.getClass().getName());
        relation.setObjectId(objId);
        relation.setObjectName(objName);
        relation.setPlayerId(rolePlayer.hashCode());
        relation.setPlayerName(rolePlayer.getClass().getName());
        relation.setRoleId(role.hashCode());
        relation.setRoleName(role.getClass().getName());
        relation.setLevel(lvl);
        relation.setType(PlayRelationEnum.ROLE_PLAYS_ROLE.getCode());
        relation.setSequence(seq);

        //added by lycog on 24.03.2016 to address inconsistency on black-box transaction
        relation.setBoundTime(LocalDateTime.now());

        Method[] methods = role.getClass().getDeclaredMethods();
        if(methods.length>0) {
            for (Method m : methods) {
                //log.debug("{}.{}", role.getName(), m.getName());

                relation.setMethodName(m.toString());

                m_relations.add(new Relation(relation));
            }
        }else{
            relation.setMethodName(""); //if null then, the match (RegEx) won't work on method invocation
            m_relations.add(new Relation(relation));
        }

    }

    /**
     * Register inheritance relation in the lookup table
     * @param compartment
     * @param baseRole is a role instance acting as a player
     * @param superRole is super class of role in the inheritance
     */
    public void registerInheritanceRelation(Object compartment, Object baseRole, Object superRole){
        ArrayDeque<Relation> m_relations = RegistryManager.getInstance().getRelations();
        int compartmentId = compartment.hashCode();

        Optional<Relation> coreRelation = m_relations.stream()
                .filter(r -> r.getCompartmentId() == compartmentId
                        && r.getRoleId() == baseRole.hashCode())
                .sorted(RelationSortHelper.SEQUENCE_DESC)
                .findFirst();

        int objId = coreRelation.get().getObjectId(); //a real core object
        String objName = coreRelation.get().getObjectName(); // a real core object name

        long seq = coreRelation.get().getSequence();
        int lvl = coreRelation.get().getLevel();

        //Register Role's methods
        Method[] methods = superRole.getClass().getDeclaredMethods();

        Relation relation = new Relation();
        relation.setCompartmentId(compartmentId);
        relation.setCompartmentName(compartment.getClass().getName());
        relation.setObjectId(objId);
        relation.setObjectName(objName);
        relation.setPlayerId(baseRole.hashCode());
        relation.setPlayerName(baseRole.getClass().getName());
        relation.setRoleId(superRole.hashCode());
        relation.setRoleName(superRole.getClass().getName());
        relation.setLevel(lvl);
        relation.setType(PlayRelationEnum.INHERITANCE.getCode());
        relation.setSequence(seq);

        //added by lycog on 24.03.2016 to address inconsistency on black-box transaction
        relation.setBoundTime(LocalDateTime.now());

        if(methods.length>0){
            for(Method m: methods){
                relation.setMethodName(m.toString());

                m_relations.add(new Relation(relation));
            }
        }else{
            relation.setMethodName(""); //prevent from error while querying method
            m_relations.add(relation);
        }
    }

    /**
     * Register prohibit relation
     * @param compartment
     * @param player is either core object or role
     * @param role is a role class to be prohibited in the playing relation
     */
    public void registerProhibitRelation(Object compartment, Object player, Class<?> role){
        ArrayDeque<Relation> m_relations = RegistryManager.getInstance().getRelations();
        int compartmentId = compartment.hashCode();

        Optional<Relation> coreObject = m_relations.stream()
                .filter(x -> x.getCompartmentId() == compartmentId
                        && x.getRoleId() == player.hashCode())
                .sorted(RelationSortHelper.SEQUENCE_DESC)
                .findFirst();

        int objId = coreObject.get().getObjectId();
        String objName = coreObject.get().getObjectName();
        long seq = coreObject.get().getSequence();
        int lvl = coreObject.get().getLevel();

        Relation relation = new Relation();
        relation.setCompartmentId(compartmentId);
        relation.setCompartmentName(compartment.getClass().getName());
        relation.setObjectId(objId);
        relation.setObjectName(objName);
        relation.setPlayerId(player.hashCode());
        relation.setPlayerName(player.getClass().getName());
        relation.setRoleId(-1); //no role instance
        relation.setRoleName(role.getName());
        relation.setLevel(lvl);
        relation.setType(PlayRelationEnum.PROHIBIT.getCode());
        relation.setSequence(seq);
        relation.setMethodName(""); //no required methods to be stored

        m_relations.add(relation);
    }

    /**
     * This is the method dispatch to find the proper role instance for invocation.
     * MethodName, ReturnType, argTypes and argValues are for constructing the method signature to be queried.
     * @param relation a flag to set whether searching performs for root (natural instance) or from role (player)
     * @param compartment
     * @param player based on the flag (fromRoot), it is either root object or role player
     * @param methodName
     * @param returnType
     * @param argTypes
     * @return
     */
    public Object getInvokingRole(PlayRelationEnum relation, Object compartment, Object player,
                                  String methodName, Class<?> returnType, Class[] argTypes){
        ArrayDeque<Relation> m_relations = RegistryManager.getInstance().getRelations();

        //added by lycog on 24.03.2016 to track the started time of method execution
        //LocalDateTime currentTime = LocalDateTime.now();
        HashMap<Long, AbstractMap.SimpleEntry<Integer, LocalDateTime>> logTheads = RegistryManager.getLogThreads();
        AbstractMap.SimpleEntry<Integer, LocalDateTime> keyValue = logTheads.get(Thread.currentThread().getId());
        final LocalDateTime currentTime = LocalDateTime.now();
        //DumpHelper.dumpRelation();
//        if(keyValue == null){
//            //System.out.println("push time");
//            logTheads.put(Thread.currentThread().getId(), new AbstractMap.SimpleEntry<>(player.hashCode(), currentTime));
//        }

        LocalDateTime ct = (keyValue==null)? currentTime:keyValue.getValue();
        //System.out.println("Log time : " + ct);

        Object retObj;
        String methodSignature = methodSignature(returnType, methodName, argTypes);
        Optional<Relation> rel = m_relations.stream()
                .filter(c -> c.getBoundTime().compareTo(ct)<=0
                        && (c.getPhantom()==null || c.getPhantom().isAfter(ct))
                        && c.getCompartmentId() == compartment.hashCode()
                        && (relation == PlayRelationEnum.OBJECT_PLAYS_ROLE ? c.getObjectId() == player.hashCode()
                            : c.getPlayerId()==player.hashCode())
                        && c.getMethodName().matches(methodSignature))
                .sorted(RelationSortHelper.SEQUENCE_DESC.thenComparing(RelationSortHelper.TYPE_DESC))
                .findFirst();

        if(relation == PlayRelationEnum.ROLE_PLAYS_ROLE){

            //Calculate the sequence of current role to cascade down
            final long c = (long)Math.pow(10, m_number_level - 2*(rel.get().getLevel()-1));
            final long  seq = (rel.get().getSequence()/c);

            //All role (including itself) relations down from the cascaded sequence
            rel = m_relations.stream()
                    .filter(x -> x.getBoundTime().compareTo(ct)<=0
                            && x.getCompartmentId() == compartment.hashCode()
                            && ((x.getSequence() / c) == seq || x.getRoleId() == player.hashCode())
                            && x.getMethodName().matches(methodSignature))
                    .sorted(RelationSortHelper.SEQUENCE_DESC.thenComparing(RelationSortHelper.TYPE_DESC))
                    .findFirst();


        }

        //if role was not found then return player
        if(rel.isPresent()){
            int roleId = rel.get().getRoleId();
            retObj = RegistryManager.getRolesMap().get(roleId);
        }else{
            retObj = player;
        }

        return retObj;
    }

    private String methodSignature(Class returnType, String methodName, Class[] clazzes){
        StringBuilder sb = new StringBuilder();
        sb.append(".*").append(returnType==null?"":" " + returnType.getName());
        sb.append(" .*.").append(methodName).append("\\(");

        if(clazzes!=null) {
            for (int i = 0; i < clazzes.length; i++) {
                sb.append(clazzes[i].getName());
                if (i < clazzes.length - 1) sb.append(",");
            }
        }
        sb.append("\\)");

        return sb.toString();
    }

    /**
     * Check if the passing obj is role in the relations
     * @param compartment
     * @param obj
     * @return
     */
    public boolean isRole(Object compartment, Object obj){
        ArrayDeque<Relation> m_relations = RegistryManager.getInstance().getRelations();

        Optional<Relation> rel = m_relations.stream()
                .filter(c -> c.getCompartmentId()==compartment.hashCode()
                        && c.getRoleId() == obj.hashCode())
                .findFirst();

        return rel.isPresent();
    }
}
