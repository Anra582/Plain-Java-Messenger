package com.anradev.plainmessenger.service;

import com.anradev.plainmessenger.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Aleksei Zhvakin
 */
class StoryExportServiceTest {

    @TempDir
    private Path tempPath;
    private String fileName;

    private final String sender = "sender";
    private final String recipient = "recipient";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        fileName = tempPath.toFile().getAbsolutePath().concat(".txt");
    }

    @Test
    void exportJsonToFile() throws IOException {
        MessageService messageService = Mockito.mock(MessageService.class);

        Message msg1 = new Message("sender", "recipient", "average message");
        Message msg2 = new Message("recipient", "sender", "non common message");

        List<Message> messages = List.of(msg1, msg2);

        when(messageService.getAllHistory(anyString(), anyString())).thenReturn(messages);
        StoryExportService storyExportService = new StoryExportService(messageService);

        storyExportService.exportJsonToFile(sender, recipient, fileName);

        Path path = Path.of(fileName);
        BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
        String readLine = reader.readLine();

        List<Message> readMessages = objectMapper.readValue(readLine, objectMapper.getTypeFactory().constructCollectionType(List.class, Message.class));

        Assertions.assertEquals(messages, readMessages);
    }
}