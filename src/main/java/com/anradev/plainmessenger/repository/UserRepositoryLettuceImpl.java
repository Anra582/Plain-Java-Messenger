package com.anradev.plainmessenger.repository;

import com.anradev.plainmessenger.model.Message;
import com.anradev.plainmessenger.model.User;
import com.anradev.plainmessenger.util.RepoKeyBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Aleksei Zhvakin
 */
public class UserRepositoryLettuceImpl implements UserRepository {

    StatefulRedisConnection<String, String> connection;
    StatefulRedisPubSubConnection<String, String> pubSubConnection;
    RedisCommands<String, String> syncCommands;
    static ObjectMapper objectMapper = new ObjectMapper();

    public UserRepositoryLettuceImpl(StatefulRedisConnection<String, String> connection,
                                     StatefulRedisPubSubConnection<String, String> pubSubConnection) {
        this.connection = connection;
        this.pubSubConnection = pubSubConnection;
        this.syncCommands = connection.sync();
    }

    @Override
    public String save(User user) {
        String userAsString;
        try {
            userAsString = objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            System.out.println("Cannot serialize message to string");
            return null;
        }
        Map<String, String> userBody = new HashMap<>();
        userBody.put("value", userAsString);

        return syncCommands.xadd("users", userBody);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<User> findAll() {
        List<StreamMessage<String, String>> streamMessages = syncCommands.xread(XReadArgs.StreamOffset.from("users", "0"));
        return streamMessages.stream()
                .map(UserRepositoryLettuceImpl::mapUserFromStreamMessage)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void subscribe(String user, Consumer<StreamMessage<String, String>> consumer) {

        pubSubConnection.reactive()
                .xreadgroup(io.lettuce.core.Consumer.from(user, user),
                        XReadArgs.StreamOffset.lastConsumed("users"))
                .repeat()
                .subscribe(consumer);
    }

    public static User mapUserFromStreamMessage(StreamMessage<String, String> streamMessage) { //Move to util class
        Map<String, String> body = streamMessage.getBody();
        String value = body.get("value");
        if (value != null) {
            try {
                return objectMapper.readValue(value, User.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
