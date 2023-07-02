package com.anradev.plainmessenger;

import com.anradev.plainmessenger.config.RedisConnectionHolder;
import com.anradev.plainmessenger.model.Message;
import com.anradev.plainmessenger.model.User;
import com.anradev.plainmessenger.repository.MessageRepository;
import com.anradev.plainmessenger.repository.MessageRepositoryLettuceImpl;
import com.anradev.plainmessenger.repository.UserRepository;
import com.anradev.plainmessenger.repository.UserRepositoryLettuceImpl;
import com.anradev.plainmessenger.service.*;
import com.anradev.plainmessenger.util.RepoKeyBuilder;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Aleksei Zhvakin
 */
public class Main {

    private static MessageRepository messageRepository;
    private static MessageService messageService;
    private static UserRepository userRepository;
    private static UserService userService;
    private static StoryExportService exportService;
    private static String userName;

    public static void main(String[] args) throws IOException {

        messageRepository = new MessageRepositoryLettuceImpl(RedisConnectionHolder.getConnection(),
                RedisConnectionHolder.getPubSubConnection());
        messageService = new MessageServiceImpl(messageRepository);

        userRepository = new UserRepositoryLettuceImpl(RedisConnectionHolder.getConnection(),
                RedisConnectionHolder.getPubSubConnection());
        userService = new UserServiceImpl(userRepository);

        exportService = new StoryExportService(messageRepository);



        //Start interactive terminal
        Terminal terminal = TerminalBuilder.terminal();
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .appName("Plain Java Messenger")
//                .completer(new StringsCompleter("describe", "create"))
                .build();

        List<String> arguments;

        //"Login" section
        while (true) {
            System.out.println("Write your login name:");
            String loginStr = reader.readLine("> ");

            if (loginStr.matches("^[a-zA-Z0-9._-]{3,}$")) {
                userName = loginStr;

                List<User> users = userService.findAll();
                boolean isAlreadyExist = users.stream()
                        .anyMatch(user -> user.name().equals(userName));

                if(!isAlreadyExist) {
                    userService.save(new User(userName));

                    RedisCommands<String, String> redisSyncCommands = RedisConnectionHolder.getConnection().sync();
                    redisSyncCommands.xgroupCreate(XReadArgs.StreamOffset.from("users", "0-0"), userName);

//                    users.stream() //тут короче выходит так что срабатывает эта вещь только при создании нового
//                            .filter(user -> !user.name().equals(loginStr))
//                            .forEach(otherUser -> {
//                                messageRepository.subscribe(loginStr, otherUser.name(), message -> {
//                                    Message mappedMessage = MessageRepositoryLettuceImpl.mapMessageFromStreamMessage(message);
//                                    System.out.println(mappedMessage.sender() + ": " + mappedMessage.message());
//                                    RedisAsyncCommands<String, String> aSyncCommands = messageRepository.getASyncCommands();
//                                    aSyncCommands.xack(RepoKeyBuilder.build(loginStr, otherUser.name()), loginStr, message.getId());
//                                });
//                            });
                    /*
                    тут короче выходит так что срабатывает эта вещь только при создании нового пользователя
                    а надо сделать так: каждый пользователь привязывает себя как группу и консюмер к стриму пользователей.
                    и исходя из полученного списка он формирует ключ к стримам сообщений с каждым из пользователей(при существовании
                    такого ключа просто игнорируется) и вносит себя в группу и консюмера к этому ключу.
                    Если добавляется новый пользователь, то будучи подписанным на это событие также
                    происходит очередная итерация алгоритма. По сути при регистрации нового пользователя надо просто добавлять его
                    в "таблицу" юзеров, а подписка остальных должна сработать автоматом. Но это не точно. Обдумай это завтра получше
                     */

                }

                userRepository.subscribe(userName, streamMessage -> {
                    User mappedUser = UserRepositoryLettuceImpl.mapUserFromStreamMessage(streamMessage);

                    if(mappedUser != null) {
                        if(!mappedUser.name().equals(userName)) {
                            RedisCommands<String, String> sync = RedisConnectionHolder.getConnection().sync();
                            String key = RepoKeyBuilder.build(userName, mappedUser.name());

                            messageRepository.subscribe(userName, mappedUser.name(), message -> {
                                Message mappedMessage = MessageRepositoryLettuceImpl.mapMessageFromStreamMessage(message);
                                System.out.println(mappedMessage.sender() + ": " + mappedMessage.message());
                                RedisAsyncCommands<String, String> aSyncCommands = messageRepository.getASyncCommands();
                                aSyncCommands.xack(RepoKeyBuilder.build(userName, mappedUser.name()), userName, message.getId());
                            });
                        }
                    }

                });

                break;
            }
            else {
                System.out.println("Username must be one word, more then 3 symbols." +
                        " It can also contain dots, dashes, and underscores.");
            }
        }

        //Subscribe section. Remake it to subscribe on adding new users
//
//        messageRepository.subscribe(userName, "recipient", message -> {
//            Message mappedMessage = MessageRepositoryLettuceImpl.mapMessageFromStreamMessage(message);
//            System.out.println(mappedMessage.sender() + ": " + mappedMessage.message());
//            RedisAsyncCommands<String, String> aSyncCommands = messageRepository.getASyncCommands();
//            aSyncCommands.xack(RepoKeyBuilder.build(userName, "recipient"), userName, message.getId());
//        });


        //Common commands section
        while (true) {
            String line = reader.readLine(userName + "> "); //Sometimes it's bugs and not shows on next line. That's Jline/Terminal issue
            if (line.equals("/exit")) {
                break;
            }
            if (line.startsWith("/story_with")) {
                arguments = Arrays.stream(line.split(" ")).toList();
                if (arguments.size() > 1) {
                    List<Message> messages = messageService.getAllHistory(userName, arguments.get(1));
                    messages.stream()
                            .map(message -> Collections.singletonMap(message.sender(), message.message()))
                            .forEach(System.out::println);
                }
            }
            if (line.startsWith("/text_to")) {
                arguments = Arrays.stream(line.split(" ")).toList();
                String messageText = arguments.stream().skip(2).collect(Collectors.joining(" "));
                if (arguments.size() > 2) {
                    Message msg = new Message(userName, arguments.get(1), messageText);
                    messageService.sendMessage(msg);
                }
            }

            if (line.startsWith("/export")) {
                arguments = Arrays.stream(line.split(" ")).toList();
                if (arguments.size() > 2) {
                    boolean isExported = exportService.exportJsonToFile(userName, arguments.get(1), arguments.get(2));
                    if(isExported) {
                        System.out.println("Chatting history with " + arguments.get(1) + " are exported to " + arguments.get(2));
                    }
                    else {
                        System.out.println("Something went wrong with exporting!");
                    }
                }
            }

            if (line.equals("/users")) {
                List<User> users = userService.findAll();
                users.forEach(System.out::println);
            }

            if(line.equals("/history")) {
                System.out.println(reader.getHistory());
            }

            reader.getHistory().add(line);
        }
    }
}