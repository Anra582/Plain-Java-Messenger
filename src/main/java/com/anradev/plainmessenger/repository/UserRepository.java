package com.anradev.plainmessenger.repository;

import com.anradev.plainmessenger.model.User;
import io.lettuce.core.StreamMessage;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Aleksei Zhvakin
 */
public interface UserRepository {
    String save(User user);
    List<User> findAll();
    void subscribe(String user, Consumer<StreamMessage<String, String>> consumer);
}
