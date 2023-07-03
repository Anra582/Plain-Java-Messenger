package com.anradev.plainmessenger.service;

import com.anradev.plainmessenger.model.Message;
import com.anradev.plainmessenger.repository.MessageRepository;
import com.anradev.plainmessenger.util.RepoKeyBuilder;
import io.lettuce.core.StreamMessage;

import java.util.List;
import java.util.function.Consumer;

/**
 * MessageServiceImpl is a service-layer wrapper over the {@link com.anradev.plainmessenger.repository.MessageRepository}.
 * It is also responsible for generating the correct key as the id for repository.
 * @see com.anradev.plainmessenger.util.RepoKeyBuilder
 * @author Aleksei Zhvakin
 */
public class MessageServiceImpl implements MessageService<StreamMessage<String, String>> {

    private final MessageRepository<StreamMessage<String, String>> messageRepository;

    public MessageServiceImpl(MessageRepository<StreamMessage<String, String>> messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public List<Message> getAllHistory(String sender, String recipient) {
        String key = RepoKeyBuilder.build(sender, recipient);
        return messageRepository.findAllByKey(key);
    }

    @Override
    public String sendMessage(Message message) {
        String key = RepoKeyBuilder.build(message.sender(), message.recipient());
        return messageRepository.save(key, message);
    }

    @Override
    public void subscribe(String sender, String recipient, Consumer<StreamMessage<String, String>> consumer) {
        messageRepository.subscribe(sender, recipient, consumer);
    }
}
