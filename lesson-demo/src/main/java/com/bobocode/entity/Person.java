package com.bobocode.entity;


import com.bobocode.annotation.Column;
import com.bobocode.annotation.Id;
import com.bobocode.annotation.Table;
import lombok.Data;

@Data
@Table("persons")
public class Person {
    @Id
    private Integer id;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;
}
