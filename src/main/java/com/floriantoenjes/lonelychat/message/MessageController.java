package com.floriantoenjes.lonelychat.message;

import com.floriantoenjes.lonelychat.user.User;
import com.floriantoenjes.lonelychat.user.UserRepository;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping("/send/{receiver}")
    public Mono<Message> sendMessage(@PathVariable String receiver, @RequestBody Message message) {
        Mono<String> username = getUsernameFromAuth();
        return findOrCreateUser(username)
                .zipWith(userRepository.findByUsername(receiver))
                .flatMap((senderAndReceiver) -> {
                    message.setSender(senderAndReceiver.getT1());
                    message.setReceiver(senderAndReceiver.getT2());

                    return messageRepository.save(message);
                });
    }

    @GetMapping(value = "/stream-sse", produces = "text/event-stream")
    public Flux<Message> streamEvents() {

        Mono<String> username = getUsernameFromAuth();
        return findOrCreateUser(username)
                .flatMapMany(user -> {
                    System.out.println("TEST");
                    return messageRepository.findAllByReceiverId(user.getId());
                });
    }

    private Mono<User> findOrCreateUser(Mono<String> username) {
        return username.flatMap(name -> userRepository.findByUsername(name)).switchIfEmpty(createUser(username));
    }

    private Mono<User> createUser(Mono<String> username) {
        return username.flatMap(name -> userRepository.save(new User(name)));
    }

}
