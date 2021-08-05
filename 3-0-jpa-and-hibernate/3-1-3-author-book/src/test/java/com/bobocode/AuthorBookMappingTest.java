package com.bobocode;

import com.bobocode.model.Author;
import com.bobocode.model.Book;
import com.bobocode.util.EntityManagerUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Session;
import org.junit.jupiter.api.*;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthorBookMappingTest {
    private static EntityManagerUtil emUtil;
    private static EntityManagerFactory entityManagerFactory;

    @BeforeAll
    static void setup() {
        entityManagerFactory = Persistence.createEntityManagerFactory("BookAuthors");
        emUtil = new EntityManagerUtil(entityManagerFactory);
    }

    @AfterAll
    static void destroy() {
        entityManagerFactory.close();
    }

    @Test
    @Order(1)
    @DisplayName("Save a book only")
    void saveBookOnly() {
        Book book = createRandomBook();

        emUtil.performWithinTx(entityManager -> entityManager.persist(book));

        assertThat(book.getId()).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("Saving a book throws an exception when the name is null")
    void saveBookWithoutName() {
        Book book = createRandomBook();
        book.setName(null);

        assertThatExceptionOfType(PersistenceException.class).isThrownBy(() -> emUtil.performWithinTx(entityManager ->
                entityManager.persist(book)));
    }

    @Test
    @Order(3)
    @DisplayName("Saving a book throws an expectation when the ISBN is duplicated")
    void saveBookWithDuplicateIsbn() {
        Book book = createRandomBook();
        emUtil.performWithinTx(entityManager -> entityManager.persist(book));
        Book bookWithDuplicateIsbn = createRandomBook();
        bookWithDuplicateIsbn.setIsbn(book.getIsbn());

        assertThatExceptionOfType(RollbackException.class).isThrownBy(() ->
                emUtil.performWithinTx(entityManager -> entityManager.persist(bookWithDuplicateIsbn)));
    }

    @Test
    @Order(4)
    @DisplayName("Save an author only")
    void saveAuthorOnly() {
        Author author = createRandomAuthor();

        emUtil.performWithinTx(entityManager -> entityManager.persist(author));

        assertThat(author.getId()).isNotNull();
    }

    @Test
    @Order(5)
    @DisplayName("Saving an author throws an expectation when the first name is null")
    void saveAuthorWithoutFirstName() {
        Author authorWithNullFirstName = createRandomAuthor();
        authorWithNullFirstName.setFirstName(null);

        assertThatExceptionOfType(PersistenceException.class).isThrownBy(() ->
                emUtil.performWithinTx(entityManager -> entityManager.persist(authorWithNullFirstName)));
    }

    @Test
    @Order(6)
    @DisplayName("Saving an author throws an expectation when the last name is null")
    void saveAuthorWithoutLastName() {
        Author authorWithNullLastName = createRandomAuthor();
        authorWithNullLastName.setLastName(null);

        assertThatExceptionOfType(PersistenceException.class).isThrownBy(() ->
                emUtil.performWithinTx(entityManager -> entityManager.persist(authorWithNullLastName)));
    }

    @Test
    @Order(7)
    @DisplayName("addBook() stores new book records when the author is stored")
    void addNewBookForExistingAuthor() {
        Author author = createRandomAuthor();
        emUtil.performWithinTx(entityManager -> entityManager.persist(author));

        Book book = createRandomBook();
        emUtil.performWithinTx(entityManager -> {
            Author managedAuthor = entityManager.find(Author.class, author.getId());
            managedAuthor.addBook(book);
        });

        assertThat(book.getId()).isNotNull();
        emUtil.performWithinTx(entityManager -> {
            Book managedBook = entityManager.find(Book.class, book.getId());
            assertThat(managedBook.getAuthors()).contains(author);
        });
    }


    @Test
    @Order(8)
    @DisplayName("Add an author to an existing book")
    void addAuthorToExistingBook() {
        Book book = createRandomBook();
        emUtil.performWithinTx(entityManager -> entityManager.persist(book));

        Author author = createRandomAuthor();
        emUtil.performWithinTx(entityManager -> {
            Book managedBook = entityManager.merge(book);
            author.addBook(managedBook);
            entityManager.persist(author);
        });

        assertThat(author.getId()).isNotNull();
        emUtil.performWithinTx(entityManager -> {
            Author managedAuthor = entityManager.find(Author.class, author.getId());
            assertThat(managedAuthor.getBooks()).contains(book);
        });
    }

    @Test
    @Order(9)
    @DisplayName("Save a new author with several new books")
    void saveNewAuthorWithCoupleNewBooks() {
        Author author = createRandomAuthor();
        List<Book> bookList = Stream.generate(this::createRandomBook).limit(3).collect(Collectors.toList());
        bookList.forEach(author::addBook);

        emUtil.performWithinTx(entityManager -> entityManager.persist(author));

        assertThat(author.getId()).isNotNull();
        assertThat(author.getBooks()).containsExactlyInAnyOrderElementsOf(bookList);
        bookList.forEach(book -> assertThat(book.getAuthors()).contains(author));
        emUtil.performWithinTx(entityManager -> {
            Author managedAuthor = entityManager.find(Author.class, author.getId());
            assertThat(managedAuthor.getBooks()).containsExactlyInAnyOrderElementsOf(bookList);
        });
    }

    @Test
    @Order(10)
    @DisplayName("Remove a book from an author")
    void removeBookFromAuthor() {
        Author author = createRandomAuthor();
        Book book = createRandomBook();
        author.addBook(book);
        emUtil.performWithinTx(entityManager -> entityManager.persist(author));

        emUtil.performWithinTx(entityManager -> {
            Author managedAuthor = entityManager.find(Author.class, author.getId());
            managedAuthor.removeBook(book);
        });

        assertThat(book.getAuthors()).doesNotContain(author);
        emUtil.performWithinTx(entityManager -> {
            Book managedBook = entityManager.find(Book.class, book.getId());
            assertThat(managedBook.getAuthors()).doesNotContain(author);
        });
    }

    @Test
    @Order(11)
    @DisplayName("Remove an author")
    void removeAuthor() {
        Author author = createRandomAuthor();
        Book book = createRandomBook();
        author.addBook(book);
        emUtil.performWithinTx(entityManager -> entityManager.persist(author));

        emUtil.performWithinTx(entityManager -> {
            Author managedAuthor = entityManager.merge(author);
            entityManager.remove(managedAuthor);
        });

        emUtil.performWithinTx(entityManager -> {
            Author foundAccount = entityManager.find(Author.class, author.getId());
            assertThat(foundAccount).isNull();

            Book managedBook = entityManager.find(Book.class, book.getId());
            assertThat(managedBook.getAuthors()).doesNotContain(author);
        });
    }

    @Test
    @Order(12)
    @DisplayName("Remove a book")
    void removeBook() {
        Author author = createRandomAuthor();
        Book book = createRandomBook();
        author.addBook(book);
        emUtil.performWithinTx(entityManager -> entityManager.persist(author));

        emUtil.performWithinTx(entityManager -> {
            Book managedBook = entityManager.merge(book);
            managedBook.getAuthors().forEach(a -> a.removeBook(managedBook));
            entityManager.remove(managedBook);
        });

        emUtil.performWithinTx(entityManager -> {
            Book foundBook = entityManager.find(Book.class, book.getId());
            assertThat(foundBook).isNull();

            Author managedAuthor = entityManager.find(Author.class, author.getId());
            assertThat(managedAuthor.getBooks()).doesNotContain(book);
        });
    }

    @Test
    @Order(13)
    @DisplayName("Setter for field \"author\" is private in Book entity")
    void bookSetAuthorsIsPrivate() throws NoSuchMethodException {
        assertThat(Book.class.getDeclaredMethod("setAuthors", Set.class).getModifiers()).isEqualTo(Modifier.PRIVATE);
    }

    @Test
    @Order(14)
    @DisplayName("Setter for field \"books\" is private in Author entity")
    void authorSetBooksIsPrivate() throws NoSuchMethodException {
        assertThat(Author.class.getDeclaredMethod("setBooks", Set.class).getModifiers()).isEqualTo(Modifier.PRIVATE);
    }

    @Test
    @Order(15)
    @DisplayName("Link table has a correct name")
    void authorBookLinkTableHasCorrectName() throws NoSuchFieldException {
        Field booksField = Author.class.getDeclaredField("books");
        JoinTable joinTable = booksField.getAnnotation(JoinTable.class);

        assertThat(joinTable.name()).isEqualTo("author_book");
    }

    @Test
    @Order(16)
    @DisplayName("Link table has a correct foreign column name to author column")
    void linkTableHasCorrectForeignKeyColumnNameToAuthor() throws NoSuchFieldException {
        Field booksField = Author.class.getDeclaredField("books");
        JoinTable joinTable = booksField.getAnnotation(JoinTable.class);

        assertThat(joinTable.joinColumns()[0].name()).isEqualTo("author_id");
    }

    @Test
    @Order(17)
    @DisplayName("Link table has a correct foreign column name to book column")
    void linkTableHasCorrectForeignKeyColumnNameToBook() throws NoSuchFieldException {
        Field booksField = Author.class.getDeclaredField("books");
        JoinTable joinTable = booksField.getAnnotation(JoinTable.class);

        assertThat(joinTable.inverseJoinColumns()[0].name()).isEqualTo("book_id");
    }

    @Test
    @Order(18)
    @DisplayName("Book ISBN is a natural key")
    void bookIsbnIsNaturalKey() {
        Book book = createRandomBook();
        emUtil.performWithinTx(entityManager -> entityManager.persist(book));

        Book foundBook = emUtil.performReturningWithinTx(entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            return session.bySimpleNaturalId(Book.class)
                    .load(book.getIsbn());
        });

        assertThat(foundBook).isEqualTo(book);
    }

    private Book createRandomBook() {
        Book book = new Book();
        book.setName(RandomStringUtils.randomAlphabetic(20));
        book.setIsbn(RandomStringUtils.randomAlphabetic(30));
        return book;
    }

    private Author createRandomAuthor() {
        Author author = new Author();
        author.setFirstName(RandomStringUtils.randomAlphabetic(20));
        author.setLastName(RandomStringUtils.randomAlphabetic(20));
        return author;
    }
}
