package com.rishit.grid07;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Disabled("Manual test - requires running server and Redis")
class RaceConditionTest {

    private static final String URL = "http://localhost:8080/api/posts/1/comments";
    private static final int THREAD_COUNT = 200;

    @Test
    void testBotRaceCondition() throws InterruptedException{
        HttpClient httpClient = HttpClient.newHttpClient();
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        for (int i=0; i < THREAD_COUNT; i++){
            final int botId = i;
            executorService.submit(()-> {
                try {
                    String body = String.format("""
                            {
                                "authorId": %d,
                                "authorType": "BOT",
                                "content": "Race condition test bot %d",
                                "depthLevel": 1,
                                "targetUserId": %d
                            }
                            """, botId, botId, botId);

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(URL))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build();

                    HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());

                    if(response.statusCode() == 200){
                        successCount.incrementAndGet();
                    }else {
                        rejectedCount.incrementAndGet();
                        System.out.println("ERROR (" + response.statusCode() + "): " + response.body());
                    }
                } catch (Exception e) {
                    rejectedCount.incrementAndGet();
                    System.out.println("EXCEPTION: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        System.out.println("=================================");
        System.out.println("Total requests:  " + THREAD_COUNT);
        System.out.println("Successful:      " + successCount.get());
        System.out.println("Rejected:        " + rejectedCount.get());
        System.out.println("=================================");

        assert successCount.get() == 100 : "Race Condition detected! " + successCount.get() + "bots got through";
        System.out.println("Race condition test passed - Redis atomic ops working correctly!");

    }
}
