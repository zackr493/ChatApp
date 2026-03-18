package com.chatapp.chat_app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@Entity
@Table(name = "servers")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerEntity {

    @Id
    private String id;

    // in case id is not set
    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
    }

    private String serverName;

    private int numClientsDay   = 0;
    private int numClientsMonth = 0;
    private int ratingTotal     = 0;
    private int ratingCount     = 0;

    private String currClientId;


    // for optimistic locking , we dont use pessimistic to avoid deadlocks
    //https://stackoverflow.com/questions/129329/optimistic-vs-pessimistic-locking
    @Version
    private Long version;

    public double getAverageRating() {
        return ratingCount == 0 ? 0.0 : (double) ratingTotal / ratingCount;
    }
}
