package com.chatapp.chat_app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lost_clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LostClientEntity {

    @Id
    private String id;

    // in case id is not set
    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    private String clientId;

    private String sessionId;

    private LocalDateTime createdAt;

}
