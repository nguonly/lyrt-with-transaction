package net.runtime.role.tax;


import net.runtime.role.actor.Compartment;

/**
 * Created by nguonly on 9/9/15.
 */
public class TaxDepartment extends Compartment {
    private double revenue;
    private String name;

    public TaxDepartment(String name){
        this.name = name;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double amount) {
        this.revenue += amount;
    }
}
