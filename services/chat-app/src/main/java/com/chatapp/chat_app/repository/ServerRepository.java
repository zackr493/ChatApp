package com.chatapp.chat_app.repository;

import com.chatapp.chat_app.model.ServerEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// we need to override updates to make updating stats threadsafe
public interface ServerRepository extends CrudRepository<ServerEntity, String> {

    @Transactional
    @Modifying
    @Query("""
        UPDATE ServerEntity s
        SET s.numClientsDay = s.numClientsDay + 1,
            s.numClientsMonth = s.numClientsMonth + 1,
            s.ratingTotal = s.ratingTotal + :rating,
            s.ratingCount = s.ratingCount + 1,
            s.currClientId = NULL,
            s.version = s.version + 1
        WHERE s.id = :serverId
    """)
    int incrementStats(@Param("serverId") String serverId, @Param("rating") int rating);
}

