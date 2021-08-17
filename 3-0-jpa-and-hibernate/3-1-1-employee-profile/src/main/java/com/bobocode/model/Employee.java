package com.bobocode.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * todo:
 * - configure JPA entity
 * - specify table name: "employee"
 * - configure auto generated identifier
 * - configure not nullable columns: email, firstName, lastName
 *
 * - map unidirectional relation between {@link Employee} and {@link EmployeeProfile} on the child side
 */
@NoArgsConstructor
@Getter
@Setter
public class Employee {
    private Long id;
    private String email;
    private String fistName;
    private String lastName;
}
