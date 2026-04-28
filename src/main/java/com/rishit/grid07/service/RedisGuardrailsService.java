package com.rishit.grid07.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisGuardrailsService {

    private final RedisTemplate<String, String> redisTemplate;

    public void incrementViralityScore(Long postId, String interactionType){
        String key = "post:" + postId + ":virality_score";
        int points = switch (interactionType){
            case "BOT_REPLY"     -> 1;
            case "HUMAN_LIKE"    ->20;
            case "HUMAN_COMMENT" ->50;
            default -> 0;
        };
        redisTemplate.opsForValue().increment(key,points);
    }

    public boolean tryIncrementBotCount(Long postId){
        String key = "post:" + postId + ":bot_count";
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null || count > 100){
            return false;
        }
        return true;
    }

    public boolean isDepthAllowed(int depthLevel){
        return depthLevel <= 20;
    }

    public boolean tryAcquireCooldown(Long botId, Long humanId){
        String key = "cooldown:bot_" + botId + ":human_" + humanId;
        Boolean wasAbsent = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofMinutes(10));
        return  Boolean.TRUE.equals(wasAbsent);
    }

    public boolean hasRecentNotification(Long userId){
        String key = "notif_cooldown:user_" + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void queuePendingNotification(Long userId, String message){
        String key = "user:" + userId + ":pending_notifs";
        redisTemplate.opsForList().rightPush(key,message);
    }

    public void setNotificationCooldown(Long userId){
        String key = "notif_cooldown:user_" + userId;
        redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(15));
    }
}
