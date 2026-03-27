package com.chatapp.chat_app.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Entity
@Table(name = "clients")
public class ClientEntity {
    @Id
    private String id;

    @Column(unique=true, nullable=false)
    private String clientName;

    private LocalDateTime arrivedAt;



    public ClientEntity() {}

    public ClientEntity(String clientName) {
        this.id = UUID.randomUUID().toString();
        this.clientName = clientName;
        this.arrivedAt = LocalDateTime.now();
    }

}
