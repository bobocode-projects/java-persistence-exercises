package com.bobocode.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "persons")
@Setter
@Getter
public class Person {
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "person_id_seq_generator")
    @SequenceGenerator(name = "person_id_seq_generator", sequenceName = "persons_id_seq", allocationSize = 1)
    private Integer id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TeamType team;

    @CreationTimestamp
    private LocalDateTime createdOn;

    @OneToMany(mappedBy = "person", cascade = ALL)
    private List<Note> notes = new ArrayList<>();

    public enum TeamType {
        Petros, Hoverla, Blyznytsia, Breskul, Svydovets
    }


    public void addNote(Note note) {
        note.setPerson(this);
        notes.add(note);
    }
}
