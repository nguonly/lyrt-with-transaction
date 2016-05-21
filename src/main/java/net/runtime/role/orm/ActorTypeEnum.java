package net.runtime.role.orm;

/**
 * Created by nguonly on 1/25/16.
 */
public enum ActorTypeEnum {
    NATURAL_TYPE(1),
    ROLE_TYPE(2),
    COMPARTMENT_TYPE(3);

    private final int actorType;

    ActorTypeEnum(int actorType){
        this.actorType = actorType;
    }

    public int getCode(){
        return this.actorType;
    }
}
