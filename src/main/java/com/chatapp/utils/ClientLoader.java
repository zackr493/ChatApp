package com.chatapp.utils;

import com.chatapp.model.Client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ClientLoader {

    // loads clients from a file


    public static List<Client> loadClients(String filePath) throws IOException {
        return Files.lines(Path.of(filePath)).map(Client::new).collect(Collectors.toList()) ;

    }
}
