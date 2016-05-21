package net.runtime.role.orm;

/**
 * Created by nguonly on 7/31/15.
 */
public enum PlayRelationEnum {
    INHERITANCE(1),
    OBJECT_PLAYS_ROLE(2),
    ROLE_PLAYS_ROLE(3),
    PROHIBIT(4);

    private final int relationType;

    PlayRelationEnum(int relationType){
        this.relationType = relationType;
    }

    public int getCode(){
        return this.relationType;
    }

}
