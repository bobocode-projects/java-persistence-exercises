package com.bobocode.entity;


import com.bobocode.annotation.Column;
import com.bobocode.annotation.Id;
import com.bobocode.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("notes")
public class Note {
    @Id
    private Integer id;

    private String body;

    @Column("person_id")
    private Integer personId;

    @Column("created_at")
    private LocalDateTime createdAt;
}
