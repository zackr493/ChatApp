package com.chatapp.chat_app.repository;

import com.chatapp.chat_app.model.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, String> {
    boolean existsByClientName(String clientName);
}