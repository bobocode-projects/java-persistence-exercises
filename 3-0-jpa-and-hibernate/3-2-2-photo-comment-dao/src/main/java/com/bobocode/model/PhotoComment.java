package com.bobocode.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * todo:
 * - implement no argument constructor
 * - implement getters and setters
 * - implement equals and hashCode based on identifier field
 *
 * - configure JPA entity
 * - specify table name: "photo_comment"
 * - configure auto generated identifier
 * - configure not nullable column: text
 *
 * - map relation between Photo and PhotoComment using foreign_key column: "photo_id"
 * - configure relation as mandatory (not optional)
 */
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "photo_comment")
public class PhotoComment {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @ManyToOne(optional = false)
    @JoinColumn(name = "photo_id")
    private Photo photo;

    public PhotoComment(String text, Photo photo) {
        this.text = text;
        this.photo = photo;
        this.createdOn = LocalDateTime.now();
    }
}

