package com.bobocode.entity;


import com.bobocode.annotation.Column;
import com.bobocode.annotation.Id;
import com.bobocode.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("products")
public class Product {
    @Id
    private Long id;
    
    private String name;
    
    private BigDecimal price;
    
    @Column("created_at")
    private LocalDateTime createdAt;
}
