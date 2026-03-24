package com.chatapp.chat_app.service;

import com.chatapp.chat_app.dto.RegisterServerResponse;
import com.chatapp.chat_app.dto.SessionStatus;
import com.chatapp.chat_app.model.*;
import com.chatapp.chat_app.repository.ClientRepository;
import com.chatapp.chat_app.repository.LostClientRepository;
import com.chatapp.chat_app.repository.ServerRepository;
import com.chatapp.chat_app.repository.SessionRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;



@Service
@RequiredArgsConstructor
public class ServerManager {

    private static final Logger logger = LoggerFactory.getLogger(ServerManager.class);

    private final ServerRepository serverRepository;
    private final SessionRepository sessionRepository;
    private final LostClientRepository lostClientRepository;




    // thread safe , blocks caller until space is available or an item arrives
    private final LinkedBlockingQueue<WaitingClient> waitingQueue = new LinkedBlockingQueue<>();


    public RegisterServerResponse registerServer(String serverName, String host) {
        // If the same host re-registers (e.g. after a restart), reuse the record
        logger.info("REGISTER SERVER CALLED: {} ", serverName, host);
        Optional<ServerEntity> existing = serverRepository.findByHost(host);
        logger.info("EXISTING: {} ", existing);
        if (existing.isPresent()) {
            ServerEntity s = existing.get();
            s.setServerName(serverName);
            s.setCurrClientId(null);
            serverRepository.save(s);
            startWorkerThread(s.getId(), s.getServerName());
            logger.info("Re-registered server: {} at {}", serverName, host);
            return new RegisterServerResponse(
                    "ALREADY REGISTERED",
                    "Server already exists at host: " + host,
                    null,
                    existing.get()
            );
        }

        ServerEntity server = ServerEntity.builder()
                .id(UUID.randomUUID().toString())
                .serverName(serverName)
                .host(host)
                .numClientsDay(0)
                .numClientsMonth(0)
                .ratingTotal(0)
                .ratingCount(0)
                .build();

        serverRepository.save(server);
        startWorkerThread(server.getId(), server.getServerName());
        logger.info("Registered new server: {} at {}", serverName, host);
        return new RegisterServerResponse(
                "REGISTERED",
                "Server registered successfully",
                server.getId(),
                server
        );
    }

    public void enqueueClient(String clientId, String sessionId, int timeoutMs) {
        WaitingClient wc = new WaitingClient(clientId, sessionId, timeoutMs);
        waitingQueue.offer(wc);
        logger.info("Client {} enqueued. Queue size now: {}", clientId, waitingQueue.size());
    }

    public List<ServerEntity> getAllServers() {
        return serverRepository.findAll();
    }

    public Optional<ServerEntity> getServerById(String id) {
        return serverRepository.findById(id);
    }



    // concurrent loop that worker runs forever, each instance when not waiting, handles a client
    private void workerLoop(String serverId, String serverName) {
        logger.info("[{}] Worker started", serverName);


        // this will run as long as the thread is not interrupted
        while (!Thread.currentThread().isInterrupted()) {
            try {

                // linkedblockingqueue.poll() aims to return a client from the queue
                // if exist , it retrieves first element and removes from queue
                // if queue is empty, it waits 1s , and returns null
                // this makes the loop stay responsive and checking if it is interrupted

                // we do this because of how take() works
                // for take if queue has an item , we return it ,
                // if it is empty , it sleeps, parking the thread and make it stay blocked until another thread add something
                WaitingClient client = waitingQueue.poll(1, TimeUnit.SECONDS);
                if (client == null) continue;  // nothing in queue, loop back


                // now that we have a client , we check if is expired (past 5mins) (
                if (client.isExpired()) {
                    logger.warn("[{}] Client {} expired in queue — marking LOST", serverName, client.getClientId());
                    markLost(client);
                    continue;
                }

                // not expired, we assign the client
                assignClient(serverId, serverName, client);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("[{}] Worker thread interrupted, shutting down", serverName);
            } catch (Exception e) {
                // Catch-all so a single bad client doesn't kill the worker thread
                logger.error("[{}] Unexpected error in worker loop", serverName, e);
            }
        }
    }

    public void recordHeartbeat(String serverHost) {
        ServerEntity server = serverRepository.findByHost(serverHost)
                .orElseThrow(() -> new RuntimeException("Server not found: " + serverHost));
        server.setLastHeartbeatAt(LocalDateTime.now());
        serverRepository.save(server);
        logger.debug("Heartbeat recorded for server {}", serverHost);
    }

    private void assignClient(String serverId, String serverName, WaitingClient client) {
        // this function gets the fresh client object from db and assigns it to server

        ServerEntity server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found: " + serverId));

        server.setCurrClientId(client.getClientId());
        serverRepository.save(server);

        SessionEntity session = sessionRepository.findById(client.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found: " + client.getSessionId()));

        session.setServerEntity(server);
        session.setStatus(SessionStatus.ASSIGNED);
        sessionRepository.save(session);

        logger.info("[{}] Assigned to client {}, sessionId={}", serverName, client.getClientId(), client.getSessionId());
    }

    private void markLost(WaitingClient client) {
        // this function marks session as lost and creates a lost client instance in db

        sessionRepository.findById(client.getSessionId()).ifPresent(session -> {
            session.setStatus(SessionStatus.LOST);
            session.setEndTime(LocalDateTime.now());
            sessionRepository.save(session);
        });

        // Persist lost client record
        LostClientEntity lost = LostClientEntity.builder()
                .clientId(client.getClientId())
                .createdAt(LocalDateTime.now())
                .build();
        lostClientRepository.save(lost);

        logger.warn("Client {} marked as LOST (sessionId={})", client.getClientId(), client.getSessionId());
    }

    private void startWorkerThread(String serverId, String serverName) {
        // this function spawns a new thread to run workerloop
        Thread worker = new Thread(() -> workerLoop(serverId, serverName));
        worker.setName("chat-worker-" + serverName);

        // we dont want jvm to hang when worker is blocked on poll()
        // this allows automatic shutdown without manually stopping every worker thread
        worker.setDaemon(true);
        worker.start();
        logger.info("Worker thread started for server {}", serverName);
    }

    private ServerEntity buildNewServer(int index) {
        // Generates names: Server 1, Server 2, Server 3 ...


        return ServerEntity.builder()
                .id(UUID.randomUUID().toString())
                .serverName("Server " + index)
                .numClientsDay(0)
                .numClientsMonth(0)
                .ratingTotal(0)
                .ratingCount(0)
                .build();
    }
}











