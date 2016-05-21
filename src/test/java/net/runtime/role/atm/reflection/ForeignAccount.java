package net.runtime.role.atm.reflection;


import net.runtime.role.actor.Role;

/**
 * Created by nguonly role 7/16/15.
 */
public class ForeignAccount extends Role {
    public boolean debit(int amount){
        return debitWithFee(amount);
    }

    boolean debitWithFee(int amount) {
        int fee = calculateFee(amount);
        boolean b = invokeBase("debit", boolean.class, new Class[]{int.class}, new Object[]{fee + amount});
        if(b){
            //System.out.println("Debiting from a foreign account: Additional fee of " + fee + " Euro will be debited!");
            //feeAccount.credit(fee);
            invokeCompartment("credit", new Class[]{int.class},new Object[]{fee});
            return true;
        }
//        if (base.debitWithFee(fee+amount)) {
//            System.out.println("Debiting from a foreign account: Additional fee of "+fee+" Euro will be debited!");
//            feeAccount.credit(fee);
//            return true;
//        }
        return false;

    }

    public int getBalance(){
        throw new RuntimeException("Access to balance of foreign account not allowed!");
    }

    public int calculateFee(int amount) {
        int feePercent = 5;
        return (amount/100)*feePercent;
    }


}
