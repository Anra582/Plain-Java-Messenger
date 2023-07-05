package com.anradev.plainmessenger.util;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * RepoKeyBuilder managed for creating a key from two users by sorting them and concatenate.
 * @author Aleksei Zhvakin
 */
public class RepoKeyBuilder {
    public static String build(String sender, String recipient) {
        return Stream.of(sender, recipient).sorted().collect(Collectors.joining(""));
    }
}
