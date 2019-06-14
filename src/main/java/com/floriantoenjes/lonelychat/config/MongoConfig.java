package com.floriantoenjes.lonelychat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoOperations;

@Configuration
public class MongoConfig {

    public MongoConfig(MongoOperations mongoOperations) {
        if (!mongoOperations.collectionExists("message")) {
            mongoOperations.createCollection("message", CollectionOptions.empty().capped().size(1024));
        }
    }

}
