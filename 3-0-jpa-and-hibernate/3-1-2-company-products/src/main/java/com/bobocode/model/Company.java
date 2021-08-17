package com.bobocode.model;

import com.bobocode.util.ExerciseNotCompletedException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * todo:
 * - make setter for field {@link Company#products} private
 * - initialize field {@link Company#products} as new {@link ArrayList}
 * - implement a helper {@link Company#addProduct(Product)} that establishes a relation on both sides
 * - implement a helper {@link Company#removeProduct(Product)} that drops a relation on both sides
 * <p>
 * - configure JPA entity
 * - specify table name: "company"
 * - configure auto generated identifier
 * - configure mandatory column "name" for field {@link Company#name}
 * <p>
 * - configure one to many relationship as mapped on the child side
 */
@NoArgsConstructor
@Getter
@Setter
public class Company {
    private Long id;
    private String name;
    private List<Product> products = new ArrayList<>();

    public void addProduct(Product product) {
        throw new ExerciseNotCompletedException();
    }

    public void removeProduct(Product product) {
        throw new ExerciseNotCompletedException();
    }
}