package net.runtime.role.atm.typesafe;

import net.runtime.role.actor.Compartment;
import net.runtime.role.actor.Player;

/**
 * Created by nguonly role 7/18/15.
 */
public class Main {
    public static void main(String[] args){

        Bank bb = new Bank("Bust-Bank");

        Account acc1 = Player.initialize(Account.class);
        acc1.setBank(bb);

        Bank cb = new Bank("Crash-Bank");
        Account acc2 = Player.initialize(Account.class);
        acc2.setBank(cb);

        try(ATM cbATM = Compartment.initialize(ATM.class)) {
            cbATM.participate(cb, acc2, acc1);
            System.out.println("Both accounts get 1000 Euros seed capital.");
            acc1.credit(1000);
            //acc1.invoke("credit", new Class[]{int.class}, new Object[]{1000});
            acc2.credit(1000);
            //acc2.invoke("credit", new Class[]{int.class}, new Object[]{1000});

            System.out.println("Withdrawing 200 Euro from both accounts:");
            //cbATM.payCash(acc1, 200);
            acc1.role(ForeignAccount.class).debit(200);
            System.out.println("Balance of foreign account: " + acc1.getBalance() + " Euro");

            //cbATM.payCash(acc2, 200);
            acc2.debit(200);
            System.out.println("Balance of home account: " + acc2.getBalance() + " Euro");

            System.out.println("ATMs fee account balance: " + cbATM.getFeeAccountBalance() + " Euro");

            System.out.println("---------------------------------------------------");
            try {
                System.out.println("Get balance of foreign account via atm: ");
                //System.out.println(cbATM.getBalance(acc1) + " Euro");
                System.out.println(acc1.role(ForeignAccount.class).getBalance() + " Euro");
            } catch (RuntimeException ade) {
                System.out.println("Sorry: Can not read the balance of a foreign account!");
            }
            try {
                System.out.println("Get balance of home account via atm: ");
                //System.out.println(cbATM.getBalance(acc2) + " Euro");
                System.out.println(acc2.getBalance() + " Euro");
            } catch (AccessDeniedException ade) {
                System.out.println("Sorry: Can not read the balance of a foreign account!");
            }
            System.out.println("---------------------------------------------------");


        }
        try(SpecialConditions sc = Compartment.initialize(SpecialConditions.class)) {
            sc.participate(acc2);

            System.out.println("Crediting 2000 Euro to both accounts:");
            //int acc1_before = acc1.invoke("getBalance", int.class);
            int acc1_before = acc1.getBalance();
            int acc2_before = acc2.getBalance();
            acc1.credit(2000); // -> balance += 2020

            System.out.println("Not participating account gets: " + (acc1.getBalance() - acc1_before) + " Euro.");
            //acc2.invoke("credit", new Class[]{int.class}, new Object[]{2000}); // -> balance += 2000
            acc2.role(BonusAccount.class).credit(2000);
            System.out.println("Special condition participating account gets: " + (acc2.getBalance() - acc2_before) + " Euro.");
        }

    }

    public void test(){
        Main m = new Main();
        m.main(null);
    }
}
