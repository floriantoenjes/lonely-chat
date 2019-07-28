package com.floriantoenjes.lonelychat.contact;

import com.floriantoenjes.lonelychat.user.User;
import com.floriantoenjes.lonelychat.user.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.floriantoenjes.lonelychat.utils.AuthUtils.getUsernameFromAuth;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    private ContactRepository contactRepository;

    private UserRepository userRepository;

    public ContactController(ContactRepository contactRepository, UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    @GetMapping()
    public Flux<Contact> getContacts() {
        Mono<String> owner = getUsernameFromAuth();
        return findOrCreateUser(owner).flatMapMany(contact -> contactRepository.findAllByOwnerId(contact.getId()));
    }

    // TODO: Extract from here and MessageController to keep code dry
    private Mono<User> findOrCreateUser(Mono<String> username) {
        return username.flatMap(name -> userRepository.findByUsername(name)).switchIfEmpty(createUser(username));
    }

    private Mono<User> createUser(Mono<String> username) {
        return username.flatMap(name -> userRepository.save(new User(name)));
    }
}
