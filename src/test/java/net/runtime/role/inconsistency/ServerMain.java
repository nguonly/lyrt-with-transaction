package net.runtime.role.inconsistency;

import net.runtime.role.actor.Compartment;
import net.runtime.role.helper.DumpHelper;
import net.runtime.role.orm.Relation;
import net.runtime.role.registry.RegistryManager;
import org.junit.Assert;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by nguonly on 4/1/16.
 */
public class ServerMain {
    static ScheduledExecutorService m_pool = Executors.newScheduledThreadPool(110);
    static Transfer m_transfer = new Transfer();

    static Random m_random = new Random();
    static int m_min = 50;
    static int m_max = 500;

    public static void main(String[] args){
        accept();

    }

    public String getData(){
        int N = 100;

        String firstChunk = "";
        String chunk = "";

        for(int i=0; i<N; i++) {
            chunk = m_transfer.send();
            if(i==0) firstChunk = chunk;

            Assert.assertEquals(firstChunk, chunk);
        }

        return firstChunk;
    }

    private static void accept(){
        Compartment compartment = new Compartment();
        compartment.activate();
        int timer = 10;
        boolean isAcceptedClient = false;

        try {
            ServerSocket server = new ServerSocket(8888);
            while(true){
                System.out.println("Waiting for client...");
                Socket socket = server.accept();
                System.out.println("Got client from : " + socket.getInetAddress());

                if(!isAcceptedClient) {
                    isAcceptedClient = true;

                    //Schedule to inject role
                    injectRole();
//                    System.out.println("Inject role");
                    Runnable logTrans = () -> DumpHelper.dumpLogThread();
                    m_pool.schedule(logTrans, 200, TimeUnit.MILLISECONDS);
                }

                //Executing client request in thread
                ServiceHandler sh = new ServiceHandler(socket, m_transfer);
//                m_pool.execute(sh);
                int randomNumber = m_random.nextInt(m_max - m_min) + m_min;
                m_pool.schedule(sh, timer, TimeUnit.MILLISECONDS);
                timer = timer + 10;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void injectRole(){
        RegistryManager reg = RegistryManager.getInstance();

        Runnable r1 = () -> {
            //LOG.println("Inject Compression");
            reg.bind(null, m_transfer, Compression.class, null, null);
            //System.out.println("Inject Compression :: ThreadId : " + Thread.currentThread().getId());
            Relation rel = reg.getRelations().stream().filter(
                    c->c.getRoleName().contains("Compression")
            ).findFirst().get();
            LOG.println("Inject Compression @ " + rel.getBoundTime());
        };

        Runnable r2 = () -> {
            //LOG.println("Inject Encryption");
            reg.bind(null, m_transfer, Encryption.class, null, null);
            //System.out.println("Inject Encryption :: ThreadId : " + Thread.currentThread().getId());
            Relation rel = reg.getRelations().stream().filter(
                    c->c.getRoleName().contains("Encryption")
            ).findFirst().get();
            LOG.println("Inject Encryption @ " + rel.getBoundTime());

        };

        Runnable r3 = () -> {
            reg.bind(null, m_transfer, Logging.class, null, null);
            Relation rel = reg.getRelations().stream().filter(
                    c->c.getRoleName().contains("Encryption")
            ).findFirst().get();
            LOG.println("Inject Logging @ " + rel.getBoundTime());
        };


//        int randomNumber = m_random.nextInt(m_max - m_min) + m_min;
//        int randomNumber2 = m_random.nextInt(m_max - m_min) + m_min;
//        int randomNumber3 = m_random.nextInt(m_max - m_min) + m_min;
//        m_pool.schedule(r1, randomNumber, TimeUnit.MILLISECONDS);
//        m_pool.schedule(r2, randomNumber2, TimeUnit.MILLISECONDS);
//        m_pool.schedule(r3, randomNumber3, TimeUnit.MILLISECONDS);
        m_pool.schedule(r1, 100, TimeUnit.MILLISECONDS);
        m_pool.schedule(r2, 200, TimeUnit.MILLISECONDS);
        m_pool.schedule(r3, 300, TimeUnit.MILLISECONDS);
    }
}
