package net.runtime.role.orm;

import java.time.LocalDateTime;

/**
 * Created by nguonly role 7/10/15.
 */
public class Relation {

    private int compartmentId;
    private int objectId;
    private int playerId;
    private int roleId;
    private int level;
    private int type;
    private long sequence;
    private String compartmentName;
    private String objectName;
    private String playerName;
    private String roleName;
    private String methodName;
    private LocalDateTime boundTime;
    private LocalDateTime phantom; //for marking the removing role in a transaction

    public Relation(){

    }

    //Copy constructor
    public Relation(Relation relation){
        this.compartmentId = relation.compartmentId;
        this.objectId = relation.objectId;
        this.playerId = relation.playerId;
        this.roleId = relation.roleId;
        this.level = relation.level;
        this.type = relation.type;
        this.sequence = relation.sequence;
        this.compartmentName = relation.compartmentName;
        this.objectName = relation.objectName;
        this.playerName = relation.playerName;
        this.roleName = relation.roleName;
        this.methodName = relation.methodName;
        this.boundTime = relation.boundTime;
        this.phantom = relation.phantom;
    }

    public String toString(){
        String ret = String.format("%25s %10d %10d %10d %10d %10d %d %d %d %s %s %s",
                boundTime, phantom, compartmentId, objectId,
                playerId, roleId, sequence, type, level,
                compartmentName, roleName, methodName);

        return ret;
    }

    public int getCompartmentId() {
        return compartmentId;
    }

    public void setCompartmentId(int compartmentId) {
        this.compartmentId = compartmentId;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public String getCompartmentName() {
        return compartmentName;
    }

    public void setCompartmentName(String compartmentName) {
        this.compartmentName = compartmentName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public LocalDateTime getBoundTime() {
        return boundTime;
    }

    public void setBoundTime(LocalDateTime boundTime) {
        this.boundTime = boundTime;
    }

    public LocalDateTime getPhantom() { return phantom; }

    public void setPhantom(LocalDateTime phantom) { this.phantom = phantom; }
}
