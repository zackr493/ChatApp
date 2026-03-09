package com.chatapp.chat_app.service;

import com.chatapp.chat_app.dto.Client;
import com.chatapp.chat_app.dto.ClientStatus;
import com.chatapp.chat_app.dto.Server;
import com.chatapp.chat_app.dto.Session;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerManager {


    // normal arraylist is not thread safe, adding multiple threads will cause race conditions
    // synclist only one thread can access methods
    private final List<Server> servers = Collections.synchronizedList(new ArrayList<Server>());

    // instead of queue, we use semaphore to allows access to a limited number of resource,
    // we dont need to handle locking, and we dont need strict FIFO waiting

    // semaphore methods :
    // acquire() -> tries, else waits
    // tryAcquire -> tries, or fail if timeout
    // release()
    private final Semaphore semaphore;

    // normal int does 3 operations which is not thread safe
    private final AtomicInteger totalClients = new AtomicInteger() ;
    private final AtomicInteger lostClients = new AtomicInteger() ;

    private final Map<String, Client> clientMap = new ConcurrentHashMap<>();
    private final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    public ServerManager(int maxServers) {


        this.semaphore = new Semaphore(maxServers);

        for (int i = 1; i <= maxServers; i++) {
            // arbituary naming
            servers.add(new Server("Server" + i));

        }

    }

    // CRUD for clients, in this case we do not need DELETE or UPDATE, we only need to update status
    public boolean addClient(Client client) {

        System.out.println(clientMap);
        System.out.println(client);
        Client existing = clientMap.putIfAbsent(client.getId(), client);

        if (existing == null) {
            client.setStatus(ClientStatus.CREATED);

            // uuid for session
            String sessionId = UUID.randomUUID().toString();
            Session session = new Session(client, null);

            sessionMap.put(sessionId, session) ;

            return true;
        }
        return false;
    }

    public Client getClient(String name) {
        return clientMap.get(name);
    }


    public List<Client> getAllClients() {
        return new ArrayList<>(clientMap.values());
    }

    public boolean updateClientStatus(Client client, ClientStatus status) {
        Client existing = clientMap.get(client.getId());

        if (existing == null) {
            return false; // client does not exist
        }

        existing.setStatus(status);
        return true;
    }

    public void handleClientJoining(Client client, int timeToExpiry) {

        // thread safe increment
        totalClients.incrementAndGet();


        try {
            // try to acquire a server within 5 minutes (in MS )
            boolean serverAcquired = semaphore.tryAcquire(timeToExpiry, java.util.concurrent.TimeUnit.MILLISECONDS);
            // if server not acquired within 5minutes, we add to lost clients
            if (!serverAcquired) {
                lostClients.incrementAndGet();
                System.out.println("LOST CLIENT: " + client.getId());
                return;


            }

            // when server acquired, find server that is free, there should be one available
            Server server = getAvailServer();

            if (server == null) {
                semaphore.release();
                return;
            }

            synchronized (server) {
                server.setCurrClient(client);
            }

//          // attach server to existing session
            // find session tied to client
            Session session = sessionMap.values().stream().filter(s -> s.getClient() == client).findFirst().orElse(null);

            if (session != null) {
                session.setServer(server);
            }

            System.out.println(client.getId() + " is connected to " + server.getId());




        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Return all servers
    public List<Server> getServers() {
        synchronized (servers) { // synchronized since servers list is wrapped in synchronizedList
            return new ArrayList<>(servers);
        }
    }

    // Get server by name
    public Server getServerByName(String name) {
        synchronized (servers) {
            for (Server server : servers) {
                if (server.getId().equals(name)) {
                    return server;
                }
            }
        }
        return null;
    }

    public Server getAvailServer() {
        synchronized (servers) {
            for (Server server : servers) {
                if (server.getCurrClient() == null) {
                    return server;
                }
            }
        }

        return null ;
    }

    public void finishSession(String sessionId, int rating) {

        Session session = sessionMap.get(sessionId);

        if (session == null) {
            System.out.println("Session not found");
            return;
        }


        Server server = session.getServer();
        Client client = session.getClient();


        // synchronized ensures only one thread at a time can enter a synchronized method on the same object
        // otheres will be blocked until lock released
        if (server != null) {
            synchronized (server) {
                server.setNumClientsDay(server.getNumClientsDay() + 1);
                server.setNumClientsMonth(server.getNumClientsMonth() + 1);
                server.setRatingTotal(server.getRatingTotal() + rating);
                server.setRatingCount(server.getRatingCount() + 1);

                System.out.println("CHAT ENDED | CLIENT: " + client.getId() + " SERVER: " + server.getId());

                server.setCurrClient(null);

                semaphore.release();
            }

        }
        else {
            System.out.println("No server assigned for session of client");
        }

        session.setEndTime(LocalDateTime.now());
        session.setRating(rating) ;
        sessionMap.remove(sessionId);



    }

    public void printServerStats() {
        int totalServedToday = 0;
        int totalServedMonth = 0;
        double totalRating = 0;
        int totalRatingCount = 0;

        System.out.println("==== SERVER STATISTICS ====");

        for (Server server : servers) {
            int servedToday = server.getNumClientsDay();
            int servedMonth = server.getNumClientsMonth();
            double avgRating = server.getAverageRating();

            totalServedToday += servedToday;
            totalServedMonth += servedMonth;

            totalRating += server.getRatingTotal();
            totalRatingCount += server.getRatingCount();

            System.out.println("Server: " + server.getId());
            System.out.println(" Clients served today: " + servedToday);
            System.out.println(" Clients served this month: " + servedMonth);
            System.out.println(" Average Rating: " + avgRating);
        }

        double overallAvgRating = totalRatingCount > 0 ? totalRating / totalRatingCount : 0 ;

        System.out.println("==== GLOBAL STATS ====");
        System.out.println("Total Clients Simulated: " + totalClients);
        System.out.println("Total Clients Lost: " + lostClients);
        System.out.println("Overall average rating: " + overallAvgRating);
    }







}
