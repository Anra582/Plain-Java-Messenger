package com.anradev.plainmessenger.repository;

import com.anradev.plainmessenger.model.Message;
import com.anradev.plainmessenger.util.RepoKeyBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Aleksei Zhvakin
 */
public class MessageRepositoryLettuceImpl implements MessageRepository {

    private final StatefulRedisConnection<String, String> connection;
    private final StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private final RedisCommands<String, String> syncCommands;
    static ObjectMapper objectMapper = new ObjectMapper();

    public MessageRepositoryLettuceImpl(StatefulRedisConnection<String, String> connection,
                                        StatefulRedisPubSubConnection<String, String> pubSubConnection) {
        this.connection = connection;
        this.pubSubConnection = pubSubConnection;
        this.syncCommands = connection.sync();
    };

    public RedisAsyncCommands<String, String> getASyncCommands() {
        return connection.async();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Message> findAllByKey(String key) {
        List<StreamMessage<String, String>> streamMessages = syncCommands.xread(XReadArgs.StreamOffset.from(key, "0"));
        return streamMessages.stream()
                .map(MessageRepositoryLettuceImpl::mapMessageFromStreamMessage)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public String save(String key, Message message) {
        String msgAsString;
        try {
            msgAsString = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            System.out.println("Cannot serialize message to string");
            return null;
        }
        Map<String, String> messageBody = new HashMap<>();
        messageBody.put("value", msgAsString);

        return syncCommands.xadd(key, messageBody);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void subscribe(String sender, String recipient, Consumer<StreamMessage<String, String>> consumer) {
        String key = RepoKeyBuilder.build(sender, recipient);
        try {
            syncCommands.xgroupCreate(XReadArgs.StreamOffset.from(key, "0-0"), sender, XGroupCreateArgs.Builder.mkstream()); //group and stream are same
        } catch (Exception e) {
            //No needs to do something
        }

        pubSubConnection.reactive()
                .xreadgroup(io.lettuce.core.Consumer.from(sender, sender),
                        XReadArgs.StreamOffset.lastConsumed(key))
                .repeat()
                .subscribe(consumer);
    }

    public static Message mapMessageFromStreamMessage(StreamMessage<String, String> streamMessage) {
        Map<String, String> body = streamMessage.getBody();
        String value = body.get("value");
        if (value != null) {
            try {
                return objectMapper.readValue(value, Message.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
