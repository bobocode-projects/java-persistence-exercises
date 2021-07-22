package com.bobocode.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "song")
public class Song {
    @Id
    @GeneratedValue
    private Long id;
    @Column
    private String name;
    @Column
    private String author;
}
