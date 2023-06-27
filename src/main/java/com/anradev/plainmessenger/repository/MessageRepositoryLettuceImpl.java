package com.anradev.plainmessenger.repository;

import com.anradev.plainmessenger.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Aleksei Zhvakin
 */
public class MessageRepositoryLettuceImpl implements MessageRepository {

    RedisClient redisClient;
    StatefulRedisConnection<String, String> connection;
    RedisCommands<String, String> syncCommands;
    ObjectMapper objectMapper = new ObjectMapper();

    public MessageRepositoryLettuceImpl() {

    }

    public MessageRepositoryLettuceImpl(String url, int port) {
        this.redisClient = RedisClient.create(RedisURI.Builder.redis(url, port).build());
        this.connection = redisClient.connect();
        this.syncCommands = connection.sync();
    };

    @SuppressWarnings("unchecked")
    @Override
    public List<Message> findAllByKey(String key) {
        List<StreamMessage<String, String>> streamMessages = syncCommands.xread(XReadArgs.StreamOffset.from(key, "0"));
        return streamMessages.stream()
                .map(StreamMessage::getBody)
                .map(map -> map.get("value"))
                .map(s -> {
                    try {
                        return objectMapper.readValue(s, Message.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return new Message(null, null, null);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean save(String key, Message message) {
        String msgAsString;
        try {
            msgAsString = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            System.out.println("Cannot serialize message to string");
            return false;
        }
        Map<String, String> messageBody = new HashMap<>();
        messageBody.put("value", msgAsString);

        String messageId = syncCommands.xadd(key, messageBody);

        return true;
    }
}
