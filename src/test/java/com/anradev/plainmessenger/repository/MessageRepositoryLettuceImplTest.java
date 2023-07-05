package com.anradev.plainmessenger.repository;

import com.anradev.plainmessenger.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.junit.jupiter.api.AfterEach;
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
class MessageRepositoryLettuceImplTest {

    private MessageRepositoryLettuceImpl repository;
    private StatefulRedisConnection<String, String> connection;
    private StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private RedisCommands<String, String> redisCommands;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IllegalAccessException {
        connection = Mockito.mock(StatefulRedisConnection.class);
        pubSubConnection = Mockito.mock(StatefulRedisPubSubConnection.class);

        repository = new MessageRepositoryLettuceImpl(connection, pubSubConnection);

        redisCommands = mock(RedisCommands.class);

        Field field = ReflectionUtils
                .findFields(MessageRepositoryLettuceImpl.class, f -> f.getName().equals("syncCommands"),
                        ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .get(0);

        field.setAccessible(true);
        field.set(repository, redisCommands);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void findAllByKey() throws Exception {

        String key = "key";

        Message msg1 = new Message("sender", "recipient", "average message");
        Message msg2 = new Message("recipient", "sender", "non common message");

        Map<String, String> msg1Body = new HashMap<>();
        msg1Body.put("value", objectMapper.writeValueAsString(msg1));

        Map<String, String> msg2Body = new HashMap<>();
        msg2Body.put("value", objectMapper.writeValueAsString(msg2));

        StreamMessage<String, String> sm1 = new StreamMessage<>(key, "0", msg1Body);
        StreamMessage<String, String> sm2 = new StreamMessage<>(key, "1", msg2Body);

        List<StreamMessage<String, String>> streamMessages = new ArrayList<>();
        streamMessages.add(sm1);
        streamMessages.add(sm2);


        when(redisCommands.xread(any(XReadArgs.StreamOffset.class))).thenReturn(streamMessages);

        List<Message> messages = repository.findAllByKey(key);

        Assertions.assertEquals(messages.size(), 2);
        Assertions.assertEquals(messages.get(0), msg1);
        Assertions.assertEquals(messages.get(1), msg2);

        verify(redisCommands).xread(any(XReadArgs.StreamOffset.class));

    }

    @Test
    void save() throws JsonProcessingException {

        String key = "key";
        Message message = new Message("sender", "recipient", "another almost empty message");

        Map<String, String> msgBody = new HashMap<>();
        msgBody.put("value", objectMapper.writeValueAsString(message));

        when(redisCommands.xadd(key, msgBody)).thenReturn("1");

        repository.save(key, message);

        verify(redisCommands).xadd(key, msgBody);
    }

}