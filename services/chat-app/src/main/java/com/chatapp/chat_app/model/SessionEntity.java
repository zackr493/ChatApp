package com.chatapp.chat_app.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sessions")
public class SessionEntity {


    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "client_id" , nullable = false)
    private ClientEntity clientEntity;

    @ManyToOne
    @JoinColumn(name = "server_id",  nullable = true)
    private ServerEntity serverEntity;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int rating;

    public SessionEntity(ClientEntity clientEntity, ServerEntity serverEntity) {
        this.clientEntity = clientEntity;
        this.serverEntity = serverEntity;
        this.startTime = LocalDateTime.now();
    }

    public SessionEntity(ClientEntity clientEntity) {
        this(clientEntity, null);
    }

    public SessionEntity() {}




}
