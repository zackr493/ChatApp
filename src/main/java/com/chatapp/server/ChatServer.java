package com.chatapp.server;

import com.chatapp.model.Client;

public class ChatServer {

    public static void main(String[] args) {
        System.out.println("test working") ;

        // starts a session manager
        //
        Client user = new Client() ;
        user.setName("Alice");
        System.out.println(user.getName());
    }


}
