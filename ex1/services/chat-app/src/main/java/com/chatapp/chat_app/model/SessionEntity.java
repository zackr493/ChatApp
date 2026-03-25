package com.chatapp.chat_app.model;

import com.chatapp.chat_app.dto.SessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sessions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionEntity {


    @Id
    private String id;

    // this should already be generated , this is just fallback
    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    private SessionStatus status;


    @ManyToOne
    @JoinColumn(name = "client_id" , nullable = false)
    private ClientEntity clientEntity;

    @ManyToOne
    @JoinColumn(name = "server_id",  nullable = true)
    private ServerEntity serverEntity;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int rating;



    // instantiate only client , not server
    public SessionEntity(ClientEntity clientEntity) {
        this.clientEntity = clientEntity;
        this.serverEntity = null; // no server yet
        this.startTime = LocalDateTime.now(); // optional default
        this.endTime = null;
        this.rating = 0; // optional default
    }





}
