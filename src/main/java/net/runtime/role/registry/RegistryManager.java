package net.runtime.role.registry;


import net.runtime.role.actor.Compartment;
import net.runtime.role.actor.Role;
import net.runtime.role.evolution.EvolutionXMLParser;
import net.runtime.role.exception.*;
import net.runtime.role.helper.ReflectionHelper;
import net.runtime.role.helper.RelationSortHelper;
import net.runtime.role.orm.ActorTypeEnum;
import net.runtime.role.orm.PlayRelationEnum;
import net.runtime.role.orm.Relation;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Created by nguonly role 7/10/15.
 */
public class RegistryManager implements IEvolution{
    private static RegistryManager m_registryManager;

    /* Instance Pool */
    private static HashMap<Integer, Object> m_objectsMap = new HashMap<>();
    private static HashMap<Integer, Object> m_rolesMap = new HashMap<>();
    private static HashMap<Integer, Object> m_compartmentsMap = new HashMap<>();

    private static ArrayDeque<Relation> m_relations = new ArrayDeque<>();

    private static ArrayDeque<Integer> m_activeCompartments = new ArrayDeque<>();

    private static int m_number_level = 4*4 + 1;

    private static LookupTable m_lookupTable = new LookupTable();

    //static final Logger log = LogManager.getLogger(RegistryManager.class);

    /**
     * added by lycog on 24.03.2016.
     * Trying to log the transaction per method call per thread if has not been done.
     * Long is threadId, SimpleEntry is a key value pair of current ObjectId and current time.
     */
    private static HashMap<Long, AbstractMap.SimpleEntry<Integer, LocalDateTime>> m_logThreads = new HashMap<>();

    /*
        can be flagged at runtime to support evolution. This triggers ClassReloading to reload role, compartment, object
     */
    private static boolean m_evolutionFlag = false;

    public static final ReentrantLock m_lock = new ReentrantLock();

    public static RegistryManager getInstance(){
        m_lock.lock();
        try {
            if (m_registryManager == null) {
                m_registryManager = new RegistryManager();
            }
        }finally {
            m_lock.unlock();
        }

        return m_registryManager;
    }

    public void setRelations(ArrayDeque<Relation> relations){
        m_relations = relations;
    }

    public ArrayDeque<Relation> getRelations(){
        return m_relations;
    }

    public static HashMap<Integer, Object> getCoreObjectMap(){
        return m_objectsMap;
    }

    public static HashMap<Integer, Object> getCompartmentsMap(){
        return m_compartmentsMap;
    }

    public static HashMap<Integer, Object> getRolesMap(){ return m_rolesMap;}

    public static ArrayDeque<Integer> getActiveCompartments(){ return m_activeCompartments;}

    public static HashMap<Long, AbstractMap.SimpleEntry<Integer, LocalDateTime>> getLogThreads(){ return m_logThreads;}

    public void setEvolutionFlag(boolean flag){
        m_evolutionFlag = flag;
    }

    public boolean getEvolutionFlag(){
        return m_evolutionFlag;
    }

    public static void registerTransaction(long threadId, int objId){

//        m_lock.lock();

        //synchronized (m_registryManager) {

            //HashMap<Long, AbstractMap.SimpleEntry<Integer, LocalDateTime>> logThreads = RegistryManager.getLogThreads();
            AbstractMap.SimpleEntry<Integer, LocalDateTime> keyValue = m_logThreads.get(threadId);
            final LocalDateTime currentTime = LocalDateTime.now();
            //DumpHelper.dumpRelation();
            if (keyValue == null) {
                //System.out.println(threadId + " push time ");
                m_logThreads.put(threadId, new AbstractMap.SimpleEntry<>(objId, currentTime));
                //DumpHelper.dumpLogThread();
            }
        //}
//        m_lock.unlock();
    }

    /************************************************
     * Relational Binding Operations
     ************************************************/

    public <T> T newPlayer(Class<T> player, Class[] argTypes, Object[] argValues) {
        return ActorInitialization.newObject(ActorTypeEnum.NATURAL_TYPE, player, argTypes, argValues);
    }

    public <T> T newCompartment(Class<T> compartment, Class[] argTypes, Object[] argValues){
        return ActorInitialization.newObject(ActorTypeEnum.COMPARTMENT_TYPE, compartment, argTypes, argValues);
    }

    public <T> T newRole(Class<T> role, Class[] argTypes, Object[] argValues){
        return ActorInitialization.newObject(ActorTypeEnum.ROLE_TYPE, role, argTypes, argValues);
    }

    public void activateCompartment(Object compartment){
        int compartmentId = compartment.hashCode();
        m_compartmentsMap.putIfAbsent(compartmentId, compartment);

        if(!m_activeCompartments.contains(compartmentId)) m_activeCompartments.push(compartmentId);
    }

    public void deactivateCompartment(Object compartment){
        int compartmentId = compartment.hashCode();
        m_relations.removeIf(r -> r.getCompartmentId() == compartmentId);

        //Pop out from active compartment stack
        if(!m_activeCompartments.isEmpty()){
            m_activeCompartments.pop();
        }else{
//            log.debug("Active Compartment is Empty");
            throw new RuntimeException("No active compartment was found");
        }
        m_compartmentsMap.remove(compartmentId);
    }

    public <T> T bind(Object compartment, Object player, Class<T> role,
                                   Class[] argTypes, Object[] argValues) throws RuntimeException{

        Object activeCompartment = getActiveCompartment(compartment);
        int activeCompartmentId = activeCompartment.hashCode();

        //Compartment cannot be a player inside their own compartment
        if(activeCompartmentId == player.hashCode()) throw new CompartmentAsPlayerInItsContextException();

        Optional<Relation> existingRole = m_relations.stream()
                .filter(x -> x.getCompartmentId() == activeCompartmentId
                        && x.getPlayerId() == player.hashCode()
                        && x.getRoleName().equals(role.getName())
                        && x.getType() != PlayRelationEnum.INHERITANCE.getCode())
                .sorted(RelationSortHelper.SEQUENCE_DESC)
                .findFirst();

        if(existingRole.isPresent()) throw new BindTheSameRoleTypeException();

        T roleInstance = newRole(role, argTypes, argValues);

        boolean isRole = m_lookupTable.isRole(activeCompartment, player);
        if(isRole){
            m_lookupTable.registerRolePlayingRoleRelation(activeCompartment, player, roleInstance);
        }else{
            //Put player into objectMap
            if(m_objectsMap.get(player.hashCode())==null){
                m_objectsMap.put(player.hashCode(), player);
            }
            m_lookupTable.registerObjectPlayingRoleRelation(activeCompartment, player, roleInstance);
        }

        return roleInstance;
    }

    public Object bind(Object compartment, Object player, String strRole,
                                   Class[] argTypes, Object[] argValues) throws RuntimeException {

        Class clsRole = null;
        try {
            clsRole = Class.forName(strRole).asSubclass(Object.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return bind(compartment, player, clsRole, argTypes, argValues);

    }

    public <T> T inherit(Compartment compartment, Object core, Class<T> superRole,
                                      Class[] argTypes, Object[] argValues) throws RuntimeException{

        Object activeCompartment = getActiveCompartment(compartment);
        int compartmentId = activeCompartment.hashCode();
        int coreId = core.hashCode();

        //Cannot inherit itself
        if(core.getClass().getName().equals(superRole.getName())) throw new InheritItselfException();

        //Allow only single inheritance
        //Check for existing role type available
        Optional<Relation> existingRole = m_relations.stream()
                .filter(r -> r.getCompartmentId() == compartmentId
                        && r.getPlayerId() == coreId
                        && r.getType() == PlayRelationEnum.INHERITANCE.getCode())
                .findFirst();

        if(existingRole.isPresent()) throw new SingleInheritanceException();

        try{
            T roleInstance = newRole(superRole, argTypes, argValues);

            m_lookupTable.registerInheritanceRelation(activeCompartment, core, roleInstance);

            return roleInstance;
        }catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void unbind(Object core,  Class role){
        //Find concrete role relation
        int compartmentId = m_activeCompartments.peek();

        Optional<Relation> concreteRoleRelation = m_relations.stream()
                .filter(c -> c.getCompartmentId() == compartmentId
                        //&& isPlayer?(c.playerId == rolePlayerId):(c.roleId == rolePlayerId)
                        && c.getPlayerId() == core.hashCode()
                        && c.getRoleName().equals(role.getName()))
                .sorted(RelationSortHelper.SEQUENCE_DESC.thenComparing(RelationSortHelper.TYPE_DESC))
                .findFirst();

        if(!concreteRoleRelation.isPresent()) return;

        //If unbind happens when there is a transaction running, just mark it as phantom.
        //This also applies to its all cascading relations.
        if(m_logThreads.size()>0){
            concreteRoleRelation.get().setPhantom(LocalDateTime.now());
        }

        //Find role list in play relation by concrete role relation
        final long c = (long)Math.pow(10, m_number_level - 2*(concreteRoleRelation.get().getLevel()-1));
        final long  seq = (concreteRoleRelation.get().getSequence()/c);

        Map<Integer, List<Relation>> uniqueRoleListToBeRemoved = m_relations.stream()
                .filter(x -> x.getCompartmentId() == compartmentId
                        && (x.getObjectId() == concreteRoleRelation.get().getObjectId())
                        && ((x.getSequence() / c) == seq))
                .sorted(RelationSortHelper.SEQUENCE_DESC.thenComparing(RelationSortHelper.TYPE_DESC))
                .collect(Collectors.groupingBy(Relation::getRoleId));

        //Actual removing process in m_relation
        for(Iterator<Relation> itr = m_relations.iterator(); itr.hasNext();){
            Relation r = itr.next();
            for (Integer roleId : uniqueRoleListToBeRemoved.keySet()) {
                if (r.getRoleId() == roleId) {
                    if(m_logThreads.size()>0){
                        r.setPhantom(LocalDateTime.now());
                    }else {
                        m_rolesMap.remove(roleId);
                        itr.remove();
                    }
                    //itrRoleToBeRemoved.remove(); //To prevent from overloading method not removing
                    break;
                }
            }
        }
    }

    /**
     * Unbind all the bound roles from a root player
     * @param root a root player
     */
    public void unbindAll(Object root){
        int rootId = root.hashCode();

        /**
         * Role Id = -1 is prohibit relation that require no initialization of role
         */
        m_relations.stream()
                .filter(c -> c.getObjectId() == rootId)
                .forEach(relation -> m_rolesMap.remove(relation.getRoleId()));

        m_relations.removeIf(relation -> relation.getObjectId() == rootId);
    }

    /**
     * Transfer role instance from one to another player. It's also possible to transfer to different compartment.
     * @param role
     * @param from
     * @param to
     * @param toCompartment
     */
    public void transfer(Class role, Object from, Compartment fromCompartment, Object to, Compartment toCompartment){
        //TODO: Should consider phantom role in the presence of transaction.
        int fromObjId = from.hashCode();
        int toObjId = to.hashCode();
        final int toCompartmentId = toCompartment==null? m_activeCompartments.peek(): toCompartment.hashCode();
        final int fromCompartmentId = fromCompartment==null? m_activeCompartments.peek(): fromCompartment.hashCode();

        //find transferring role to do cascading later
        Optional<Relation> transferringRoleRel = m_relations.stream()
                .filter(r -> r.getCompartmentId() == fromCompartmentId
                        && r.getPlayerId() == fromObjId
                        && r.getRoleName().equals(role.getName()))
                .findFirst();

        //Check if the [To] player has current bound roles. If so, get the latest
        Optional<Relation> latestRoleTo = m_relations.stream()
                .filter(r -> r.getCompartmentId() == toCompartmentId
                        && r.getPlayerId() == toObjId)
                .sorted(RelationSortHelper.SEQUENCE_DESC.thenComparing(RelationSortHelper.TYPE_DESC))
                .findFirst();

        //Get current relation of To player if available
        Optional<Relation> currentTo = m_relations.stream()
                .filter(r -> r.getCompartmentId() == toCompartmentId)
                .filter(r -> (to instanceof Role) ?
                        r.getRoleId() == toObjId :
                        r.getPlayerId() == toObjId)
                .findFirst();

        //Compute sequence to search the lower cascading
        final long c = (long)Math.pow(10, m_number_level - 2*(transferringRoleRel.get().getLevel()));
        final long  seq = (transferringRoleRel.get().getSequence()/c);

        //Get all the roles in the play-relation
        List<Relation> uniqueRoleList = m_relations.stream()
                .filter(r -> r.getCompartmentId() == fromCompartmentId
                        && r.getObjectId() == fromObjId
                        && (r.getSequence() / c) >= seq)
                .collect(Collectors.toList());
        //.collect(Collectors.groupingBy(r -> r.roleId));

        //Remove role and its children from previous bound player
        for(Iterator<Relation> itr = m_relations.iterator();itr.hasNext();){
            Relation r = itr.next();
            for (Relation removingRoleId : uniqueRoleList) {
                if (r.getRoleId() == removingRoleId.getRoleId()) {
                    itr.remove();
                    break;
                }
            }
        }

        //loop in uniqueRoleList and construct relation to be added
        long seq1;
        int lvl;
        long c1;
        if(latestRoleTo.isPresent()){
            lvl = latestRoleTo.get().getLevel();
            seq1 = latestRoleTo.get().getSequence();
            c1 = (long)Math.pow(10, m_number_level - 2*(lvl-1));
            seq1 = ((seq1 / c1) + 1) * c1;
        }else{
            //No previous bound role
            //find the current object whether it's a role or a core object.
            //If it's a role then find the level by lvl = role.level + 1
            if(to instanceof Role){
                //currentTo is not always empty because it's a role
                lvl = currentTo.get().getLevel() + 1;
                c1 = (long) Math.pow(10, m_number_level - 2 * (lvl - 1));
                seq1 = (currentTo.get().getSequence()/c1 + 1)*c1;
            }else {
                lvl = 1;
                seq1 = (long) Math.pow(10, m_number_level);
            }
        }

        //find the difference between old and new
        int levelOffset = transferringRoleRel.get().getLevel() - lvl;

        //Check if the target player has previous role type the same as one of uniqueRoleList

        for (Relation r : uniqueRoleList) {
            Relation relation = new Relation();
            relation.setCompartmentId(toCompartmentId);
            relation.setCompartmentName(toCompartment == null ?
                    m_compartmentsMap.get(toCompartmentId).getClass().getName() :
                    toCompartment.getClass().getName());
            relation.setObjectId((to instanceof Role)
                    ? currentTo.get().getObjectId() : toObjId);
            relation.setObjectName((to instanceof Role) ? currentTo.get().getObjectName() :
                    to.getClass().getName());

            if (r.getRoleId() == transferringRoleRel.get().getRoleId()) {
                //This the transferring root role
                relation.setPlayerId((to instanceof Role) ? currentTo.get().getObjectId()
                        : toObjId);
                relation.setPlayerName((to instanceof Role) ? currentTo.get().getObjectName()
                        : to.getClass().getName());
                relation.setLevel(lvl);
                relation.setType((to instanceof Role) ?
                        PlayRelationEnum.ROLE_PLAYS_ROLE.getCode() :
                        PlayRelationEnum.OBJECT_PLAYS_ROLE.getCode());
                relation.setSequence(seq1);
            } else {
                relation.setPlayerId(r.getPlayerId());
                relation.setPlayerName(r.getPlayerName());
                relation.setType(r.getType()); //remain the same

                int newLevel = r.getLevel() - levelOffset;
                long remainderOfSequence = r.getSequence() % (long) Math.pow(10, m_number_level - 2 * (transferringRoleRel.get().getLevel() - 1));
                long newSeq = seq1 + remainderOfSequence * (long) Math.pow(10, 2 * (levelOffset));
                relation.setLevel(newLevel);
                relation.setSequence(newSeq);
            }

            relation.setRoleId(r.getRoleId());
            relation.setRoleName(r.getRoleName()); //rolesMap.get(r.roleId).getClass().getName();

            relation.setMethodName(r.getMethodName());

            m_relations.add(new Relation(relation));
        }
    }

    /****************************************************************************
     * Method Invocation.
     * Various points from which method is called in the playing relation.
     * **************************************************************************/

    public <T> T invokeRole(Object compartment, Object core, String methodName, Class<T> returnType,
                                  Class[] argTypes, Object[] argValues) throws RuntimeException{

        Object activeCompartment = getActiveCompartment(compartment);

        boolean isRole = m_lookupTable.isRole(activeCompartment, core);
        PlayRelationEnum relation = isRole?PlayRelationEnum.ROLE_PLAYS_ROLE:PlayRelationEnum.OBJECT_PLAYS_ROLE;

        Object role = m_lookupTable.getInvokingRole(relation, activeCompartment,
                core, methodName, returnType, argTypes);

        T ret = ReflectionHelper.invoke(role, methodName, returnType, argTypes, argValues);

        return ret;
    }

    /**
     * Invoke based method. It can be either root player or role player
     * @param role Current role
     * @param methodName Method Name role Base
     * @param argTypes Type of parameter
     * @param argValues Value of parameter
     * @return Object
     */
    public <T> T invokeBase(Object role, String methodName, Class<T> returnType, Class[] argTypes, Object[] argValues) {
        Optional<Relation> rel = m_relations.stream()
                .filter(c -> c.getRoleId() == role.hashCode()).findFirst();

        if(rel.isPresent()){
            Object base;
            if(rel.get().getObjectId() == rel.get().getPlayerId()){
                //It's root player (Player)
                base = m_objectsMap.get(rel.get().getObjectId());
            }else{
                //It's role player (Role)
                base = m_rolesMap.get(rel.get().getPlayerId());
            }
            return ReflectionHelper.invoke(base, methodName, returnType, argTypes, argValues);
        }else{
//                log.error("{}.{} was not found", role.getClass().getName(), methodName);
            throw new RuntimeException("Method was not found");
        }
    }

    /**
     * The method is to invoke root (core object) methods
     * @param role a role
     * @param methodName case-sentitive method name
     * @param returnType
     * @param argTypes
     * @param argValues
     * @param <T>
     * @return
     */
    public <T> T invokeCore(Role role, String methodName, Class<T> returnType, Class[] argTypes, Object[] argValues){
        int roleId = role.hashCode();
        Optional<Relation> rootRel = m_relations.stream()
                .filter(r -> r.getRoleId() == roleId)
                .findFirst();
        if(rootRel.isPresent()){
            Object objRoot = m_objectsMap.get(rootRel.get().getObjectId());
            return ReflectionHelper.invoke(objRoot, methodName, returnType, argTypes, argValues);
        }else{
            throw new RuntimeException("Root or Core Object was not found");
        }
    }

    public <T> T invokeCompartment(boolean isPlayer, Object core, String methodName,
                                   Class<T> returnType, Class[] argTypes, Object[] argValues){
        int coreId = core.hashCode();
        Optional<Relation> compartmentRel = m_relations.stream()
                .filter(r -> isPlayer ? r.getPlayerId() == coreId : r.getRoleId() == coreId)
                .findFirst();

        if(compartmentRel.isPresent()){
            Object objCompartment = m_compartmentsMap.get(compartmentRel.get().getCompartmentId());
            return ReflectionHelper.invoke(objCompartment, methodName, returnType, argTypes, argValues);

        }else{
            throw new CompartmentNotFoundException();
        }
    }

    /***************************************
     *Role Constraints
     ***************************************/

    /**
     * This prohibit constraint is applied for Role.
     * It seems not need at this moment.
     * @param core a Player either (root object or role)
     * @param role a prohibited role
     */
    public void prohibit(Compartment compartment, Object core, Class role){
        Object activeCompartment = getActiveCompartment(compartment);

        m_lookupTable.registerProhibitRelation(activeCompartment, core, role);

    }

    /******************************************************************************
     * Type safe of role, player and compartment searching for static invocation.
     * It can be used whenever type of actors is known in advance.
     ******************************************************************************/

    public <T> T role(Object compartment, Object player, Class<T> roleClass){
        Object activeCompartment = getActiveCompartment(compartment);
        int activeCompartmentId = activeCompartment.hashCode();

        int playerId = player.hashCode();
        String roleName = roleClass.getName();

        boolean isRole = m_lookupTable.isRole(activeCompartment, player);
        Optional<Relation> roleRel = m_relations.stream()
                .filter(r -> r.getCompartmentId() == activeCompartmentId)
                .filter(r -> (isRole) ? r.getPlayerId() == playerId : r.getObjectId() == playerId)
                .filter(r -> r.getRoleName().equals(roleName))
                .findFirst();

        if (roleRel.isPresent()) {
            Object obj = m_rolesMap.get(roleRel.get().getRoleId());
            return roleClass.cast(obj);
        }

        return null;
    }

    public <T> T base(Compartment compartment, Role role, Class<T> baseClass){
        int compartmentId = getActiveCompartment(compartment).hashCode();
        //Check if base is root player (Player) or role player (Role)
        Optional<Relation> baseRelation = m_relations.stream()
                .filter(r -> r.getCompartmentId() == compartmentId
                        && r.getRoleId() == role.hashCode())
                .findFirst();

        if (baseRelation.isPresent()) {
            Object base;
            if (baseRelation.get().getObjectId() == baseRelation.get().getPlayerId()) {
                //It's root player (Player)
                base = m_objectsMap.get(baseRelation.get().getObjectId());
            } else {
                //It's role player (Role)
                base = m_rolesMap.get(baseRelation.get().getPlayerId());
            }
            return baseClass.cast(base);
        } else {
//            log.error("{} was not found", role.getClass().getName());
        }

        return null;
    }

    /**
     * Delete phantom roles marked when there is an ongoing transaction running.
     * @param tx
     */
    public static void delPhantomRoles(Transaction tx){
        LocalDateTime txTime = null;

        //get the started time of current Tx
        for(AbstractMap.SimpleEntry<Integer, LocalDateTime> entry : m_logThreads.values()){
            if(entry.getKey() == tx.hashCode()){
                txTime = entry.getValue();
            }
        }
        LocalDateTime finalTxTime = txTime;

        List<LocalDateTime> txTimeList = m_logThreads.values().stream().map(r -> r.getValue()).collect(Collectors.toList());
        Collections.sort(txTimeList);

        int idx = 0;
        for(int i=0; i<txTimeList.size(); i++){
            if(txTimeList.get(i).equals(txTime)){
                idx = i;
                break;
            }
        }

        if(idx>0) return;

        if(idx==txTimeList.size()-1) {
            m_relations.removeIf(r -> {
                assert finalTxTime != null;
                return r.getPhantom() != null && r.getPhantom().isAfter(finalTxTime);
            });
        }else{
            //get the started time of the next Tx
            LocalDateTime txNextTime = txTimeList.get(idx+1);
            m_relations.removeIf(r -> r.getPhantom()!=null && r.getPhantom().isBefore(txNextTime));
        }
    }

    public <T> T compartment(Compartment compartment, Class<T> compartmentClass){
        try{
            Object objCompartment = getActiveCompartment(compartment);
            return compartmentClass.cast(objCompartment);
        }catch(CompartmentNotFoundException e){
            e.printStackTrace();
        }

        return null;
    }

    /************************************************
     * Instance Querying
     ************************************************/

    public Object getRootPlayer(Compartment compartment, Object role){
        int compartmentId = getActiveCompartment(compartment).hashCode();
        //Check if base is root player (Player) or role player (Role)
        Optional<Relation> baseRelation = m_relations.stream()
                .filter(r -> r.getCompartmentId() == compartmentId
                        && r.getRoleId() == role.hashCode())
                .findFirst();
        if(baseRelation.isPresent()){
            int objId = baseRelation.get().getObjectId();
            return m_objectsMap.get(objId);
        }

        return null;
    }

    public Object[] getRootPlayers(Compartment compartment, Class roleClass){
        int compartmentId = getActiveCompartment(compartment).hashCode();

        ArrayDeque<Object> lstObjects = new ArrayDeque<>();

        m_relations.stream()
                .filter(r -> r.getCompartmentId() == compartmentId
                        && r.getRoleName().equals(roleClass.getName()))
                .forEach(c->{
                    Object p = m_objectsMap.get(c.getObjectId());
                    if(!lstObjects.contains(p)) lstObjects.add(p);
                });

        return lstObjects.toArray();
    }

    public Object getPlayer(Compartment compartment, Object role){
        int compartmentId = getActiveCompartment(compartment).hashCode();
        //Check if base is root player (Player) or role player (Role)
        Optional<Relation> baseRelation = m_relations.stream()
                .filter(r -> r.getCompartmentId() == compartmentId
                        && r.getRoleId() == role.hashCode())
                .findFirst();

        if (baseRelation.isPresent()) {
            Object base;
            if (baseRelation.get().getObjectId() == baseRelation.get().getPlayerId()) {
                //It's root player (Player)
                base = m_objectsMap.get(baseRelation.get().getObjectId());
            } else {
                //It's role player (Role)
                base = m_rolesMap.get(baseRelation.get().getPlayerId());
            }
            return base;
        }

        return null;
    }

    public Object getCompartment(Object compartment, Object obj){
        Object activeCompartment = getActiveCompartment(compartment);
        int activeCompartmentId = activeCompartment.hashCode();
        int objId = obj.hashCode();

        boolean isRole = m_lookupTable.isRole(activeCompartment, obj);

        Optional<Relation> compartmentRel = m_relations.stream()
                .filter(r -> r.getCompartmentId() == activeCompartmentId)
                .filter(r -> (isRole)? r.getRoleId() == objId : r.getPlayerId() == objId)
                .findFirst();

        if(compartmentRel.isPresent()){
            return m_compartmentsMap.get(compartmentRel.get().getCompartmentId());
        }

        return null;
    }

    /***********************************************************************************
     * Evolution
     * Our evolution is represented in a XML-like configuration
     * that requires primitive data types such as int, string.
     * So the operations should be extended to accept those primitive types as arguments.
     ***********************************************************************************/

    /**
     * Parsing evolution xml configuration to evolution operations
     * @param xmlPath
     */
    public void evolve(String xmlPath){
        setEvolutionFlag(true);
        EvolutionXMLParser evolution = new EvolutionXMLParser();
        evolution.evolve(xmlPath);
    }

    public Object newPlayer(String player, Class[] argTypes, Object[] argValues) {
        return ActorInitialization.newObject(ActorTypeEnum.NATURAL_TYPE, player, argTypes, argValues);
    }

    public Object newCompartment(String compartment, Class[] argTypes, Object[] argValues){
        return ActorInitialization.newObject(ActorTypeEnum.COMPARTMENT_TYPE, compartment, argTypes, argValues);
    }

    public Object newRole(String role, Class[] argTypes, Object[] argValues){
        return ActorInitialization.newObject(ActorTypeEnum.ROLE_TYPE, role, argTypes, argValues);
    }

    public Object bind(int compartment, int playerId, String role,
                       Class[] argTypes, Object[] argValues) throws RuntimeException{

        Object activeCompartment= getActiveCompartment(compartment);
        int activeCompartmentId = activeCompartment.hashCode();

        //Compartment cannot be a player inside their own compartment
        if(activeCompartmentId == playerId) throw new CompartmentAsPlayerInItsContextException();

        Object player = m_objectsMap.get(playerId);

        return bind(activeCompartment, player, role, argTypes, argValues);
    }

    public void rebind(int compartmentId, int coreId, String roleClass, Class[] argTypes, Object[] argValues){
        Object core = m_objectsMap.get(coreId);
        try {
            unbind(core, Class.forName(roleClass));
            bind(compartmentId, coreId, roleClass, argTypes, argValues);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void unbind(int coreId, String roleClass){
        Object player = getCoreObjectMap().get(coreId);
        try {
            Class role = Class.forName(roleClass);
            unbind(player, role);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /****************************************
     * End of Evolution Operations
     ****************************************/

    private Object getActiveCompartment(Object compartment){
        Object activeCompartment = compartment;
        if(compartment==null){
            //check in the compartment stacks
            if(m_activeCompartments.isEmpty()) throw new CompartmentNotFoundException();
            activeCompartment = m_compartmentsMap.get(m_activeCompartments.peek());
        }
        return activeCompartment;
    }
}
