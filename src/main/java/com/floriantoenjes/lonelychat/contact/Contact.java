package com.floriantoenjes.lonelychat.contact;

import com.floriantoenjes.lonelychat.message.Message;
import com.floriantoenjes.lonelychat.user.User;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document
public class Contact {
    private String id;

    @DBRef
    private User owner;

    @DBRef
    private User target;

    @DBRef
    private List<Message> messages;

    private Message lastMessage;

    private LocalDateTime lastMessageAt;

    public boolean addMessage(Message message) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        return messages.add(message);
    }
}
