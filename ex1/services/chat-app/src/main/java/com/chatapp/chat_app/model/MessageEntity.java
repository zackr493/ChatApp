package com.chatapp.chat_app.model;


import com.chatapp.chat_app.dto.MessageRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "messages")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageEntity {

    @Id
    private String id;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    private String sessionId;
    private String clientId;
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    private MessageRole role;

    private LocalDateTime sentAt;



}
