package com.chatapp.chat_app.repository;

import com.chatapp.chat_app.model.ClientEntity;
import com.chatapp.chat_app.model.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, String> {
    // No extra methods needed for basic CRUD
}