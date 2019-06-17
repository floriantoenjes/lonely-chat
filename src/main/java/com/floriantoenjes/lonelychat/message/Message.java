package com.floriantoenjes.lonelychat.message;

import com.floriantoenjes.lonelychat.user.User;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document
public class Message {

    private String id;

    private LocalDateTime sentAt;

    private String message;

    private boolean heartbeat;

    @DBRef
    private User sender;

    @DBRef
    private User receiver;
}
