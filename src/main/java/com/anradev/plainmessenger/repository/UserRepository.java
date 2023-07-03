package com.anradev.plainmessenger.repository;

import com.anradev.plainmessenger.model.User;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Aleksei Zhvakin
 */
public interface UserRepository<T> {
    String save(User user);
    List<User> findAll();
    void subscribe(String user, Consumer<T> consumer);
}
