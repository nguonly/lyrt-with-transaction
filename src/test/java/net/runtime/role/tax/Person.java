package net.runtime.role.tax;


import net.runtime.role.actor.Player;

/**
 * Created by nguonly on 9/9/15.
 */
public class Person extends Player {
    private String name;
    private double saving;

    public Person(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSaving() {
        return saving;
    }

    public void setSaving(double saving) {
        this.saving += saving;
    }
}
