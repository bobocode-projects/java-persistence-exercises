package com.bobocode.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JavaType;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "notes")
@ToString
@Setter
@Getter
public class Note {
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "notes_seq_generator")
    @SequenceGenerator(name = "notes_seq_generator", sequenceName = "notes_id_seq", allocationSize = 1)
    private Integer id;

    private String title;

    private String body;

    @CreationTimestamp
    private LocalDateTime createdOn;

    @ManyToOne(optional = false)
    @JoinColumn(name = "person_id")
    private Person person;

    public enum TeamType {
        Petros, Hoverla, Blyznytsia, Breskul, Svydovets
    }
}
