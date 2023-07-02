package com.anradev.plainmessenger.repository;

import com.anradev.plainmessenger.config.RedisConnectionHolder;
import com.anradev.plainmessenger.model.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Aleksei Zhvakin
 */
class MessageRepositoryLettuceImplTest {

    MessageRepository repository = new MessageRepositoryLettuceImpl(RedisConnectionHolder.getConnection(),
            RedisConnectionHolder.getPubSubConnection());

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void findAllByKey() {
        final List<Message> messages = repository.findAllByKey("recipientsender");
        System.out.println(messages);
    }

    @Test
    void save() {
        Assertions.assertNotNull(repository.save("recipientsender", new Message("sender", "recipient", "average message")));
    }
}