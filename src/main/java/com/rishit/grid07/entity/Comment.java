package com.rishit.grid07.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private Long postId;

    private Long authorId;

    private String authorType;

    private String content;

    private int depthLevel;

    private LocalDateTime createdAt = LocalDateTime.now();
}
