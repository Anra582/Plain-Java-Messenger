package com.anradev.plainmessenger;

import com.anradev.plainmessenger.config.ConfigLoader;
import com.anradev.plainmessenger.model.Message;
import com.anradev.plainmessenger.repository.MessageRepository;
import com.anradev.plainmessenger.repository.MessageRepositoryLettuceImpl;
import com.anradev.plainmessenger.service.MessageService;
import com.anradev.plainmessenger.service.MessageServiceImpl;
import com.anradev.plainmessenger.service.StoryExportService;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.*;

/**
 * @author Aleksei Zhvakin
 */
public class Main {

    private static MessageRepository messageRepository;
    private static MessageService messageService;
    private static StoryExportService exportService;
    private static String user;

    public static void main(String[] args) throws IOException {

        final String url = ConfigLoader.getValueOfProperty("redis.server");
        final String port = ConfigLoader.getValueOfProperty("redis.port");

        messageRepository = new MessageRepositoryLettuceImpl(url, Integer.parseInt(port));
        messageService = new MessageServiceImpl(messageRepository);
        exportService = new StoryExportService(messageRepository);



        //Start interactive terminal
        Terminal terminal = TerminalBuilder.terminal();
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new StringsCompleter("describe", "create"))
                .build();

        List<String> arguments;

        //"Login" section
        while (true) {
            System.out.println("Write your login name:");
            String loginStr = reader.readLine("> ");

            if (loginStr.matches("^[a-zA-Z0-9._-]{3,}$")) {
                user = loginStr;
                break;
            }
            else {
                System.out.println("Username must be one word, more then 3 symbols." +
                        " It can also contain dots, dashes, and underscores.");
            }
        }

        //Common commands section
        while (true) {
            String line = reader.readLine(user + "> "); //Sometimes it's bugs and not shows on next line. That's Jline issue
            if (line.equals("/exit")) {
                break;
            }
            if (line.startsWith("/story_with")) {
                arguments = Arrays.stream(line.split(" ")).toList();
                if (arguments.size() > 1) {
                    final List<Message> messages = messageService.getAllHistory(user, arguments.get(1));
                    messages.stream()
                            .map(message -> Collections.singletonMap(message.sender(), message.message()))
                            .forEach(System.out::println);
                }
            }
            if (line.startsWith("/text_to")){
                arguments = Arrays.stream(line.split(" ")).toList();
                if (arguments.size() > 2) {
                    Message msg = new Message(user, arguments.get(1), arguments.get(2));
                    messageService.sendMessage(msg);
                }
            }

            reader.getHistory().add(line);
        }
    }
}