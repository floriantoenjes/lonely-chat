package com.floriantoenjes.lonelychat.message;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public interface MessageRepository extends ReactiveMongoRepository<Message, String> {

    Flux<Message> findAllByReceiverId(String receiverId);

    @Tailable
    Flux<Message> findAllBySenderIdOrReceiverIdAndSentAtAfter(String senderId, String receiverId, LocalDateTime sentAt);

    Flux<Message> findAllBySenderId(String senderId);

}
