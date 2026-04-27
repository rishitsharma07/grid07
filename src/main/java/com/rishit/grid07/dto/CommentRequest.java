package com.rishit.grid07.dto;

import lombok.Data;

@Data
public class CommentRequest {

    private Long authorId;
    private String authorType;
    private String content;
    private int depthLevel;
    private Long targetUserId; //the human who owns the post
}
