package com.bobocode.orm;

public interface Session {

    <T> T find(Class<T> type, Object id);
}
