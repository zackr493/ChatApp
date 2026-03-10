package com.chatapp.chat_app.service;

import com.chatapp.chat_app.model.ClientEntity;
import com.chatapp.chat_app.dto.ClientStatus;
import com.chatapp.chat_app.model.ServerEntity;
import com.chatapp.chat_app.model.SessionEntity;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerManager {


    // normal arraylist is not thread safe, adding multiple threads will cause race conditions
    // synclist only one thread can access methods
    private final List<ServerEntity> serverEntities = Collections.synchronizedList(new ArrayList<ServerEntity>());

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

    private final Map<String, ClientEntity> clientMap = new ConcurrentHashMap<>();
    private final Map<String, SessionEntity> sessionMap = new ConcurrentHashMap<>();

    public ServerManager(int maxServers) {


        this.semaphore = new Semaphore(maxServers);

        for (int i = 1; i <= maxServers; i++) {
            // arbituary naming
            serverEntities.add(new ServerEntity("Server" + i));

        }

    }

    // CRUD for clients, in this case we do not need DELETE or UPDATE, we only need to update status
    public boolean addClient(ClientEntity clientEntity) {

        System.out.println(clientMap);
        System.out.println(clientEntity);
        ClientEntity existing = clientMap.putIfAbsent(clientEntity.getId(), clientEntity);

        if (existing == null) {
            clientEntity.setStatus(ClientStatus.CREATED);

            // uuid for session
            String sessionId = UUID.randomUUID().toString();
            SessionEntity sessionEntity = new SessionEntity(clientEntity, null);

            sessionMap.put(sessionId, sessionEntity) ;

            return true;
        }
        return false;
    }

    public ClientEntity getClient(String name) {
        return clientMap.get(name);
    }


    public List<ClientEntity> getAllClients() {
        return new ArrayList<>(clientMap.values());
    }

    public boolean updateClientStatus(ClientEntity clientEntity, ClientStatus status) {
        ClientEntity existing = clientMap.get(clientEntity.getId());

        if (existing == null) {
            return false; // client does not exist
        }

        existing.setStatus(status);
        return true;
    }

    public void handleClientJoining(ClientEntity clientEntity, int timeToExpiry) {

        // thread safe increment
        totalClients.incrementAndGet();


        try {
            // try to acquire a server within 5 minutes (in MS )
            boolean serverAcquired = semaphore.tryAcquire(timeToExpiry, java.util.concurrent.TimeUnit.MILLISECONDS);
            // if server not acquired within 5minutes, we add to lost clients
            if (!serverAcquired) {
                lostClients.incrementAndGet();
                System.out.println("LOST CLIENT: " + clientEntity.getId());
                return;


            }

            // when server acquired, find server that is free, there should be one available
            ServerEntity serverEntity = getAvailServer();

            if (serverEntity == null) {
                semaphore.release();
                return;
            }

            synchronized (serverEntity) {
                serverEntity.setCurrClientEntity(clientEntity.getId());
            }

//          // attach server to existing session
            // find session tied to client
            SessionEntity sessionEntity = sessionMap.values().stream().filter(s -> s.getClientEntity() == clientEntity).findFirst().orElse(null);

            if (sessionEntity != null) {
                sessionEntity.setServerEntity(serverEntity);
            }

            System.out.println(clientEntity.getId() + " is connected to " + serverEntity.getId());




        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Return all servers
    public List<ServerEntity> getServers() {
        synchronized (serverEntities) { // synchronized since servers list is wrapped in synchronizedList
            return new ArrayList<>(serverEntities);
        }
    }

    // Get server by name
    public ServerEntity getServerByName(String name) {
        synchronized (serverEntities) {
            for (ServerEntity serverEntity : serverEntities) {
                if (serverEntity.getId().equals(name)) {
                    return serverEntity;
                }
            }
        }
        return null;
    }

    public ServerEntity getAvailServer() {
        synchronized (serverEntities) {
            for (ServerEntity serverEntity : serverEntities) {
                if (serverEntity.getCurrClientEntity() == null) {
                    return serverEntity;
                }
            }
        }

        return null ;
    }

    public void finishSession(String sessionId, int rating) {

        SessionEntity sessionEntity = sessionMap.get(sessionId);

        if (sessionEntity == null) {
            System.out.println("Session not found");
            return;
        }


        ServerEntity serverEntity = sessionEntity.getServerEntity();
        ClientEntity clientEntity = sessionEntity.getClientEntity();


        // synchronized ensures only one thread at a time can enter a synchronized method on the same object
        // otheres will be blocked until lock released
        if (serverEntity != null) {
            synchronized (serverEntity) {
                serverEntity.setNumClientsDay(serverEntity.getNumClientsDay() + 1);
                serverEntity.setNumClientsMonth(serverEntity.getNumClientsMonth() + 1);
                serverEntity.setRatingTotal(serverEntity.getRatingTotal() + rating);
                serverEntity.setRatingCount(serverEntity.getRatingCount() + 1);

                System.out.println("CHAT ENDED | CLIENT: " + clientEntity.getId() + " SERVER: " + serverEntity.getId());

                serverEntity.setCurrClientEntity(null);

                semaphore.release();
            }

        }
        else {
            System.out.println("No server assigned for session of client");
        }

        sessionEntity.setEndTime(LocalDateTime.now());
        sessionEntity.setRating(rating) ;
        sessionMap.remove(sessionId);



    }

    public void printServerStats() {
        int totalServedToday = 0;
        int totalServedMonth = 0;
        double totalRating = 0;
        int totalRatingCount = 0;

        System.out.println("==== SERVER STATISTICS ====");

        for (ServerEntity serverEntity : serverEntities) {
            int servedToday = serverEntity.getNumClientsDay();
            int servedMonth = serverEntity.getNumClientsMonth();
            double avgRating = serverEntity.getAverageRating();

            totalServedToday += servedToday;
            totalServedMonth += servedMonth;

            totalRating += serverEntity.getRatingTotal();
            totalRatingCount += serverEntity.getRatingCount();

            System.out.println("Server: " + serverEntity.getId());
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
