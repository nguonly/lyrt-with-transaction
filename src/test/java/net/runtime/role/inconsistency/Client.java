package net.runtime.role.inconsistency;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by nguonly on 4/1/16.
 */
public class Client {
    public static void main(String[] args){

//        ServerMain server = new ServerMain();
//        String data = server.getData();
//
//        System.out.println("Client : " + data);

        connect();
    }

    private static void connect(){
        int N = 50;

        ClientHandler[] clients = new ClientHandler[N];

        for(int i=0; i<N; i++){
            clients[i] = new ClientHandler();
            clients[i].start();
        }
    }

   static class ClientHandler extends Thread {

       @Override
       public void run() {
           try {
               Socket socket = new Socket("localhost", 8888);
               //System.out.println("Connect to server " + socket.getInetAddress());

               //Read the first chunk to display. The assertion has been placed in the server.
               //So it ensures that the data in a transaction is consistent.
               BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
               String data = in.readLine();
               System.out.println("From server : " + data);
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
   }
}
