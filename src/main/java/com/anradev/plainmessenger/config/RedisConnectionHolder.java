package com.anradev.plainmessenger.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

/**
 * @author Aleksei Zhvakin
 */
public class RedisConnectionHolder {

    private static RedisClient redisClient;
    private static StatefulRedisConnection<String, String> connection;
    private static StatefulRedisPubSubConnection<String, String> pubSubConnection;

    private RedisConnectionHolder() {
    }

    public static StatefulRedisConnection<String, String> getConnection() {
        if (redisClient == null) {
            createRedisClient();
        }

        if (connection == null) {
            connection = redisClient.connect();
        }
        return connection;
    }

    public static StatefulRedisPubSubConnection<String, String> getPubSubConnection() {
        if (redisClient == null) {
            createRedisClient();
        }

        if (pubSubConnection == null) {
            pubSubConnection = redisClient.connectPubSub();
        }
        return pubSubConnection;
    }

    private static void createRedisClient() {
        String url = ConfigLoader.getValueOfProperty("redis.server");
        String port = ConfigLoader.getValueOfProperty("redis.port");

        redisClient = RedisClient.create(RedisURI.Builder.redis(url, Integer.parseInt(port)).build());
    }

}
