package net.runtime.role.atm.typesafe;


import net.runtime.role.actor.Compartment;

/**
 * Created by nguonly role 7/18/15.
 */
public class SpecialConditions extends Compartment {
    Account account;
    public void participate(Account account){
        this.account = account;
        account.bind(BonusAccount.class);
    }



}
