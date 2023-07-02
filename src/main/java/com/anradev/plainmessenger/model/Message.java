package com.anradev.plainmessenger.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDateTime;

/**
 * @author Aleksei Zhvakin
 */
public record Message(
        String sender,
        String recipient,
        String message
) {

}
