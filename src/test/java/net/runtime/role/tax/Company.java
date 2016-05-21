package net.runtime.role.tax;


import net.runtime.role.actor.Compartment;

/**
 * Created by nguonly on 9/9/15.
 */
public class Company extends Compartment {
    private double balance;
    private String name;

    public Company(String name){
        this.name = name;
    }

    public void setRevenue(double amount){
        balance += amount;
    }

    public double getRevenue(){
        return balance;
    }

    public double taxToBePaid(){
        double revenue = getRevenue();
        double tax = revenue*20/100;
        balance -= tax;

        return tax;
    }

    /**
     * Pay salary to individual contract (position)
     * @param amount
     */
    public void paySalary(double amount){
        balance -= amount;
    }
}
