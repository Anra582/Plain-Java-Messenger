package com.anradev.plainmessenger.service;

import com.anradev.plainmessenger.model.User;
import com.anradev.plainmessenger.repository.UserRepository;

import java.util.List;
import java.util.Set;

/**
 * @author Aleksei Zhvakin
 */
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }
}
