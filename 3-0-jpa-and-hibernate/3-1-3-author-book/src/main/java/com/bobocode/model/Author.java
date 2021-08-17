package com.bobocode.model;

import com.bobocode.util.ExerciseNotCompletedException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import java.util.HashSet;
import java.util.Set;

/**
 * todo:
 * - implement hashCode() that return constant value 31
 * - make setter for field {@link Author#books} private
 * - initialize field {@link Author#books} as new {@link HashSet}
 * - implement a helper {@link Author#addBook(Book)} that establishes a relation on both sides
 * - implement a helper {@link Author#removeBook(Book)} that drops a relation on both sides
 * <p>
 * - configure JPA entity
 * - specify table name: "author"
 * - configure auto generated identifier
 * - configure mandatory column "first_name" for field {@link Author#firstName}
 * - configure mandatory column "last_name" for field {@link Author#lastName}
 * <p>
 * - configure many-to-many relation between {@link Author} and {@link Book}
 * - configure cascade operations for this relations {@link CascadeType#PERSIST} and {@link CascadeType#MERGE}
 * - configure link (join) table "author_book"
 * - configure foreign key column "book_id" references book table
 * - configure foreign key column "author_id" references author table
 */
@NoArgsConstructor
@Getter
@Setter
public class Author {
    private Long id;
    private String firstName;
    private String lastName;
    private Set<Book> books;

    public void addBook(Book book) {
        throw new ExerciseNotCompletedException();
    }

    public void removeBook(Book book) {
        throw new ExerciseNotCompletedException();
    }
}
