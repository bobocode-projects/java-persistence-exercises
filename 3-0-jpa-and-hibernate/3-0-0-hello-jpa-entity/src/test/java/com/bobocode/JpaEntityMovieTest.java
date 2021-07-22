package com.bobocode;

import com.bobocode.model.Movie;
import org.junit.jupiter.api.*;

import javax.persistence.*;
import java.lang.reflect.Field;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JpaEntityMovieTest {

    @Test
    @Order(1)
    @DisplayName("The class is marked as JPA entity")
    public void classIsMarkedAsJpaEntity() {
        assertThat(Movie.class.getAnnotation(Entity.class)).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("@Table name is specified")
    public void tableIsSpecified() {
        Table table = Movie.class.getAnnotation(Table.class);

        assertThat(table).isNotNull();
        assertThat(table.name()).isEqualTo("movie");
    }

    @Test
    @Order(3)
    @DisplayName("The entity has an ID")
    public void entityHasId() throws NoSuchFieldException {
        Field idField = Movie.class.getDeclaredField("id");

        assertThat(idField.getAnnotation(Id.class)).isNotNull();
    }

    @Test
    @Order(4)
    @DisplayName("Id field type is Long")
    public void idTypeIsLong() throws NoSuchFieldException {
        Field idField = Movie.class.getDeclaredField("id");
        String idTypeName = idField.getType().getName();
        String longName = Long.class.getName();

        assertThat(idTypeName).isEqualTo(longName);
    }

    @Test
    @Order(5)
    @DisplayName("Id field is marked as generated value")
    public void idIsGenerated() throws NoSuchFieldException {
        Field idField = Movie.class.getDeclaredField("id");

        assertThat(idField.getAnnotation(GeneratedValue.class)).isNotNull();
    }

    @Test
    @Order(6)
    @DisplayName("Id generation strategy is Identity")
    public void idGenerationStrategyIsIdentity() throws NoSuchFieldException {
        Field idField = Movie.class.getDeclaredField("id");
        GeneratedValue generatedValue = idField.getAnnotation(GeneratedValue.class);

        assertThat(generatedValue.strategy()).isEqualTo(GenerationType.IDENTITY);
    }

    @Test
    @Order(7)
    @DisplayName("Name field is marked as Column")
    public void movieNameIsMarkedAsColumn() throws NoSuchFieldException {
        Field nameField = Movie.class.getDeclaredField("name");

        assertThat(nameField.getAnnotation(Column.class)).isNotNull();
    }

    @Test
    @Order(8)
    @DisplayName("Name column name is specified explicitly")
    public void movieNameColumnIsSpecified() throws NoSuchFieldException {
        Field nameField = Movie.class.getDeclaredField("name");
        Column column = nameField.getAnnotation(Column.class);

        assertThat(column.name()).isEqualTo("name");
    }

    @Test
    @Order(9)
    @DisplayName("Name column is not nullable")
    public void movieNameColumnIsNotNull() throws NoSuchFieldException {
        Field nameField = Movie.class.getDeclaredField("name");
        Column column = nameField.getAnnotation(Column.class);

        assertThat(column.nullable()).isEqualTo(false);
    }

    @Test
    @Order(10)
    @DisplayName("Director field is marked as column")
    public void directorIsMarkedAsColumn() throws NoSuchFieldException {
        Field declaredField = Movie.class.getDeclaredField("director");

        assertThat(declaredField.getAnnotation(Column.class)).isNotNull();
    }

    @Test
    @Order(11)
    @DisplayName("Director column name is specified explicitly")
    public void directorColumnIsSpecified() throws NoSuchFieldException {
        Field declaredField = Movie.class.getDeclaredField("director");
        Column column = declaredField.getAnnotation(Column.class);

        assertThat(column.name()).isEqualTo("director");
    }

    @Test
    @Order(12)
    @DisplayName("Director column is not nullable")
    public void directorColumnIsNotNull() throws NoSuchFieldException {
        Field declaredField = Movie.class.getDeclaredField("director");
        Column column = declaredField.getAnnotation(Column.class);

        assertThat(column.nullable()).isEqualTo(false);
    }

    @Test
    @Order(13)
    @DisplayName("Duration field is marked as column")
    public void durationIsMarkedAsColumn() throws NoSuchFieldException {
        Field declaredField = Movie.class.getDeclaredField("durationSeconds");

        assertThat(declaredField.getAnnotation(Column.class)).isNotNull();
    }

    @Test
    @Order(14)
    @DisplayName("Duration column name is specified explicitly")
    public void durationColumnIsSpecified() throws NoSuchFieldException {
        Field declaredField = Movie.class.getDeclaredField("durationSeconds");
        Column column = declaredField.getAnnotation(Column.class);

        assertThat(column.name()).isEqualTo("duration");
    }

    @Test
    @Order(15)
    @DisplayName("Duration column is not nullable")
    public void durationColumnIsNullable() throws NoSuchFieldException {
        Field declaredField = Movie.class.getDeclaredField("durationSeconds");
        Column column = declaredField.getAnnotation(Column.class);

        assertThat(column.nullable()).isEqualTo(true);
    }
}
