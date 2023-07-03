package com.anradev.plainmessenger.repository;

import com.anradev.plainmessenger.model.Message;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Aleksei Zhvakin
 */
public interface MessageRepository<T> {
    List<Message> findAllByKey(String key);
    String save(String key, Message message);
    void subscribe(String sender, String recipient, Consumer<T> consumer);
}
