package com.anradev.plainmessenger.service;

import com.anradev.plainmessenger.model.User;
import com.anradev.plainmessenger.repository.UserRepository;
import io.lettuce.core.StreamMessage;

import java.util.List;
import java.util.function.Consumer;

/**
 * UserServiceImpl is a service-layer wrapper over the {@link com.anradev.plainmessenger.repository.UserRepository}.
 * @see com.anradev.plainmessenger.util.RepoKeyBuilder
 * @author Aleksei Zhvakin
 */
public class UserServiceImpl implements UserService<StreamMessage<String, String>> {

    private final UserRepository<StreamMessage<String, String>> userRepository;

    public UserServiceImpl(UserRepository<StreamMessage<String, String>> userRepository) {
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

    @Override
    public void subscribe(String user, Consumer<StreamMessage<String, String>> consumer) {
        userRepository.subscribe(user, consumer);
    }
}
