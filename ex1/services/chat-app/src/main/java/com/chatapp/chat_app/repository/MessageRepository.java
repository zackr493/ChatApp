package com.chatapp.chat_app.repository;

import com.chatapp.chat_app.model.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    List<MessageEntity> findBySessionIdOrderBySentAt(String sessionId);
}
