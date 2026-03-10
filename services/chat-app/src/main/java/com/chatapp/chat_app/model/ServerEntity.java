package com.chatapp.chat_app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;



@Data
@Entity
@Table(name = "servers")
public class ServerEntity {

    @Id
    private String id;

    private int numClientsDay = 0;

    private int numClientsMonth = 0;

    private int ratingTotal = 0;

    private int ratingCount = 0;

    private String currClientEntity;

    public ServerEntity(String id) {
        this.id = id ;
    }

    public double getAverageRating() {
        if (ratingCount == 0) {
            return 0;
        }

        return (double) ratingTotal / ratingCount ;
    }

}
