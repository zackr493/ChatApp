package com.chatapp.chat_app.dto;

import lombok.Data;



@Data
public class Server {

    private String id;

    private int numClientsDay = 0;

    private int numClientsMonth = 0;

    private int ratingTotal = 0;

    private int ratingCount = 0;

    private Client currClient ;

    public Server (String id) {
        this.id = id ;
    }

    public double getAverageRating() {
        if (ratingCount == 0) {
            return 0;
        }

        return ratingTotal / ratingCount ;
    }

}
