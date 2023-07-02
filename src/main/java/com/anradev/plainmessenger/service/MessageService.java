package com.anradev.plainmessenger.service;

import com.anradev.plainmessenger.model.Message;

import java.util.List;

/**
 * @author Aleksei Zhvakin
 */
public interface MessageService {
    List<Message> getAllHistory(String sender, String recipient);
    String sendMessage(Message message);
}
