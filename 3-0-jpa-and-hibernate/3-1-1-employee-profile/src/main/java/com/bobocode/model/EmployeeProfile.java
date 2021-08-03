package com.bobocode.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "employee_profile")
public class EmployeeProfile {
    @Id
    private Long id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private String department;
}
