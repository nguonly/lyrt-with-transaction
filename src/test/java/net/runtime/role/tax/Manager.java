package net.runtime.role.tax;


import net.runtime.role.actor.Player;
import net.runtime.role.actor.Role;

/**
 * Created by nguonly on 9/9/15.
 */
public class Manager extends Role {
    private double salary = 2000;

    public double getSalary(){
        return salary;
    }

    public void assignTask(){
        Player manager = (Player)getRootPlayer();
        String managerName = manager.invoke("getName", String.class);

        System.out.println(managerName + " assigns work to Employee");

        Object[] employees = getRootPlayer(Employee.class);
        for(Object obj: employees){
            ((Player)obj).invoke("work");
        }
    }
}
