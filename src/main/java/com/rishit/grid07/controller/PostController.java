package com.rishit.grid07.controller;

import com.rishit.grid07.dto.CommentRequest;
import com.rishit.grid07.dto.LikeRequest;
import com.rishit.grid07.dto.PostRequest;
import com.rishit.grid07.entity.Post;
import com.rishit.grid07.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody PostRequest req){
        return ResponseEntity.ok(postService.createPost(req));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long postId, @RequestBody CommentRequest req){
        return postService.addComment(postId, req);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable Long postId, @RequestBody LikeRequest req){
        return postService.likePost(postId, req);
    }


}
