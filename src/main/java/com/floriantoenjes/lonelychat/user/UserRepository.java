package com.floriantoenjes.lonelychat.user;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface UserRepository  extends ReactiveMongoRepository<User, String> {
}
