package com.anradev.plainmessenger.service;

import com.anradev.plainmessenger.model.User;

import java.util.List;
import java.util.Set;

/**
 * @author Aleksei Zhvakin
 */
public interface UserService {
    String save(User user);
    List<User> findAll();
}
