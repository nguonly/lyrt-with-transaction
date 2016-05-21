package net.runtime.role.helper;

import net.runtime.role.orm.PlayRelationEnum;
import net.runtime.role.orm.Relation;
import net.runtime.role.registry.RegistryManager;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by nguonly on 8/3/15.
 */
public class StatisticsHelper {
    public static Deque<Relation> getRelations(){
        return RegistryManager.getInstance().getRelations();
    }

    public static int rolesCount(int playerId){
        int sum = 0;
        Deque<Relation> relations = getRelations();

        //find root
        Map<Integer, List<Relation>> roots = relations.stream()
                .filter(r -> r.getPlayerId() == playerId
                        && (r.getType() == PlayRelationEnum.OBJECT_PLAYS_ROLE.getCode()
                        || r.getType() == PlayRelationEnum.ROLE_PLAYS_ROLE.getCode()))
                .collect(Collectors.groupingBy(Relation::getRoleId));

        sum += roots.size();
        for(Iterator<Integer> roleItr = roots.keySet().iterator();roleItr.hasNext();){
            Integer roleId = roleItr.next();
            sum += rolesCount(roleId);
        }

        return sum;
    }
}
