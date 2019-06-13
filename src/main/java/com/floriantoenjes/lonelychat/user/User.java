package com.floriantoenjes.lonelychat.user;

import com.floriantoenjes.lonelychat.message.Message;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
@NoArgsConstructor
public class User {
    String id;

    public User(String username) {
        this.username = username;
    }

    @Indexed(unique = true)
    String username;

    @DBRef
    List<Message> received;

    @DBRef
    List<Message> sent;

    List<User> allowedContacts;

    List<User> blockedContacts;
}
