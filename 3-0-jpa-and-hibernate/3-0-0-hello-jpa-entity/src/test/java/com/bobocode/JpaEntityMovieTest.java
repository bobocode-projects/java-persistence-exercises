package com.bobocode;

import com.bobocode.model.Movie;
import org.junit.jupiter.api.Test;

import javax.persistence.*;
import java.lang.reflect.Field;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class JpaEntityMovieTest {

    @Test
    public void testClassIsMarkedAsJpaEntity() {
        assertThat(Movie.class.getAnnotation(Entity.class), notNullValue());
    }

    @Test
    public void testTableIsSpecified() {
        Table table = Movie.class.getAnnotation(Table.class);

        assertThat(table, notNullValue());
        assertThat(table.name(), is("movie"));
    }

    @Test
    public void testEntityHasId() throws NoSuchFieldException {
        Field idField = Movie.class.getDeclaredField("id");

        assertThat(idField.getAnnotation(Id.class), notNullValue());
    }

    @Test
    public void testIdTypeIsLong() throws NoSuchFieldException {
        Field idField = Movie.class.getDeclaredField("id");

        assertThat(idField.getType().getName(), is(Long.class.getName()));
    }

    @Test
    public void testIdIsGenerated() throws NoSuchFieldException {
        Field idField = Movie.class.getDeclaredField("id");

        assertThat(idField.getAnnotation(GeneratedValue.class), notNullValue());
    }

    @Test
    public void testIdGenerationStrategyIsIdentity() throws NoSuchFieldException {
        Field idField = Movie.class.getDeclaredField("id");
        GeneratedValue generatedValue = idField.getAnnotation(GeneratedValue.class);

        assertThat(generatedValue.strategy(), is(GenerationType.IDENTITY));
    }

    @Test
    public void testMovieNameIsMarkedAsColumn() throws NoSuchFieldException {
        Field nameField = Movie.class.getDeclaredField("name");

        assertThat(nameField.getAnnotation(Column.class), notNullValue());
    }

    @Test
    public void testMovieNameColumnIsSpecified() throws NoSuchFieldException {
        Field nameField = Movie.class.getDeclaredField("name");
        Column column = nameField.getAnnotation(Column.class);

        assertThat(column.name(), is("name"));
    }

    @Test
    public void testMovieNameColumnIsNotNull() throws NoSuchFieldException {
        Field nameField = Movie.class.getDeclaredField("name");
        Column column = nameField.getAnnotation(Column.class);

        assertThat(column.nullable(), is(false));
    }

    @Test
    public void testDirectorIsMarkedAsColumn() throws NoSuchFieldException {
        Field declaredField = Movie.class.getDeclaredField("director");

        assertThat(declaredField.getAnnotation(Column.class), notNullValue());
    }

    @Test
    public void testDirectorColumnIsSpecified() throws NoSuchFieldException {
        Field declaredField = Movie.class.getDeclaredField("director");
        Column column = declaredField.getAnnotation(Column.class);

        assertThat(column.name(), is("director"));
    }

    @Test
    public void testDirectorColumnIsNotNull() throws NoSuchFieldException {
        Field declaredField = Movie.class.getDeclaredField("director");
        Column column = declaredField.getAnnotation(Column.class);

        assertThat(column.nullable(), is(false));
    }

    @Test
    public void testDurationIsMarkedAsColumn() throws NoSuchFieldException {
        Field declaredField = Movie.class.getDeclaredField("durationSeconds");

        assertThat(declaredField.getAnnotation(Column.class), notNullValue());
    }

    @Test
    public void testDurationColumnIsSpecified() throws NoSuchFieldException {
        Field declaredField = Movie.class.getDeclaredField("durationSeconds");
        Column column = declaredField.getAnnotation(Column.class);

        assertThat(column.name(), is("duration"));
    }

    @Test
    public void testDurationColumnIsNullable() throws NoSuchFieldException {
        Field declaredField = Movie.class.getDeclaredField("durationSeconds");
        Column column = declaredField.getAnnotation(Column.class);

        assertThat(column.nullable(), is(true));
    }

}
