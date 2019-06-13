package com.floriantoenjes.lonelychat.message;

import com.floriantoenjes.lonelychat.user.User;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document
public class Message {
    String id;

    LocalDateTime sentAt;

    @DBRef
    User sender;

    @DBRef
    User receiver;
}
