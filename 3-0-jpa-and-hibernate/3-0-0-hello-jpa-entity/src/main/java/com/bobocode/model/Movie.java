package com.bobocode.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * TODO: you're job is to implement mapping for JPA entity {@link Movie}
 * - specify id
 * - configure id as auto-increment column
 * - explicitly specify each column name ("id", "name", "director", and "duration" accordingly)
 * - specify not null constraint for fields {@link Movie#name} and {@link Movie#director}
 */
@NoArgsConstructor
@Getter
@Setter
public class Movie {
    private Long id;

    private String name;

    private String director;

    private Integer durationSeconds;
}