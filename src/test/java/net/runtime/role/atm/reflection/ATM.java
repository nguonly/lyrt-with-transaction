package net.runtime.role.atm.reflection;


import net.runtime.role.actor.Compartment;

/**
 * Created by nguonly role 7/16/15.
 * Compartment
 */
public class ATM extends Compartment {
    private Bank myBank;
    private Account feeAccount;
    private Account homeAccount;
    private Account foreignAccount;

//    public ATM(Bank bank, Account homeAccount, Account foreignAccount){
//        myBank = bank;
//        feeAccount = new Account(bank);
//        this.homeAccount = homeAccount;
//        this.foreignAccount = foreignAccount;
//        this.foreignAccount.bind(ForeignAccount.class);
//        //foreignAccount = new Account();
//        //foreignAccount.bind(this, ForeignAccount.class);
//    }

    public void participate(Bank bank, Account homeAccount, Account foreignAccount){
        myBank = bank;
        feeAccount = new Account(bank);
        this.homeAccount = homeAccount;
        this.foreignAccount = foreignAccount;
        this.foreignAccount.bind(this, ForeignAccount.class);
        //foreignAccount = new Account();
        //foreignAccount.bind(this, ForeignAccount.class);
    }

    public void credit(int amount) {
        feeAccount.credit(amount);
    }

    int getFeeAccountBalance() {
        return feeAccount.getBalance();
    }

    /**
     * Pays the given amount of cash from the given account, if it contains enough money.
     */
    public int payCash(Account account, int amount) {
        //boolean ok = account.debit(amount);
        boolean ok = account.invoke("debit", boolean.class, new Class[]{int.class}, new Object[]{amount});
        if (ok)
            return amount;
        else
            return 0;
    }

    public int getBalance(Account account) throws AccessDeniedException{
        try{
            return account.invoke("getBalance", int.class);
        }catch(Exception e){
            throw new AccessDeniedException();
        }
    }
}
