package net.runtime.role.inconsistency;

import net.runtime.role.registry.Transaction;
import net.runtime.role.registry.RegistryManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by nguonly on 4/1/16.
 */
public class ServiceHandler implements Runnable {

    private Socket m_socket;
    private Transfer m_transfer;
    private String firstChunk = "";
    private int m_N = 50;

    public ServiceHandler(Socket socket, Transfer transfer){
        m_socket = socket;
        m_transfer = transfer;
    }

    @Override
    public void run() {
        LOG.println(" first Execute");
        String chunk;
        boolean isConsistent = true;
        RegistryManager reg = RegistryManager.getInstance();

        try(Transaction tx = new Transaction()) {

            for (int i = 0; i < m_N; i++) {
                //chunk = m_transfer.send();
                chunk = reg.invokeRole(null, m_transfer, "send", String.class, null, null);

                if (i == 0) {
                    firstChunk = chunk;
//                    LOG.println(" first Execute");
                }

                //Assert.assertEquals(firstChunk, chunk);
                if(!firstChunk.equalsIgnoreCase(chunk)){
                    isConsistent = false;
                    //System.out.println("NOoooooT equal");

                }

                //delay for experiemental injection new role
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Send back to client the result of the first chunk
            try {
                PrintWriter out = new PrintWriter(m_socket.getOutputStream(), true);
                String msg = String.format("[%d] %s", Thread.currentThread().getId(), isConsistent ? firstChunk : "<<<< BREAK >>>>");
                out.println(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
