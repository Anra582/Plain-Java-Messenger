package com.anradev.plainmessenger.repository;

import com.anradev.plainmessenger.model.User;
import com.anradev.plainmessenger.util.ObjectMapperFromStreamMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * UserRepositoryLettuceImpl provides functionality for saving and reading {@link com.anradev.plainmessenger.model.User}
 * from Redis via <a href="https://lettuce.io/">Lettuce</a>. It also allows you to subscribe to an event about adding new Users to Redis stream.
 * @author Aleksei Zhvakin
 */
public class UserRepositoryLettuceImpl implements UserRepository<StreamMessage<String, String>> {

    private final String streamKey = "users";
    private final StatefulRedisConnection<String, String> connection;
    private final StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private final RedisCommands<String, String> syncCommands;
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
            System.out.println("Cannot serialize value to string");
            return null;
        }
        Map<String, String> userBody = new HashMap<>();
        userBody.put("value", userAsString);

        return syncCommands.xadd(streamKey, userBody);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<User> findAll() {
        List<StreamMessage<String, String>> streamMessages = syncCommands.xread(XReadArgs.StreamOffset.from(streamKey, "0"));
        return streamMessages.stream()
                .map(streamMessage -> ObjectMapperFromStreamMessage.map(streamMessage, "value",User.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void subscribe(String user, Consumer<StreamMessage<String, String>> consumer) {

        pubSubConnection.reactive()
                .xreadgroup(io.lettuce.core.Consumer.from(user, user),
                        XReadArgs.StreamOffset.lastConsumed(streamKey))
                .repeat()
                .subscribe(consumer);
    }
}
