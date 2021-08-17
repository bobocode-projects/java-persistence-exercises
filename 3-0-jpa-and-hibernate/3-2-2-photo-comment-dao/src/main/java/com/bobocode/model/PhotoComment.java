package com.bobocode.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * todo:
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
@Getter
@Setter
public class PhotoComment {
    private Long id;
    private String text;
    private LocalDateTime createdOn;
    private Photo photo;
}
