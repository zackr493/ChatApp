package com.chatapp.model;

import lombok.Data;


import java.time.LocalDateTime;

@Data
public class Server {

    private String name;

    private int numClientsDay = 0;

    private int numClientsMonth = 0;

    private int ratingTotal = 0;

    private int ratingCount = 0;

    private Client currClient ;

    public double getAverageRating() {
        if (ratingCount == 0) {
            return 0;
        }

        return ratingTotal / ratingCount ;
    }

}
