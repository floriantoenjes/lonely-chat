package com.floriantoenjes.lonelychat.user;

import com.floriantoenjes.lonelychat.contact.Contact;
import com.floriantoenjes.lonelychat.message.Message;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    private String username;

    @DBRef
    private List<Message> received;

    @DBRef
    private List<Message> sent;

    @DBRef
    private List<Contact> contacts;

    @DBRef
    private List<Contact> blockedContacts;
}
