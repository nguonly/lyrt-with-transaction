package net.runtime.role.atm.typesafe;


import net.runtime.role.actor.Player;

/**
 * Created by nguonly role 7/16/15.
 */
public class Account extends Player {
    private int balance;
    private Bank bank;

    /**
     * Constructor of an account object. Gets the owning bank as parameter.
     */
    public Account(){

    }
    public Account(Bank _bank) {
        bank = _bank;
    }

    /**
     * Get the balance of the account.
     */
    public int getBalance() {
        return balance;
    }

    /**
     * Get the bank of the account.
     */
    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank){
        this.bank = bank;
    }

    /**
     * Debit an amount from the account.
     */
    public boolean debit(int amount) {

        if (!(amount>balance)) {
            balance -= amount;
            return true;
        }
        return false;
    }

    /**
     * Credit an amount to the account.
     */
    public void credit(int amount) {
        balance += amount;
    }
}
