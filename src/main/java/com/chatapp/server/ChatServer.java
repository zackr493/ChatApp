package com.chatapp.server;

import com.chatapp.model.Client;
import com.chatapp.model.Server;
import com.chatapp.model.Session;
import com.chatapp.utils.ClientLoader;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChatServer {

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.out.println("FILE LOCATION NOT FOUND. java ChatServer <clients-file-path> <num-servers>");
        }

        final String filePath = args[0] ;
        final Integer NUM_SERVER = Integer.parseInt(args[1]);

        List<Client> clients = ClientLoader.loadClients(filePath) ;
        System.out.println("Loaded " + clients.size() + "clients");

        ServerManager manager = new ServerManager(NUM_SERVER) ;

        // limit threads to 50, if overflow place in queue
        ExecutorService executor = Executors.newFixedThreadPool(50) ;

        for (Client client : clients) {
            executor.submit(() -> manager.handleClientJoining(client));
        }



        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        manager.printServerStats();









    }


}
