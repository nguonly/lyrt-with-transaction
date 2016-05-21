package net.runtime.role.transfersystem.account;


import net.runtime.role.actor.Player;

/**
 * Created by nguonly on 8/19/15.
 */
public class InterAccountTransferSystem extends Player {
    public void transfer(Account from, Account to, float amount) {
        //to.credit(amount);
        to.invoke("credit", void.class, new Class[]{float.class}, new Object[]{amount});
        //from.debit(amount);
        from.invoke("debit", void.class, new Class[]{float.class}, new Object[]{amount});
    }
}
