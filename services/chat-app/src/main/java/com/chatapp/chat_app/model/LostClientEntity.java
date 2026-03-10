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

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();  // auto-generate UUID string
        }
    }

    private String clientId;

    private LocalDateTime createdAt;

}
