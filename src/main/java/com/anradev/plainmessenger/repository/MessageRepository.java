package com.anradev.plainmessenger.repository;

import com.anradev.plainmessenger.model.Message;
import io.lettuce.core.StreamMessage;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Aleksei Zhvakin
 */
public interface MessageRepository {
    List<Message> findAllByKey(String key);
    String save(String key, Message message);
    void subscribe(String sender, String recipient, Consumer<StreamMessage<String, String>> consumer);
}
