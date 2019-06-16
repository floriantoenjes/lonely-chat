package com.floriantoenjes.lonelychat.message;

import com.floriantoenjes.lonelychat.contact.Contact;
import com.floriantoenjes.lonelychat.contact.ContactRepository;
import com.floriantoenjes.lonelychat.user.User;
import com.floriantoenjes.lonelychat.user.UserRepository;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;

import static com.floriantoenjes.lonelychat.utils.AuthUtils.getUsernameFromAuth;

@RestController
public class MessageController {

    private ContactRepository contactRepository;

    private MessageRepository messageRepository;

    private UserRepository userRepository;

    public MessageController(ContactRepository contactRepository, MessageRepository messageRepository, UserRepository userRepository) {
        this.contactRepository = contactRepository;
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

    @PostMapping("/send/{receiverName}")
    public Mono<Message> sendMessage(@PathVariable String receiverName, @RequestBody Message message) {
        Mono<String> senderName = getUsernameFromAuth();
        return findOrCreateUser(senderName)
                .zipWith(userRepository.findByUsername(receiverName))
                .flatMap(senderAndReceiver -> {
                    User sender = senderAndReceiver.getT1();
                    User receiver = senderAndReceiver.getT2();

                    message.setSender(sender);
                    message.setReceiver(receiver);
                    message.setSentAt(LocalDateTime.now());

                    return messageRepository.save(message)
                            .zipWith(findOrCreateContact(sender))
                            .doOnNext(messageAndSenderContact -> {
                                Contact senderContact = messageAndSenderContact.getT2();
                                senderContact.addMessage(messageAndSenderContact.getT1());
                                contactRepository.save(senderContact);
                            })
                            .flatMap(messageAndSenderContact -> {
                                Message msg = messageAndSenderContact.getT1();
                                return Mono.just(msg).zipWith(findOrCreateContact(receiver));
                            })
                            .doOnNext(messageAndReceiverContact -> {
                                Contact receiverContact = messageAndReceiverContact.getT2();
                                receiverContact.addMessage(messageAndReceiverContact.getT1());
                                contactRepository.save(receiverContact);
                            }).map(Tuple2::getT1);

//                    return findOrCreateContact(sender)
//                            .doOnNext(senderContact -> {
//                                senderContact.addMessage(message);
//                                contactRepository.save(senderContact);
//                            })
//                            .flatMap(senderContact -> findOrCreateContact(receiver))
//                            .doOnNext(receiverContact -> {
//                                receiverContact.addMessage(message);
//                                contactRepository.save(receiverContact);
//                            })
//                            .flatMap(receiverContact -> messageRepository.save(message));
                });
    }

    private Mono<Contact> findOrCreateContact(User user) {
        return contactRepository.findByOwnerId(user.getId()).switchIfEmpty(createContact(user));
    }

    private Mono<Contact> createContact(User user) {
        Contact contact = new Contact();
        contact.setOwner(user);

        return contactRepository.save(contact);
    }

    @GetMapping(value = "/stream-sse", produces = "text/event-stream")
    public Flux<Message> streamEvents() {
        Mono<String> username = getUsernameFromAuth();
        return findOrCreateUser(username)
                .flatMapMany(user -> messageRepository
                        .findAllBySenderIdOrReceiverIdAndSentAtAfter(user.getId(), user.getId(), LocalDateTime.now()));
    }

    private Mono<User> findOrCreateUser(Mono<String> username) {
        return username.flatMap(name -> userRepository.findByUsername(name)).switchIfEmpty(createUser(username));
    }

    private Mono<User> createUser(Mono<String> username) {
        return username.flatMap(name -> userRepository.save(new User(name)));
    }

}
