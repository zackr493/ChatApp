package com.chatapp.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerEntity {

    private String id;
    private String serverName;
    private String host;

    private int numClientsDay   = 0;
    private int numClientsMonth = 0;
    private int ratingTotal     = 0;
    private int ratingCount     = 0;

    private String currClientId;

    private LocalDateTime lastHeartbeatAt;


}
