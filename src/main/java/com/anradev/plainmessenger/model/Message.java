package com.anradev.plainmessenger.model;

/**
 * @author Aleksei Zhvakin
 */
public record Message(
        String sender,
        String recipient,
        String value
) {

}
