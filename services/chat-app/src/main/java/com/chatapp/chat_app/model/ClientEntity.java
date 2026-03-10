package com.chatapp.chat_app.model;

import com.chatapp.chat_app.dto.ClientStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "clients")
public class ClientEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private LocalDateTime arrivedAt;

    @Enumerated(EnumType.STRING)
    private ClientStatus status;

    public ClientEntity() {}

    public ClientEntity(String name) {
        this.id = name ;
        this.arrivedAt = LocalDateTime.now() ;
        this.status = ClientStatus.CREATED;

    }

}
