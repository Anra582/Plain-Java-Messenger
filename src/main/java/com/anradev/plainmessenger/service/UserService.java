package com.anradev.plainmessenger.service;

import com.anradev.plainmessenger.model.User;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Aleksei Zhvakin
 */
public interface UserService<T> {
    String save(User user);
    List<User> findAll();
    void subscribe(String user, Consumer<T> consumer);
}
