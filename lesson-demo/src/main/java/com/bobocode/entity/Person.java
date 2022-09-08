package com.bobocode.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "persons")
@Data
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.PRIVATE)
    @OneToMany(mappedBy = "person", cascade = CascadeType.PERSIST)
    private List<Note> notes = new ArrayList<>();

    public void addNote(Note note) {
        note.setPerson(this);
        notes.add(note);
    }
    
}
