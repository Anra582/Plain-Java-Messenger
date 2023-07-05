package com.anradev.plainmessenger.service;

import com.anradev.plainmessenger.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * StoryExportService provides exporting the chatting history between two users in JSON-format.
 * @author Aleksei Zhvakin
 */
public class StoryExportService {

    private final MessageService messageService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public StoryExportService(MessageService messageService) {
        this.messageService = messageService;
    }

    public boolean exportJsonToFile(String sender, String recipient, String fileName) {
        final List<Message> messages = messageService.getAllHistory(sender, recipient);
        if(messages.isEmpty()) {
            return false;
        }

        try {
            final String valueAsString = objectMapper.writeValueAsString(messages);

            Path path = Path.of(fileName);
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(valueAsString);
                writer.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
