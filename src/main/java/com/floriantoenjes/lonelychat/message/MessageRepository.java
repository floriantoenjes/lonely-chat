package com.floriantoenjes.lonelychat.message;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;

public interface MessageRepository extends ReactiveMongoRepository<Message, String> {

    Flux<Message> findAllByReceiverId(String receiverId);

    @Tailable
    Flux<Message> findAllBySenderIdOrReceiverId(String senderId, String receiverId);

    Flux<Message> findAllBySenderId(String senderId);

}
