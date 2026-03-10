package com.chatapp.chat_app.repository;

import com.chatapp.chat_app.model.LostClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LostClientRepository extends JpaRepository<LostClientEntity, Long> {
}