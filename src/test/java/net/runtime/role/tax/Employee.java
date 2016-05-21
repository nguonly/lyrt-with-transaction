package net.runtime.role.tax;


import net.runtime.role.actor.Player;
import net.runtime.role.actor.Role;

/**
 * Created by nguonly on 9/9/15.
 */
public class Employee extends Role {
    private double salary=1500;

    public double getSalary(){
        return salary;
    }

    public void work(){
        Player person = (Player)getRootPlayer();
        String name = person.invoke("getName", String.class);

        System.out.println(name + " works");
    }
}
