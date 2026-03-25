package com.chatapp.chat_app.service;

import com.chatapp.chat_app.dto.RegisterServerResponse;
import com.chatapp.chat_app.dto.SessionStatus;
import com.chatapp.chat_app.model.*;
import com.chatapp.chat_app.repository.LostClientRepository;
import com.chatapp.chat_app.repository.ServerRepository;
import com.chatapp.chat_app.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;



@Service
@RequiredArgsConstructor
public class ServerManager {

    private static final Logger logger = LoggerFactory.getLogger(ServerManager.class);

    private final ServerRepository serverRepository;
    private final SessionRepository sessionRepository;
    private final LostClientRepository lostClientRepository;




    // thread safe , blocks caller until space is available or an item arrives
    private final LinkedBlockingQueue<WaitingClient> waitingQueue = new LinkedBlockingQueue<>();

    public WaitingClient enqueueClient(String clientId, String sessionId) {
        WaitingClient wc = new WaitingClient(clientId, sessionId);
        waitingQueue.offer(wc);
        logger.info("Client {} enqueued. Queue size: {}", clientId, waitingQueue.size());

        // queue only holds back when nginx returns 502
        wc.getReadyLatch().countDown();

        return wc;
    }

    public void signalNextClient() {
        WaitingClient next = waitingQueue.poll();
        if (next != null) {
            next.getReadyLatch().countDown();
            logger.info("Signalled next client: {}", next.getClientId());
        }
    }



    public List<ServerEntity> getAllServers() {
        return serverRepository.findAll();
    }

    public Optional<ServerEntity> getServerById(String id) {
        return serverRepository.findById(id);
    }

    public RegisterServerResponse registerServer(String serverName, String host) {
        logger.info("REGISTER SERVER CALLED: name={}, host={}", serverName, host);
        Optional<ServerEntity> existing = serverRepository.findByHost(host);
        logger.info("EXISTING: {} ", existing);
        if (existing.isPresent()) {
            ServerEntity s = existing.get();
            s.setServerName(serverName);
            s.setLastHeartbeatAt(LocalDateTime.now());
            serverRepository.save(s);
            logger.info("Re-registered server: {} at {}", serverName, host);
            return new RegisterServerResponse(
                    "REGISTERED",
                    "Server re-registered successfully",
                    s.getId(),
                    s
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
        logger.info("Registered new server: {} at {}", serverName, host);
        return new RegisterServerResponse(
                "REGISTERED",
                "Server registered successfully",
                server.getId(),
                server
        );
    }


    public void recordHeartbeat(String serverHost) {
        ServerEntity server = serverRepository.findByHost(serverHost)
                .orElseThrow(() -> new RuntimeException("Server not found: " + serverHost));
        server.setLastHeartbeatAt(LocalDateTime.now());
        serverRepository.save(server);
        logger.debug("Heartbeat recorded for server {}", serverHost);
    }



    public void markLost(WaitingClient client) {
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


}











