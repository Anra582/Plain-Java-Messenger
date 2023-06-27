package com.anradev.plainmessenger.repository;

import com.anradev.plainmessenger.model.Message;

import java.util.List;

/**
 * @author Aleksei Zhvakin
 */
public interface MessageRepository {
    List<Message> findAllByKey(String key);
    boolean save(String key, Message message);
}
