package com.rishit.grid07.service;

import com.rishit.grid07.dto.CommentRequest;
import com.rishit.grid07.dto.LikeRequest;
import com.rishit.grid07.dto.PostRequest;
import com.rishit.grid07.entity.Comment;
import com.rishit.grid07.entity.Post;
import com.rishit.grid07.repository.CommentRepository;
import com.rishit.grid07.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public Post createPost(PostRequest req){

        Post post = new Post();

        post.setAuthorId(req.getAuthorId());
        post.setAuthorType(req.getAuthorType());
        post.setContent(req.getContent());
        post.setCreatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    public ResponseEntity<?> addComment(Long postId, CommentRequest req){

        Comment comment = new Comment();

        comment.setPostId(postId);
        comment.setAuthorId(req.getAuthorId());
        comment.setAuthorType(req.getAuthorType());
        comment.setContent(req.getContent());
        comment.setDepthLevel(req.getDepthLevel());
        comment.setCreatedAt(LocalDateTime.now());

        return ResponseEntity.ok(commentRepository.save(comment));
    }

    public ResponseEntity<?> likePost(Long postId, LikeRequest req){
        //Phase 2 will add Redis virality score here
        return ResponseEntity.ok("Post" + postId + " liked by user " + req.getUserId());
    }
}
