package com.chatapp.model;

import java.time.LocalDateTime;

public class Session {

    private Client client ;
    private Server server ;
    private LocalDateTime startTime ;
    private LocalDateTime endTime ;
    private int rating;

    public Session(Client client, Server server) {
        this.client = client ;
        this.server = server ;
        this.startTime = LocalDateTime.now() ;

    }

    public void endSession(int rating) {
        this.endTime = LocalDateTime.now() ;
        this.rating = rating ;

        synchronized (server) {
            server.setNumClientsDay(server.getNumClientsDay() + 1 );
            server.setNumClientsMonth(server.getNumClientsMonth() + 1); ;
            server.setRatingTotal(server.getRatingTotal() + rating) ;
            server.setRatingCount(server.getRatingCount() + 1) ;

            server.setCurrClient(null);

        }

        client.setCurrentServer(null) ;


}
