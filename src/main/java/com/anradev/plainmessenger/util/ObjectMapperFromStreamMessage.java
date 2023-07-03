package com.anradev.plainmessenger.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.StreamMessage;

import java.io.Serializable;
import java.util.Map;

/**
 * ObjectMapperFromStreamMessage allows you to map {@link io.lettuce.core.StreamMessage} into needed class object.
 * @author Aleksei Zhvakin
 */
public class ObjectMapperFromStreamMessage<T extends Serializable> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T map(StreamMessage<String, String> streamMessage, String fieldName, Class<T> clazz) {
        Map<String, String> body = streamMessage.getBody();
        String value = body.get(fieldName);
        if (value != null) {
            try {
                return objectMapper.readValue(value, clazz);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
