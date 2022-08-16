package com.bobocode.orm;

import com.bobocode.annotation.Column;
import com.bobocode.annotation.Id;
import com.bobocode.annotation.Table;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Arrays;

@RequiredArgsConstructor
public class SessionImpl implements Session {
    private static String SELECT_BY_ID_QUERY = "select * from %s where %s = ?";
    private final DataSource dataSource;

    @Override
    @SneakyThrows
    public <T> T find(Class<T> entityType, Object id) {
        try (var connection = dataSource.getConnection()) {
            var selectQuery = buildSelectQuery(entityType);
            try (var selectStatement = connection.prepareStatement(selectQuery)) {
                selectStatement.setObject(1, id);
                System.out.println("SQL: " + selectStatement);
                var resultSet = selectStatement.executeQuery();
                resultSet.next();
                return createEntityFromResultSet(entityType, resultSet);
            }
        }
    }

    private <T> String buildSelectQuery(Class<T> type) {
        var tableName = type.getAnnotation(Table.class).value();
        var idField = Arrays.stream(type.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findAny().orElseThrow();
        var idColumnName = resolveColumnName(idField);
        return String.format(SELECT_BY_ID_QUERY, tableName, idColumnName);
    }

    private String resolveColumnName(Field field) {
        return field.isAnnotationPresent(Column.class)
                ? field.getAnnotation(Column.class).value()
                : field.getName();
    }

    @SneakyThrows
    private <T> T createEntityFromResultSet(Class<T> entityType, ResultSet resultSet) {
        var constructor = entityType.getConstructor();
        var entity = constructor.newInstance();
        for (var field : entityType.getDeclaredFields()) {
            var columnName = resolveColumnName(field);
            var fieldValue = resultSet.getObject(columnName);
            if (fieldValue instanceof Timestamp timestamp) {
                fieldValue = timestamp.toLocalDateTime();
            }
            field.setAccessible(true);
            field.set(entity, fieldValue);
        }
        return entity;
    }
}
