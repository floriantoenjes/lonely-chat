package com.floriantoenjes.lonelychat.message;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Collection;

public interface MessageRepository extends ReactiveMongoRepository<Message, String> {

    Flux<Message> findAllByReceiverId(String receiverId);

    @Tailable
    Flux<Message> findAllBySenderIdAndSentAtAfterOrReceiverIdAndSentAtAfter(String senderId, LocalDateTime sentAt1, String receiverId, LocalDateTime sentAt2);

    Flux<Message> findAllBySenderId(String senderId);

    Flux<Message> findAllByIdIn(Collection<String> ids);

}
