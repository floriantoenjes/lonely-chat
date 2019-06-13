package com.floriantoenjes.lonelychat.message;

import com.floriantoenjes.lonelychat.user.User;
import com.floriantoenjes.lonelychat.user.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.floriantoenjes.lonelychat.utils.AuthUtils.getUsernameFromAuth;

@RestController
public class MessageController {

    private MessageRepository messageRepository;

    private UserRepository userRepository;

    public MessageController(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/received")
    public Flux<Message> getReceivedMessages() {
        Mono<String> username = getUsernameFromAuth();
        return findOrCreateUser(username)
                .flatMapMany(user -> messageRepository.findAllByReceiverId(user.getId()));
    }

    @GetMapping("/sent")
    public Flux<Message> getSentMessages() {
        Mono<String> username = getUsernameFromAuth();
        return findOrCreateUser(username)
                .flatMapMany(user -> messageRepository.findAllBySenderId(user.getId()));
    }

    private Mono<User> findOrCreateUser(Mono<String> username) {
        return username.flatMap(name -> userRepository.findByUsername(name)).flatMap(user -> {
            if (user == null) {
                User newUser = new User("test");
                return userRepository.save(newUser);
            } else {
                return Mono.just(user);
            }
        });
    }

}
