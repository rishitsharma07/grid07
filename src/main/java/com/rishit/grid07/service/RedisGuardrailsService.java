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
        String key = "post:" + postId +"virality_score";
        int points = switch (interactionType){
            case "BOT_REPLY"     -> 1;
            case "HUMAN_LIKE"    ->20;
            case "HUMAN_COMMENT" ->50;
            default -> 0;
        };
        redisTemplate.opsForValue().increment(key,points);
    }

    public boolean tryIncrementBotCount(Long postId){
        String key = "post:" + postId + "bot_count";
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count > 100){
            redisTemplate.opsForValue().decrement(key);
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
}
