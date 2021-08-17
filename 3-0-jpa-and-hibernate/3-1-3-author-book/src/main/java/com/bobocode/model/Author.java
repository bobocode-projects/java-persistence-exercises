package com.bobocode.model;

import com.bobocode.util.ExerciseNotCompletedException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
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
@Entity
@Table(name = "author")
public class Author {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Setter(AccessLevel.PRIVATE)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "author_book",
            joinColumns = @JoinColumn(name = "author_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private Set<Book> books = new HashSet<>();

    public void addBook(Book book) {
        books.add(book);
        book.getAuthors().add(this);
    }

    public void removeBook(Book book) {
        books.remove(book);
        book.getAuthors().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Author)) return false;

        Author author = (Author) o;

        return Objects.equals(id, author.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}