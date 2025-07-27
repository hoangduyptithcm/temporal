package com.example.temporal.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Greeting Activity Implementation
 * 
 * Đây là implementation thực tế của GreetingActivity.
 * Trong thực tế, đây là nơi bạn sẽ:
 * - Gọi các API bên ngoài
 * - Thực hiện database operations
 * - Gửi email/SMS
 * - Xử lý file
 * - Các tác vụ có side effects
 */
@Component
public class GreetingActivityImpl implements GreetingActivity {
    
    private static final Logger logger = LoggerFactory.getLogger(GreetingActivityImpl.class);
    private final Random random = new Random();
    
    @Override
    public String sayHello(String name) {
        logger.info("🎯 Executing sayHello activity for: {}", name);
        
        // Mô phỏng thời gian xử lý
        simulateProcessingTime(1000, 2000);
        
        // Tạo lời chào với các biến thể khác nhau
        String[] greetings = {
            "Hello, %s! Welcome to Temporal Learning!",
            "Hi there, %s! Great to see you!",
            "Greetings, %s! Hope you're having a wonderful day!",
            "Hey %s! Ready to learn Temporal?",
            "Welcome %s! Let's explore Temporal together!"
        };
        
        String greeting = String.format(greetings[random.nextInt(greetings.length)], name);
        
        logger.info("✅ Generated greeting: {}", greeting);
        return greeting;
    }
    
    @Override
    public String processGreeting(String greeting) {
        logger.info("🔄 Processing greeting: {}", greeting);
        
        // Mô phỏng thời gian xử lý
        simulateProcessingTime(500, 1500);
        
        // Xử lý và format lời chào
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String processedGreeting = String.format(
            "🌟 %s\n📅 Processed at: %s\n🤖 Processed by: Temporal Activity",
            greeting.toUpperCase(),
            timestamp
        );
        
        logger.info("✅ Processed greeting completed");
        return processedGreeting;
    }
    
    @Override
    public String sendNotification(String message, String recipient) {
        logger.info("📧 Sending notification to: {}", recipient);
        logger.info("📝 Message: {}", message);
        
        // Mô phỏng thời gian gửi thông báo
        simulateProcessingTime(800, 1200);
        
        // Mô phỏng khả năng thất bại (10% chance)
        if (random.nextInt(10) == 0) {
            logger.error("❌ Failed to send notification to: {}", recipient);
            throw new RuntimeException("Notification service temporarily unavailable");
        }
        
        String result = String.format(
            "✅ Notification sent successfully!\n" +
            "📧 Recipient: %s\n" +
            "📅 Sent at: %s\n" +
            "🆔 Message ID: MSG-%d",
            recipient,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            System.currentTimeMillis()
        );
        
        logger.info("✅ Notification sent successfully to: {}", recipient);
        return result;
    }
    
    /**
     * Mô phỏng thời gian xử lý
     * 
     * @param minMs Thời gian tối thiểu (milliseconds)
     * @param maxMs Thời gian tối đa (milliseconds)
     */
    private void simulateProcessingTime(int minMs, int maxMs) {
        try {
            int processingTime = minMs + random.nextInt(maxMs - minMs);
            logger.debug("⏳ Simulating processing time: {}ms", processingTime);
            Thread.sleep(processingTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("⚠️ Processing interrupted");
        }
    }
}
