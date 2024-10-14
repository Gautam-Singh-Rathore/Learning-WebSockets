package com.gautam.ChatApplication.chat;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {

    private String content;
    private String Sender ;
    private MessageType type;


    public ChatMessage(String username, MessageType messageType) {
        Sender = username;
        type = messageType;
    }

    public Object getSender() {
        return Sender;
    }
}
