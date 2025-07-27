package com.example.temporal.workflow;

import com.example.temporal.activity.GreetingActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

/**
 * Greeting Workflow Implementation
 * 
 * Đây là implementation của GreetingWorkflow.
 * Workflow này thực hiện các bước:
 * 1. Tạo lời chào
 * 2. Xử lý lời chào
 * 3. Gửi thông báo
 * 4. Trả về kết quả
 * 
 * Lưu ý quan trọng:
 * - Sử dụng Workflow.getLogger() thay vì Logger thông thường
 * - Không được thực hiện side effects trực tiếp
 * - Phải deterministic (có thể replay)
 */
public class GreetingWorkflowImpl implements GreetingWorkflow {
    
    // Sử dụng Workflow logger để đảm bảo deterministic
    private static final Logger logger = Workflow.getLogger(GreetingWorkflowImpl.class);
    
    // Cấu hình Activity options
    private final ActivityOptions defaultActivityOptions = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(2))
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(1))
                    .setMaximumInterval(Duration.ofSeconds(10))
                    .setBackoffCoefficient(2.0)
                    .setMaximumAttempts(3)
                    .build()
            )
            .build();
    
    // Cấu hình riêng cho notification activity (có thể fail)
    private final ActivityOptions notificationActivityOptions = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(1))
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(2))
                    .setMaximumInterval(Duration.ofSeconds(30))
                    .setBackoffCoefficient(2.0)
                    .setMaximumAttempts(5) // Retry nhiều hơn cho notification
                    .build()
            )
            .build();
    
    // Tạo Activity stubs
    private final GreetingActivity greetingActivity = 
        Workflow.newActivityStub(GreetingActivity.class, defaultActivityOptions);
    
    private final GreetingActivity notificationActivity = 
        Workflow.newActivityStub(GreetingActivity.class, notificationActivityOptions);
    
    @Override
    public String greetUser(String name) {
        logger.info("🚀 Starting greeting workflow for user: {}", name);
        
        try {
            // Bước 1: Tạo lời chào
            logger.info("📝 Step 1: Creating greeting for user: {}", name);
            String greeting = greetingActivity.sayHello(name);
            logger.info("✅ Step 1 completed. Greeting: {}", greeting);
            
            // Bước 2: Xử lý lời chào
            logger.info("🔄 Step 2: Processing greeting");
            String processedGreeting = greetingActivity.processGreeting(greeting);
            logger.info("✅ Step 2 completed. Processed greeting ready");
            
            // Bước 3: Gửi thông báo
            logger.info("📧 Step 3: Sending notification to user: {}", name);
            String notificationResult = notificationActivity.sendNotification(
                processedGreeting, 
                name + "@example.com"
            );
            logger.info("✅ Step 3 completed. Notification sent");
            
            // Bước 4: Tạo kết quả cuối cùng
            String finalResult = String.format(
                "🎉 WORKFLOW COMPLETED SUCCESSFULLY! 🎉\n\n" +
                "👤 User: %s\n" +
                "📝 Greeting: %s\n\n" +
                "📧 Notification Status:\n%s\n\n" +
                "⏰ Workflow completed at: %s\n" +
                "🆔 Workflow ID: %s",
                name,
                greeting,
                notificationResult,
                Workflow.currentTimeMillis(), // Sử dụng Workflow time để đảm bảo deterministic
                Workflow.getInfo().getWorkflowId()
            );
            
            logger.info("🏁 Workflow completed successfully for user: {}", name);
            return finalResult;
            
        } catch (Exception e) {
            logger.error("❌ Workflow failed for user: {} with error: {}", name, e.getMessage());
            
            // Tạo error result
            String errorResult = String.format(
                "❌ WORKFLOW FAILED ❌\n\n" +
                "👤 User: %s\n" +
                "🚨 Error: %s\n" +
                "⏰ Failed at: %s\n" +
                "🆔 Workflow ID: %s\n\n" +
                "💡 Please check logs for more details.",
                name,
                e.getMessage(),
                Workflow.currentTimeMillis(),
                Workflow.getInfo().getWorkflowId()
            );
            
            // Re-throw exception để Temporal có thể handle
            throw new RuntimeException("Workflow execution failed: " + e.getMessage(), e);
        }
    }
}
