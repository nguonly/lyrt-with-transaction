package net.runtime.role.registry;

import net.runtime.role.bytecode.BuddyProxy;
import net.runtime.role.evolution.ClassReloader;
import net.runtime.role.orm.ActorTypeEnum;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by nguonly on 1/25/16.
 */
public class ActorInitialization {
    public static Object newObject(ActorTypeEnum actorType, String ct, Class<?>[] types, Object[] params) {
        try {
            //Class<?> cls = getClassLoader(forEvolution, ct);
            Class<?> cls = Class.forName(ct);
            return newObject(actorType, cls, types, params);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T> T newObject(ActorTypeEnum actorType, Class<T> ct, Class<?>[] types, Object[] params) {
        //boolean forEvolution = RegistryManager.getInstance().getEvolutionFlag();
        T retObj = null;

        retObj = initialize(ct, types, params);

        if (actorType == ActorTypeEnum.NATURAL_TYPE) {
            //retObj = BuddyProxy.create(ct, types, params);
            RegistryManager.getCoreObjectMap().put(retObj.hashCode(), retObj);
        } else {

            if (actorType == ActorTypeEnum.COMPARTMENT_TYPE) {
                RegistryManager.getCompartmentsMap().put(retObj.hashCode(), retObj);

                //push current active compartment
                RegistryManager.getActiveCompartments().push(retObj.hashCode());
//            }else if(actorType == ActorTypeEnum.NATURAL_TYPE){
//                RegistryManager.getCoreObjectMap().put(retObj.hashCode(), retObj);
            } else if (actorType == ActorTypeEnum.ROLE_TYPE) {
                RegistryManager.getRolesMap().put(retObj.hashCode(), retObj);
            }
        }
        return retObj;
//        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//            e.printStackTrace();
//        }

//        return null;
    }

    private static <T> T initialize(Class<T> ct, Class<?>[] types, Object[] params) {
        boolean forEvolution = RegistryManager.getInstance().getEvolutionFlag();
        T retObj = null;
        try {
            if (types == null || params == null) {
                if (forEvolution) { //load with class reloader
                    Class<?> cls = new ClassReloader().loadClass(ct);
                    retObj = (T) cls.newInstance();
                } else { // load with normal class loader
                    retObj = ct.newInstance();
                }
            } else {
                if (forEvolution) {
                    Class<?> cls = new ClassReloader().loadClass(ct);
                    Constructor<?> constructor = cls.getConstructor(types);
                    retObj = (T) constructor.newInstance(params);
                } else {
                    Constructor<T> constructor = ct.getConstructor(types);
                    retObj = constructor.newInstance(params);
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return retObj;
    }

}
