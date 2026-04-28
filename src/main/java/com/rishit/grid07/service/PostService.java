package com.rishit.grid07.service;

import com.rishit.grid07.dto.CommentRequest;
import com.rishit.grid07.dto.LikeRequest;
import com.rishit.grid07.dto.PostRequest;
import com.rishit.grid07.entity.Comment;
import com.rishit.grid07.entity.Post;
import com.rishit.grid07.repository.CommentRepository;
import com.rishit.grid07.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
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

        if (!postRepository.existsById(postId)) {
            return ResponseEntity.badRequest().body("Post not found");
        }

        log.info("Request → botId: {}, targetUserId: {}", req.getAuthorId(), req.getTargetUserId());

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

            Long targetUserId = req.getTargetUserId();
            String message = "Bot " + req.getAuthorType() + " replied to your post";

            if (redisGuardrailsService.hasRecentNotification(targetUserId)){
                redisGuardrailsService.queuePendingNotification(targetUserId,message);
                log.info("Notification queued for user {}: {}",targetUserId,message);
            }
            else {
                log.info("Push Notification Sent to User {}: {}", targetUserId, message);
                redisGuardrailsService.setNotificationCooldown(targetUserId);
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

        log.info("Saving comment for botId: {}", req.getAuthorId());
        return ResponseEntity.ok(commentRepository.save(comment));
    }

    public ResponseEntity<?> likePost(Long postId, LikeRequest req){

        redisGuardrailsService.incrementViralityScore(postId, "HUMAN_LIKE");

        return ResponseEntity.ok("Post" + postId + " liked by user " + req.getUserId() + " - virality score updated!");
    }
}
