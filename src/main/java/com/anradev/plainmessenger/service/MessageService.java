package com.anradev.plainmessenger.service;

import com.anradev.plainmessenger.model.Message;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Aleksei Zhvakin
 */
public interface MessageService<T> {
    List<Message> getAllHistory(String sender, String recipient);
    String sendMessage(Message message);
    void subscribe(String sender, String recipient, Consumer<T> consumer);
}
