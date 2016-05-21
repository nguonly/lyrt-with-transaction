package net.runtime.role.atm.reflection;


import net.runtime.role.actor.Role;

/**
 * Created by nguonly role 7/18/15.
 */
public class BonusAccount extends Role {
    void creditBonus(int amount){
        int bonus = amount/100;
        invokeBase("credit", new Class[]{int.class}, new Object[]{amount + bonus});
        System.out.println("You will gain a bonous of " + bonus + " Euro!");
    }

    public void credit(int amount){
        if(amount>1000) creditBonus(amount);
        else invokeBase("credit", new Class[]{int.class}, new Object[]{amount});
    }
}
