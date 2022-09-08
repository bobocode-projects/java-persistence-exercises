package com.bobocode.service;

import com.bobocode.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;

    @EventListener(classes = ContextRefreshedEvent.class)
    public void printPerson() {
        var person = personRepository.findById(17L).orElseThrow();
        System.out.println(person.getFirstName() + " " + person.getLastName());
    }
}
