package com.bobocode;

import com.bobocode.model.Author;
import com.bobocode.model.Book;
import com.bobocode.util.EntityManagerUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.JoinTable;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

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
    void testSaveBookOnly() {
        Book book = createRandomBook();

        emUtil.performWithinTx(entityManager -> entityManager.persist(book));

        assertThat(book.getId(), notNullValue());
    }

    private Book createRandomBook() {
        Book book = new Book();
        book.setName(RandomStringUtils.randomAlphabetic(20));
        book.setIsbn(RandomStringUtils.randomAlphabetic(30));
        return book;
    }

    @Test
    void testSaveBookWithoutName() {
        Book book = createRandomBook();
        book.setName(null);
        try {
            emUtil.performWithinTx(entityManager -> entityManager.persist(book));
            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e.getClass(), equalTo(PersistenceException.class));
        }
    }

    @Test
    void testSaveBookWithDuplicateIsbn() {
        Book book = createRandomBook();
        emUtil.performWithinTx(entityManager -> entityManager.persist(book));
        Book bookWithDuplicateIsbn = createRandomBook();
        bookWithDuplicateIsbn.setIsbn(book.getIsbn());

        try {
            emUtil.performWithinTx(entityManager -> entityManager.persist(bookWithDuplicateIsbn));
            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e.getClass(), equalTo(RollbackException.class));
        }
    }

    @Test
    void testSaveAuthorOnly() {
        Author author = createRandomAuthor();

        emUtil.performWithinTx(entityManager -> entityManager.persist(author));

        assertThat(author.getId(), notNullValue());
    }

    private Author createRandomAuthor() {
        Author author = new Author();
        author.setFirstName(RandomStringUtils.randomAlphabetic(20));
        author.setLastName(RandomStringUtils.randomAlphabetic(20));
        return author;
    }

    @Test
    void testSaveAuthorWithoutFirstName() {
        Author authorWithNullFirstName = createRandomAuthor();
        authorWithNullFirstName.setFirstName(null);
        try {
            emUtil.performWithinTx(entityManager -> entityManager.persist(authorWithNullFirstName));
            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e.getClass(), equalTo(PersistenceException.class));
        }
    }

    @Test
    void testSaveAuthorWithoutLastName() {
        Author authorWithNullLastName = createRandomAuthor();
        authorWithNullLastName.setLastName(null);
        try {
            emUtil.performWithinTx(entityManager -> entityManager.persist(authorWithNullLastName));
            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e.getClass(), equalTo(PersistenceException.class));
        }
    }

    @Test
    void testAddNewBookForExistingAuthor() {
        Author author = createRandomAuthor();
        emUtil.performWithinTx(entityManager -> entityManager.persist(author));

        Book book = createRandomBook();
        emUtil.performWithinTx(entityManager -> {
            Author managedAuthor = entityManager.find(Author.class, author.getId());
            managedAuthor.addBook(book);
        });

        assertThat(book.getId(), notNullValue());
        emUtil.performWithinTx(entityManager -> {
            Book managedBook = entityManager.find(Book.class, book.getId());
            assertThat(managedBook.getAuthors(), hasItem(author));
        });
    }


    @Test
    void testAddAuthorToExistingBook() {
        Book book = createRandomBook();
        emUtil.performWithinTx(entityManager -> entityManager.persist(book));

        Author author = createRandomAuthor();
        emUtil.performWithinTx(entityManager -> {
            Book managedBook = entityManager.merge(book);
            author.addBook(managedBook);
            entityManager.persist(author);
        });

        assertThat(author.getId(), notNullValue());
        emUtil.performWithinTx(entityManager -> {
            Author managedAuthor = entityManager.find(Author.class, author.getId());
            assertThat(managedAuthor.getBooks(), hasItem(book));
        });
    }

    @Test
    void testSaveNewAuthorWithCoupleNewBooks() {
        Author author = createRandomAuthor();
        List<Book> bookList = Stream.generate(this::createRandomBook).limit(3).collect(Collectors.toList());
        bookList.forEach(author::addBook);

        emUtil.performWithinTx(entityManager -> entityManager.persist(author));

        assertThat(author.getId(), notNullValue());
        assertThat(author.getBooks(), containsInAnyOrder(bookList.toArray()));
        bookList.forEach(book -> assertThat(book.getAuthors(), hasItem(author)));
        emUtil.performWithinTx(entityManager -> {
            Author managedAuthor = entityManager.find(Author.class, author.getId());
            assertThat(managedAuthor.getBooks(), containsInAnyOrder(bookList.toArray()));
        });
    }

    @Test
    void testRemoveBookFromAuthor() {
        Author author = createRandomAuthor();
        Book book = createRandomBook();
        author.addBook(book);
        emUtil.performWithinTx(entityManager -> entityManager.persist(author));

        emUtil.performWithinTx(entityManager -> {
            Author managedAuthor = entityManager.find(Author.class, author.getId());
            managedAuthor.removeBook(book);
        });

        assertThat(book.getAuthors(), not(hasItem(author)));
        emUtil.performWithinTx(entityManager -> {
            Book managedBook = entityManager.find(Book.class, book.getId());
            assertThat(managedBook.getAuthors(), not(hasItem(author)));
        });
    }

    @Test
    void testRemoveAuthor() {
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
            assertThat(foundAccount, nullValue());

            Book managedBook = entityManager.find(Book.class, book.getId());
            assertThat(managedBook.getAuthors(), not(hasItem(author)));
        });
    }

    @Test
    void testRemoveBook() {
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
            assertThat(foundBook, nullValue());

            Author managedAuthor = entityManager.find(Author.class, author.getId());
            assertThat(managedAuthor.getBooks(), not(hasItem(book)));
        });
    }

    @Test
    void testBookSetAuthorsIsPrivate() throws NoSuchMethodException {
        assertThat(Book.class.getDeclaredMethod("setAuthors", Set.class).getModifiers(), equalTo(Modifier.PRIVATE));
    }

    @Test
    void testAuthorSetBooksIsPrivate() throws NoSuchMethodException {
        assertThat(Author.class.getDeclaredMethod("setBooks", Set.class).getModifiers(), equalTo(Modifier.PRIVATE));
    }

    @Test
    void testAuthorBookLinkTableHasCorrectName() throws NoSuchFieldException {
        Field booksField = Author.class.getDeclaredField("books");
        JoinTable joinTable = booksField.getAnnotation(JoinTable.class);

        assertThat(joinTable.name(), equalTo("author_book"));
    }

    @Test
    void testLinkTableHasCorrectForeignKeyColumnNameToAuthor() throws NoSuchFieldException {
        Field booksField = Author.class.getDeclaredField("books");
        JoinTable joinTable = booksField.getAnnotation(JoinTable.class);

        assertThat(joinTable.joinColumns()[0].name(), equalTo("author_id"));
    }

    @Test
    void testLinkTableHasCorrectForeignKeyColumnNameToBook() throws NoSuchFieldException {
        Field booksField = Author.class.getDeclaredField("books");
        JoinTable joinTable = booksField.getAnnotation(JoinTable.class);

        assertThat(joinTable.inverseJoinColumns()[0].name(), equalTo("book_id"));
    }

    @Test
    void testBookIsbnIsNaturalKey() {
        Book book = createRandomBook();
        emUtil.performWithinTx(entityManager -> entityManager.persist(book));

        Book foundBook = emUtil.performReturningWithinTx(entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            return session.bySimpleNaturalId(Book.class)
                    .load(book.getIsbn());
        });

        assertThat(foundBook, equalTo(book));
    }
}
