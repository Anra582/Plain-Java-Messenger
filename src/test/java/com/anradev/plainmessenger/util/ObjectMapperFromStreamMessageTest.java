package com.anradev.plainmessenger.util;

import com.anradev.plainmessenger.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.StreamMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

/**
 * @author Aleksei Zhvakin
 */
class ObjectMapperFromStreamMessageTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void map() throws JsonProcessingException {
        Message message = new Message("sender", "recipient", "boring words");

        Map<String, String> map = Collections.singletonMap("value", objectMapper.writeValueAsString(message));
        StreamMessage<String, String> streamMessage = new StreamMessage<>("stream", "id", map);

        Message mappedMessage = ObjectMapperFromStreamMessage.map(streamMessage, "value", Message.class);

        Assertions.assertEquals(mappedMessage, mappedMessage);
    }
}