package net.runtime.role.tax;


import net.runtime.role.actor.Player;
import net.runtime.role.actor.Role;

/**
 * Created by nguonly on 9/10/15.
 */
public class Accountant extends Role {
    private double salary = 1600;

    public double getSalary(){
        return salary;
    }

    /**
     * Pay salary to all employee working in this company
     */
    public void paySalary(){
        Object[] employees = getRootPlayer(Employee.class);
        for(Object obj: employees){
            pay(obj);
        }

        employees = getRootPlayer(Manager.class);
        for(Object obj: employees){
            pay(obj);
        }

        employees = getRootPlayer(Accountant.class);
        for(Object obj: employees){
            pay(obj);
        }
    }

    private void pay(Object obj){
        double salary = ((Player)obj).invoke("getSalary", double.class);
        invokeCompartment("paySalary", void.class, new Class[]{double.class}, new Object[]{salary});
        ((Player)obj).invoke("setSaving", void.class, new Class[]{double.class}, new Object[]{salary});
    }
}
