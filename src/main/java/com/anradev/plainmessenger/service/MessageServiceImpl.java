package com.anradev.plainmessenger.service;

import com.anradev.plainmessenger.model.Message;
import com.anradev.plainmessenger.repository.MessageRepository;
import com.anradev.plainmessenger.repository.MessageRepositoryLettuceImpl;
import com.anradev.plainmessenger.util.RepoKeyBuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Aleksei Zhvakin
 */
public class MessageServiceImpl implements MessageService {

    private MessageRepository messageRepository;

    public MessageServiceImpl(MessageRepository messageRepository) {
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
}
