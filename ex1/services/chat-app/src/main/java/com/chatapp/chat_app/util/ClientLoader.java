package com.chatapp.chat_app.util;

import com.chatapp.chat_app.model.ClientEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ClientLoader {

    // loads clients from a file


    public static List<ClientEntity> loadClients(String filePath) throws IOException {
        return Files.lines(Path.of(filePath)).map(ClientEntity::new).collect(Collectors.toList()) ;

    }
}
