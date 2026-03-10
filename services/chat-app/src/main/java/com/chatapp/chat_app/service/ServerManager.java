package com.chatapp.chat_app.service;

import com.chatapp.chat_app.dto.SessionStatus;
import com.chatapp.chat_app.model.ClientEntity;
import com.chatapp.chat_app.model.LostClientEntity;
import com.chatapp.chat_app.model.ServerEntity;
import com.chatapp.chat_app.model.SessionEntity;
import com.chatapp.chat_app.repository.ClientRepository;
import com.chatapp.chat_app.repository.LostClientRepository;
import com.chatapp.chat_app.repository.ServerRepository;
import com.chatapp.chat_app.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
public class ServerManager {




    // normal arraylist is not thread safe, adding multiple threads will cause race conditions
    // synclist only one thread can access methods
    private final ClientRepository clientRepository;
    private final SessionRepository sessionRepository;
    private final ServerRepository serverRepository;
    private final LostClientRepository lostClientRepository;

    private final int maxServers;

    // instead of queue, we use semaphore to allows access to a limited number of resource,
    // we dont need to handle locking, and we dont need strict FIFO waiting

    // semaphore methods :
    // acquire() -> tries, else waits
    // tryAcquire -> tries, or fail if timeout
    // release()
    private final Semaphore semaphore = new Semaphore(maxServers);

    public void handleClientJoining(String clientId, String sessionId, int timeToExpiry) {

        try {

            // find session tied to client
            SessionEntity session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));

            session.setStatus(SessionStatus.WAITING);
            sessionRepository.save(session);


            // try to acquire a server within 5 minutes (in MS )
            boolean serverAcquired = semaphore.tryAcquire(timeToExpiry, java.util.concurrent.TimeUnit.MILLISECONDS);
            // if server not acquired within 5 minutes, we add to lost clients



            if (!serverAcquired) {
                LostClientEntity lost = LostClientEntity.builder()
                        .clientId(clientId)
                        .createdAt(LocalDateTime.now())
                        .build();
                lostClientRepository.save(lost);

                // we update the session to finish





                System.out.println("LOST CLIENT: " + clientId);
                return;


            }



            // when server acquired, find server that is free, there should be one available
            ServerEntity serverEntity = getAvailServer();

            if (serverEntity == null) {
                semaphore.release();
                return;
            }

            serverEntity.setCurrClientId(clientId);
            serverRepository.save(serverEntity);

            // find client instance, we find client so we can do set actions with the client,
            // in this case, this is simulated
            ClientEntity client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client not found"));



            // attach server to existing session
            session.setServerEntity(serverEntity);
            session.setStatus(SessionStatus.ASSIGNED);
            sessionRepository.save(session);

            System.out.println(clientId + " is connected to " + serverEntity.getId());




        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public ServerEntity getAvailServer() {
        for (ServerEntity s : serverRepository.findAll()) {
            if (s.getCurrClientId() == null) {
                return s;
            }
        }
        return null;
    }

    public List<ServerEntity> getAllServers() {
        //  returns Iterable, convert to List
        return (List<ServerEntity>) serverRepository.findAll();
    }

    // optional because might not find
    public Optional<ServerEntity> getServerById(String id) {
        try {
            return serverRepository.findById(id);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public ServerEntity createServer() {

        ServerEntity server = ServerEntity.builder()
                .id(UUID.randomUUID().toString())
                .numClientsDay(0)
                .numClientsMonth(0)
                .ratingTotal(0)
                .ratingCount(0)
                .build();

        serverRepository.save(server);
        return server;


    }








//    public void printServerStats() {
//        int totalServedToday = 0;
//        int totalServedMonth = 0;
//        double totalRating = 0;
//        int totalRatingCount = 0;
//
//        System.out.println("==== SERVER STATISTICS ====");
//
//        for (ServerEntity serverEntity : serverEntities) {
//            int servedToday = serverEntity.getNumClientsDay();
//            int servedMonth = serverEntity.getNumClientsMonth();
//            double avgRating = serverEntity.getAverageRating();
//
//            totalServedToday += servedToday;
//            totalServedMonth += servedMonth;
//
//            totalRating += serverEntity.getRatingTotal();
//            totalRatingCount += serverEntity.getRatingCount();
//
//            System.out.println("Server: " + serverEntity.getId());
//            System.out.println(" Clients served today: " + servedToday);
//            System.out.println(" Clients served this month: " + servedMonth);
//            System.out.println(" Average Rating: " + avgRating);
//        }
//
//        double overallAvgRating = totalRatingCount > 0 ? totalRating / totalRatingCount : 0 ;
//
//        System.out.println("==== GLOBAL STATS ====");
//        System.out.println("Total Clients Simulated: " + totalClients);
//        System.out.println("Total Clients Lost: " + lostClients);
//        System.out.println("Overall average rating: " + overallAvgRating);
//    }







}
