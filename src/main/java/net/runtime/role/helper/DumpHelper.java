package net.runtime.role.helper;


import net.runtime.role.actor.Compartment;
import net.runtime.role.orm.Relation;
import net.runtime.role.registry.RegistryManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by nguonly on 7/28/15.
 */
public class DumpHelper {

    public static void dumpRelation(Deque<Relation> relations){
        System.out.println("-----------------------------");
        for(Relation r : relations){
            System.out.format("%10d %10d %10d %10d %d %d %d %s %s %s\n", r.getCompartmentId(), r.getObjectId(),
                    r.getPlayerId(), r.getRoleId(), r.getSequence(), r.getType(), r.getLevel(),
                    r.getCompartmentName(), r.getRoleName(), r.getMethodName());
        }

    }

    public static void dumpRelation(){
        Deque<Relation> relations = RegistryManager.getInstance().getRelations();
        System.out.println("-----------------------------");
        System.out.format("%25s %25s %10s %10s %10s %10s %18s %s %s %15s %20s %20s\n",
                    "BoundTime", "Phantom", "Compment", "Root", "Player", "Role", "Sequence",
                    "Type", "Level", "CompName", "RoleName", "MethName");
        for(Relation r : relations){
            String compartmentName = getSimpleName(r.getCompartmentName());
            String roleName = getSimpleName(r.getRoleName());
            String methodName = getSimpleMethodName(r.getMethodName());
            System.out.format("%25s %25s %10d %10d %10d %10d %18d %4d %5d %15s %20s %20s\n",
                    r.getBoundTime(), r.getPhantom(),
                    r.getCompartmentId(), r.getObjectId(),
                    r.getPlayerId(), r.getRoleId(), r.getSequence(), r.getType(), r.getLevel(),
                    compartmentName, roleName, methodName);
        }

    }

    /**
     * Return the relation in string for UI
     * @return
     */
    public static String getFormattedRelation(){
        StringBuffer sb = new StringBuffer();

        Deque<Relation> relations = RegistryManager.getInstance().getRelations();
//        System.out.println("-----------------------------");
//        System.out.format("%10s %10s %10s %10s %18s %s %s %15s %20s %20s\n",
//                "Compment", "Root", "Player", "Role", "Sequence",
//                "Type", "Level", "CompName", "RoleName", "MethName");
        sb.append("-----------------------------\n");
        sb.append(String.format("%10s %10s %10s %10s %18s %s %s %15s %20s %20s\n",
                "Compment", "Root", "Player", "Role", "Sequence",
                "Type", "Level", "CompName", "RoleName", "MethName"));
        for(Relation r : relations){
            String compartmentName = getSimpleName(r.getCompartmentName());
            String roleName = getSimpleName(r.getRoleName());
            String methodName = getSimpleMethodName(r.getMethodName());
//            System.out.format("%10d %10d %10d %10d %18d %4d %5d %15s %20s %20s\n",
//                    r.getCompartmentId(), r.getObjectId(),
//                    r.getPlayerId(), r.getRoleId(), r.getSequence(), r.getType(), r.getLevel(),
//                    compartmentName, roleName, methodName);
            sb.append(String.format("%10d %10d %10d %10d %18d %4d %5d %15s %20s %20s\n",
                    r.getCompartmentId(), r.getObjectId(),
                    r.getPlayerId(), r.getRoleId(), r.getSequence(), r.getType(), r.getLevel(),
                    compartmentName, roleName, methodName));
        }

        return sb.toString();
    }

    /**
     * Get name without package name "."
     * @param name
     * @return
     */
    private static String getSimpleName(String name){
        int lastIndex;

        if(name==null) return "";

        int lastIndexOfDollar = name.lastIndexOf('$'); // for internal class
        if(lastIndexOfDollar>0) lastIndex = lastIndexOfDollar;
        else lastIndex = name.lastIndexOf('.');

        return name.substring(lastIndex+1);
    }

    private static String getSimpleMethodName(String name){
        if(name==null) return "";

        String[] methodParts = name.split("\\(");
        String firstPart = methodParts[0].substring(methodParts[0].lastIndexOf('.')+1);
        //System.out.println(methodParts.length);
        //System.out.println(methodParts[1]);
        if(methodParts.length<=1) return firstPart;
        int last = methodParts[1].lastIndexOf('.');
        String lastPart;
        if(last>0)
            lastPart = methodParts[1].substring(last+1);
        else
            lastPart = methodParts[1];

        return firstPart + "(" +lastPart;
    }


    public static void printTree(Deque<Relation> relations, Compartment compartment){
        System.out.println("***** Tree Display ******");
        int compartmentId = compartment.hashCode();
        //find root
        Map<Integer, List<Relation>> roots = relations.stream()
                .filter(r -> r.getCompartmentId() == compartmentId
                        && r.getObjectId() == r.getPlayerId())
                .collect(Collectors.groupingBy(r -> r.getObjectId()));

        roots.forEach((objectId, list) ->{
            Optional<Relation> objectName = relations.stream()
                    .filter(r -> r.getCompartmentId() == compartmentId
                            && r.getObjectId() == objectId
                            && r.getPlayerId() == objectId)
                    .findFirst();

            String objName = objectName.get().getObjectName();
            int idx = objName.lastIndexOf('$'); //in case of inner class
            if(idx<0) idx = objName.lastIndexOf('.');
            String oName = objName.substring(idx+1);
            System.out.format("%s:%d\n", oName, objectId);
            print(relations, compartmentId, objectId, objectId, 1);
        });
        System.out.println("***** End of Tree Display ******");
    }

    private static void print(Deque<Relation> relations, int compartmentId, int objectId, int playerId, int level){

        Map<Integer, List<Relation>> rel = relations.stream()
                .filter(r -> r.getObjectId() == objectId
                        && r.getPlayerId() == playerId
                        && r.getLevel() == level)
                .collect(Collectors.groupingBy(r -> r.getRoleId()));

        if(rel.size()>0){
            rel.forEach((roleId, list) -> {
                Optional<Relation> objectNameRel = relations.stream()
                        .filter(r -> r.getCompartmentId() == compartmentId
                                && r.getObjectId() == objectId
                                && r.getPlayerId() == playerId
                                && r.getRoleId() == roleId
                                && r.getLevel() == level)
                        .findFirst();

                String objName = objectNameRel.get().getRoleName();
                int idx = objName.lastIndexOf('$'); //in case of inner class
                if(idx<0) idx = objName.lastIndexOf('.');
                String oName = objName.substring(idx+1);

                System.out.format("%s%s:%d\n", getSpaces(level), oName, roleId);
                print(relations, compartmentId, objectId, roleId, level + 1);
            });

        }
    }

    private static String getSpaces(int depth){
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<depth;i++){
            sb.append("  ");
        }
        return sb.toString();
    }

    public static void dumpCoreObjects(){
        System.out.println("::: Core Object :::");
        HashMap<Integer, Object> coreMap = RegistryManager.getInstance().getCoreObjectMap();
        System.out.format("%10s %s\n", "Key", "Class");
        coreMap.forEach((k, v) -> System.out.format("%10s %s\n", k, v.getClass().getName()));
    }

    public static void dumpCompartments(){
        System.out.println("::: Compartment :::");
        HashMap<Integer, Object> compartmentMap = RegistryManager.getInstance().getCompartmentsMap();
        System.out.format("%10s %s\n", "Key", "Class");
        compartmentMap.forEach((k, v) -> System.out.format("%10s %s\n", k, v.getClass().getName()));
    }

    public static void dumpRoles(){
        System.out.println("::: Role :::");
        HashMap<Integer, Object> roleMap = RegistryManager.getInstance().getRolesMap();
        System.out.format("%10s %s\n", "Key", "Class");
        roleMap.forEach((k, v) -> System.out.format("%10s %s\n", k, v.getClass().getName()));
    }

    public synchronized static void dumpLogThread(){
        HashMap<Long, AbstractMap.SimpleEntry<Integer, LocalDateTime>> transactions = RegistryManager.getLogThreads();
        System.out.format("%s %10s %s\n", "Thread", "Key", "Class");
        transactions.forEach((k, v) ->{
                    String msg = String.format("[%d] <%d, %s>", k, v.getKey(), v.getValue());
                    System.out.println(msg);
                }
        );
        System.out.println("----------------");
    }
}
