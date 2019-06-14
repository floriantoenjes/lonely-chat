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
    private String id;

    public User(String username) {
        this.username = username;
    }

//    @Indexed(unique = true)
    private String username;

    @DBRef
    private List<Message> received;

    @DBRef
    private List<Message> sent;

    private List<User> allowedContacts;

    private List<User> blockedContacts;
}
