package com.floriantoenjes.lonelychat.utils;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

public class AuthUtils {

    public static Mono<String> getUsernameFromAuth() {
        return ReactiveSecurityContextHolder.getContext().map(context -> context.getAuthentication().getName());
    }

}
