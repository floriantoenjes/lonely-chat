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
import java.util.stream.Collectors;

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

    @GetMapping("/messages/{contactName}")
    public Flux<Message> getMessagesFromContact(@PathVariable String targetName) {
        Mono<String> owner = getUsernameFromAuth();
        return findOrCreateUser(owner)
                .zipWith(findOrCreateUser(Mono.just(targetName)))
                .flatMap(ownerAndTarget ->
                        contactRepository.findByOwnerIdAndTargetId(ownerAndTarget.getT1().getId(),
                                ownerAndTarget.getT2().getId()))
                .map(contact -> contact.getMessages().stream().map(Message::getId).collect(Collectors.toList()))
                .flatMapMany(msgIds -> messageRepository.findAllByIdIn(msgIds));
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
                            .zipWith(findOrCreateContact(sender, receiver))
                            .doOnNext(messageAndSenderContact -> {
                                Contact senderContact = messageAndSenderContact.getT2();
                                senderContact.addMessage(messageAndSenderContact.getT1());
                                contactRepository.save(senderContact).subscribe();
                            })
                            .flatMap(messageAndSenderContact -> {
                                Message msg = messageAndSenderContact.getT1();
                                return Mono.just(msg).zipWith(findOrCreateContact(receiver, sender));
                            })
                            .doOnNext(messageAndReceiverContact -> {
                                Contact receiverContact = messageAndReceiverContact.getT2();
                                receiverContact.addMessage(messageAndReceiverContact.getT1());
                                contactRepository.save(receiverContact).subscribe();
                            }).map(Tuple2::getT1);
                });
    }

    @GetMapping(value = "/stream-sse", produces = "text/event-stream")
    public Flux<Message> streamEvents() {
        Mono<String> username = getUsernameFromAuth();
        return findOrCreateUser(username)
                .flatMapMany(user -> messageRepository
                        .findAllBySenderIdOrReceiverIdAndSentAtAfter(user.getId(), user.getId(), LocalDateTime.now()));
    }

    private Mono<Contact> findOrCreateContact(User owner, User target) {
        return contactRepository.findByOwnerIdAndTargetId(owner.getId(), target.getId())
                .switchIfEmpty(createContact(owner, target));
    }

    private Mono<Contact> createContact(User owner, User target) {
        Contact contact = new Contact();
        contact.setOwner(owner);
        contact.setTarget(target);

        return contactRepository.save(contact);
    }

    private Mono<User> findOrCreateUser(Mono<String> username) {
        return username.flatMap(name -> userRepository.findByUsername(name)).switchIfEmpty(createUser(username));
    }

    private Mono<User> createUser(Mono<String> username) {
        return username.flatMap(name -> userRepository.save(new User(name)));
    }

}
