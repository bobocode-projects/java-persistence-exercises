package com.bobocode.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class Product {
    private Long id;
    private String name;
    private String producer;
    private BigDecimal price;
    private LocalDate expirationDate;
    private LocalDateTime creationTime;
}
