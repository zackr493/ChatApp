package com.chatapp.chat_app.repository;

import com.chatapp.chat_app.model.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, String> {
    // No extra methods needed for basic CRUD
}