package com.anradev.plainmessenger.repository;

import com.anradev.plainmessenger.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @author Aleksei Zhvakin
 */
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class UserRepositoryLettuceImplTest {

    private UserRepositoryLettuceImpl repository;
    private StatefulRedisConnection<String, String> connection;
    private StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private RedisCommands<String, String> redisCommands;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IllegalAccessException {
        connection = Mockito.mock(StatefulRedisConnection.class);
        pubSubConnection = Mockito.mock(StatefulRedisPubSubConnection.class);

        repository = new UserRepositoryLettuceImpl(connection, pubSubConnection);

        redisCommands = mock(RedisCommands.class);

        Field field = ReflectionUtils
                .findFields(UserRepositoryLettuceImpl.class, f -> f.getName().equals("syncCommands"),
                        ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .get(0);

        field.setAccessible(true);
        field.set(repository, redisCommands);
    }

    @Test
    void save() throws JsonProcessingException {

        String key = "users";
        User user = new User("username");

        Map<String, String> userBody = new HashMap<>();
        userBody.put("value", objectMapper.writeValueAsString(user));

        when(redisCommands.xadd(key, userBody)).thenReturn("1");

        repository.save(user);

        verify(redisCommands).xadd(key, userBody);
    }

    @Test
    void findAll() throws JsonProcessingException {

        String key = "users";

        User user1 = new User("sender");
        User user2 = new User("recipient");

        Map<String, String> user1Body = new HashMap<>();
        user1Body.put("value", objectMapper.writeValueAsString(user1));

        Map<String, String> user2Body = new HashMap<>();
        user2Body.put("value", objectMapper.writeValueAsString(user2));

        StreamMessage<String, String> sm1 = new StreamMessage<>(key, "0", user1Body);
        StreamMessage<String, String> sm2 = new StreamMessage<>(key, "1", user2Body);

        List<StreamMessage<String, String>> streamUsers = new ArrayList<>();
        streamUsers.add(sm1);
        streamUsers.add(sm2);


        when(redisCommands.xread(any(XReadArgs.StreamOffset.class))).thenReturn(streamUsers);

        List<User> messages = repository.findAll();

        Assertions.assertEquals(messages.size(), 2);
        Assertions.assertEquals(messages.get(0), user1);
        Assertions.assertEquals(messages.get(1), user2);

        verify(redisCommands).xread(any(XReadArgs.StreamOffset.class));
    }
}