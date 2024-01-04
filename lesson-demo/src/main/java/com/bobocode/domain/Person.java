package com.bobocode.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "persons")
@ToString
@Setter
@Getter
public class Person {
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "person_seq_generator")
    @SequenceGenerator(name = "person_seq_generator", sequenceName = "persons_id_seq", allocationSize = 1)
    private Integer id;

    private String firstName;

    private String lastName;

    @Enumerated(STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TeamType team;


    @CreationTimestamp
    private LocalDateTime createdOn;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL)
    private List<Note> notes = new ArrayList<>();

    public enum TeamType {
        Petros, Hoverla, Blyznytsia, Breskul, Svydovets
    }
}
