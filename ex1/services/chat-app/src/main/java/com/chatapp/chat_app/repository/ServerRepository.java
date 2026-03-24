package com.chatapp.chat_app.repository;

import com.chatapp.chat_app.model.ServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// we need to override updates to make updating stats threadsafe
@Repository
public interface ServerRepository extends JpaRepository<ServerEntity, String> {

    Optional<ServerEntity> findByHost(String host);
    List<ServerEntity> findByLastHeartbeatAtBefore(LocalDateTime cutoff);

}

