package com.chatapp.model;

import lombok.Data;


import java.time.LocalDateTime;

@Data
public class Server {

    private String name;

    private LocalDateTime created_at;

    private LocalDateTime expired_at;

    private int num_clients_day ;

    private int num_clients_month;

    private int average_rating;

    private int num_clients_approached ;

    private int num_clients_lost;

    private String curr_client ;

}
