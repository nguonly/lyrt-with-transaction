package net.runtime.role.transfersystem.encryption;


import net.runtime.role.actor.Role;
import net.runtime.role.transfersystem.account.Account;

/**
 * Created by nguonly on 8/19/15.
 */
public class EncryptionRole extends Role {
    public void credit(float amount){
        //this.invokeBase("credit");
        //this.base(Account.class).credit(Encryption.decrypt(amount));
        this.invokeBase("credit", void.class, new Class[]{float.class}, new Object[]{Encryption.decrypt(amount)});
    }

    public void debit(float amount){

        //this.base(Account.class).debit(Encryption.decrypt(amount));
        this.invokeBase("debit", void.class, new Class[]{float.class}, new Object[]{Encryption.decrypt(amount)});
    }

    public float getBalance(){

        //return Encryption.encrypt(this.base(Account.class).getBalance());
        return Encryption.encrypt(this.invokeBase("getBalance", float.class));
    }

    //for InterAccountTransferSystem
    public void transfer(Account from, Account to, float amount) {
        //this.base(InterAccountTransferSystem.class).transfer(from, to, Encryption.encrypt(amount));
        this.invokeBase("transfer", void.class, new Class[]{Account.class, Account.class, float.class},
            new Object[]{from, to, Encryption.encrypt(amount)});
    }
}
