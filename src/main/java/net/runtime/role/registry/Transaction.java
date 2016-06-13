package net.runtime.role.registry;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.HashMap;

/**
 * Created by nguonly on 3/25/16.
 */
public class Transaction implements AutoCloseable {

    public Transaction(){
        beginTransaction();
    }

    public void beginTransaction(){
        RegistryManager.m_lock.lock();
        try {
            long threadId = Thread.currentThread().getId();
            String msg = String.format("[%d] begins TX @ %s", threadId, LocalDateTime.now());
//            System.out.println(msg);
            RegistryManager.registerTransaction(threadId, this.hashCode());
        }finally {
            RegistryManager.m_lock.unlock();
        }

    }

    @Override
    public void close()  {
        endTransaction();
    }

    public void endTransaction(){
        RegistryManager.m_lock.lock();
        try {
            long threadId = Thread.currentThread().getId();
//            System.out.format("[%d] ends TX @ %s\n", threadId, LocalDateTime.now());
            //Remove phantom roles
            RegistryManager.delPhantomRoles(this);
            //clear transaction log in current thread
            HashMap<Long, AbstractMap.SimpleEntry<Integer, LocalDateTime>> logTheads = RegistryManager.getLogThreads();
            AbstractMap.SimpleEntry<Integer, LocalDateTime> keyValue = logTheads.get(Thread.currentThread().getId());
            if (keyValue.getKey() == this.hashCode()) {
                logTheads.remove(Thread.currentThread().getId());
            }
        }finally {
            RegistryManager.m_lock.unlock();
        }
    }
}
