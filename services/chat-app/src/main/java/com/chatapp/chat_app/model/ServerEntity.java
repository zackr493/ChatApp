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

    // this should already be generated , this is just fallback
    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    private int numClientsDay = 0;

    private int numClientsMonth = 0;

    private int ratingTotal = 0;

    private int ratingCount = 0;

    // nullable true
    private String currClientId;

    // this key provides optimisticLocking, when transactions occur, others are blocked
    // checks version before update, retries on conflict
    @Version
    private Long version;

    public double getAverageRating() {
        if (ratingCount == 0) {
            return 0;
        }

        return (double) ratingTotal / ratingCount ;
    }

}
