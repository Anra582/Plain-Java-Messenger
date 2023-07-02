package com.anradev.plainmessenger.service;

import com.anradev.plainmessenger.model.Message;
import com.anradev.plainmessenger.repository.MessageRepository;
import com.anradev.plainmessenger.util.RepoKeyBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Aleksei Zhvakin
 */
public class StoryExportService {

    private final MessageRepository messageRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public StoryExportService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public boolean exportJsonToFile(String sender, String recipient, String fileName) {
        String key = RepoKeyBuilder.build(sender, recipient);
        final List<Message> messages = messageRepository.findAllByKey(key);
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
