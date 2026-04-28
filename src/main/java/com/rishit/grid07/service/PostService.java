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
    private final RedisGuardrailsService redisGuardrailsService;

    public Post createPost(PostRequest req){

        Post post = new Post();

        post.setAuthorId(req.getAuthorId());
        post.setAuthorType(req.getAuthorType());
        post.setContent(req.getContent());
        post.setCreatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    public ResponseEntity<?> addComment(Long postId, CommentRequest req){

        if(!redisGuardrailsService.isDepthAllowed(req.getDepthLevel())){
            return ResponseEntity.status(429).body("Thread too deep - maximum 20 levels allowed");
        }

        if ("BOT".equals(req.getAuthorType())){

            if (!redisGuardrailsService.tryIncrementBotCount(postId)){
                return ResponseEntity.status(429).body("Bot reply limit reached - maximum 100 bot replies per post");
            }

            if (!redisGuardrailsService.tryAcquireCooldown(req.getAuthorId(), req.getTargetUserId())){
                return ResponseEntity.status(429).body("Bot cooldown active - please wait 10 minutes");
            }

            redisGuardrailsService.incrementViralityScore(postId, "BOT_REPLY");
        }
        else {
            redisGuardrailsService.incrementViralityScore(postId,"HUMAN_COMMENT");
        }

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

        redisGuardrailsService.incrementViralityScore(postId, "HUMAN_LIKE");

        return ResponseEntity.ok("Post" + postId + " liked by user " + req.getUserId() + " - virality score updated!");
    }
}
