package com.rishit.grid07.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(fixedRate = 300000)
    public void sweepPendingNotifications(){
        log.info("CRON Sweeper running - checking for pending notifications...");

        Set<String> keys = redisTemplate.keys("user:*:pending_notifs");
        if ( keys.isEmpty()){
            log.info("CRON Sweeper - no pending notifications found.");
            return;
        }

        for (String key: keys){

            String userId = key.split(":")[1];

            List<String> message = redisTemplate.opsForList().range(key, 0,-1);
            if (message == null || message.isEmpty()) continue;

            int count = message.size();
            String firstBot = message.get(0);

            log.info("Summarized Push Notification: {} and [{}] others interacted with your posts.",firstBot,count - 1);

            redisTemplate.delete(key);
            log.info("Cleared pending notifications for user {}",userId);

        }
    }
}
