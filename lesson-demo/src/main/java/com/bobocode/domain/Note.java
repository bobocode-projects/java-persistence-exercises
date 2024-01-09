package com.bobocode.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "notes")
@Setter
@Getter
public class Note {
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "note_id_seq_generator")
    @SequenceGenerator(name = "note_id_seq_generator", sequenceName = "notes_id_seq", allocationSize = 1)
    private Integer id;

    private String title;
    
    private String body;
    
    @CreationTimestamp
    private LocalDateTime createdOn;

    @ManyToOne(optional = false)
    @JoinColumn(name = "person_id")
    private Person person;
}
