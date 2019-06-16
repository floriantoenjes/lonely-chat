package com.floriantoenjes.lonelychat.contact;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ContactRepository extends ReactiveMongoRepository<Contact, String> {

    Mono<Contact> findByOwnerIdAndTargetId(String ownerId, String targetId);

}
