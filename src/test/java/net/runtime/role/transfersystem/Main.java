package net.runtime.role.transfersystem;


import net.runtime.role.actor.Compartment;
import net.runtime.role.actor.Player;
import net.runtime.role.transfersystem.account.Account;
import net.runtime.role.transfersystem.account.Bank;
import net.runtime.role.transfersystem.account.InterAccountTransferSystem;
import net.runtime.role.transfersystem.encryption.EncryptionRole;
import net.runtime.role.transfersystem.logging.LoggingRole;

/**
 * Created by nguonly on 8/19/15.
 */
public class Main extends Player {
    private Account a, b, c;

    public static void main(String[] args){
        try(Compartment comp = Compartment.initialize(Compartment.class)) {
            Main main = Player.initialize(Main.class);
            main.bind(LoggingRole.class);
            main.transfer();
        }
    }

    public void transfer() {
        Bank deutscheBank = new Bank("Deutsche Bank");
        //a = new Account(deutscheBank, 1);
        a = Player.initialize(Account.class, new Class[]{Bank.class, int.class}, new Object[]{deutscheBank, 1});
        a.credit(2000000);
        //b = new Account(deutscheBank, 2);
        b = Player.initialize(Account.class, new Class[]{Bank.class, int.class}, new Object[]{deutscheBank, 2});
        b.credit(5000000);

        Bank bankOfAmerica = new Bank("Bank of America");
        //c = new Account(bankOfAmerica, 1);
        c = Player.initialize(Account.class, new Class[]{Bank.class, int.class}, new Object[]{bankOfAmerica, 1});
        c.credit(13000045);


//            a.bind(EncryptionRole.class).bind(LoggingRole.class);
//            b.bind(EncryptionRole.class).bind(LoggingRole.class);
//            c.bind(EncryptionRole.class).bind(LoggingRole.class);

        a.bind(LoggingRole.class).bind(EncryptionRole.class);
        b.bind(LoggingRole.class).bind(EncryptionRole.class);
        c.bind(LoggingRole.class).bind(EncryptionRole.class);

            //commitTransfer();
            invoke("commitTransfer");

    }

    public  void commitTransfer() {
        InterAccountTransferSystem transfer = Player.initialize(InterAccountTransferSystem.class);

        transfer.bind(LoggingRole.class).bind(EncryptionRole.class);

        transfer.invoke("transfer", void.class, new Class[]{Account.class, Account.class, float.class},
                new Object[]{a, b, 300f});

        transfer.invoke("transfer", void.class, new Class[]{Account.class, Account.class, float.class},
                new Object[]{b, c, 300f});
    }
}
