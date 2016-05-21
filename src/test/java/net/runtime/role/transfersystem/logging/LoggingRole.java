package net.runtime.role.transfersystem.logging;


import net.runtime.role.actor.Role;
import net.runtime.role.transfersystem.Main;
import net.runtime.role.transfersystem.account.Account;

/**
 * Created by nguonly on 8/19/15.
 */
public class LoggingRole extends Role {

    //for main
    public void commitTransfer(){
        System.out.println("enter layer");
        this.base(Main.class).commitTransfer();
        System.out.println("exit layer");
    }

    //for InterAccountTransferSystem
    public void transfer(Account from, Account to, float amount){
        System.out.println("<transfer>");
        System.out.println(" transfer amount " + amount);
        //proceed(from, to, amount);
        //this.base(InterAccountTransferSystem.class).transfer(from, to, amount);
        invokeBase("transfer", void.class, new Class[]{Account.class, Account.class, float.class},
                new Object[]{from, to, amount});
        System.out.println("</transfer>");
    }

    //for Account "before"
    public void credit(float amount){
        Account root = (Account)getRootPlayer();
        //Bank b = invokeCor
        System.out.println(" account (" + root.getBank() + "," + root.getAccountNumber() + "): credit ->" + amount);
        invokeBase("credit", void.class, new Class[]{float.class}, new Object[]{amount});
    }

    //for Account "after"
    public void debit(float amount){
        //base(EncryptionRole.class).credit(amount);
        invokeBase("debit", void.class, new Class[]{float.class}, new Object[]{amount});
        Account root = (Account)getRootPlayer();
        System.out.println(" account (" + root.getBank() + "," + root.getAccountNumber() + "): debit ->" + amount);
    }
}
