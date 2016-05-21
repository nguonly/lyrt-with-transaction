package net.runtime.role.transfersystem.account;


import net.runtime.role.actor.Player;

/**
 * Created by nguonly on 8/19/15.
 */
public class Account extends Player {
    private int accountNumber;
    private float balance;
    private Bank bank;

    public Account(Bank bank, int accountNumber) {
        this.bank = bank;
        this.accountNumber = accountNumber;
    }

    public void credit(float amount) {
        balance = balance + amount;
    }

    public void debit(float amount) {
        if(balance < amount)
            System.err.println("Total balance not sufficient: "+ amount + "," + balance);
        else {
            balance = balance - amount;
        }
    }

    public Bank getBank(){
        return bank;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public float getBalance() {
        return balance;
    }
}
