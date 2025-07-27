package com.example.temporal.worker;

import com.example.temporal.activity.GreetingActivityImpl;
import com.example.temporal.workflow.GreetingWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Temporal Worker Configuration
 * 
 * Worker là service chạy và thực thi các Workflow và Activity.
 * 
 * Nhiệm vụ của Worker:
 * - Lắng nghe task queue để nhận tasks
 * - Thực thi Workflow và Activity
 * - Báo cáo kết quả về Temporal Server
 * - Xử lý retry và error handling
 * 
 * Một ứng dụng có thể có nhiều Worker để:
 * - Scale horizontally
 * - Phân chia workload
 * - Tách biệt các loại task khác nhau
 */
@Component
public class TemporalWorker {
    
    private static final Logger logger = LoggerFactory.getLogger(TemporalWorker.class);
    
    // Task queue name - đây là "địa chỉ" mà Worker sẽ lắng nghe
    public static final String GREETING_TASK_QUEUE = "greeting-task-queue";
    
    @Autowired
    private WorkflowClient workflowClient;
    
    @Autowired
    private GreetingActivityImpl greetingActivity;
    
    private WorkerFactory workerFactory;
    
    /**
     * Khởi tạo và start Worker khi Spring Boot application khởi động
     */
    @PostConstruct
    public void startWorker() {
        logger.info("🏗️ Initializing Temporal Worker...");
        
        try {
            // Tạo WorkerFactory
            workerFactory = WorkerFactory.newInstance(workflowClient);
            
            // Tạo Worker cho greeting task queue
            Worker greetingWorker = workerFactory.newWorker(GREETING_TASK_QUEUE);
            
            // Đăng ký Workflow implementations
            logger.info("📝 Registering Workflow implementations...");
            greetingWorker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
            
            // Đăng ký Activity implementations
            logger.info("⚡ Registering Activity implementations...");
            greetingWorker.registerActivitiesImplementations(greetingActivity);
            
            // Bắt đầu tất cả workers
            logger.info("🚀 Starting Temporal Worker...");
            workerFactory.start();
            
            logger.info("✅ Temporal Worker started successfully!");
            logger.info("👂 Listening on task queue: {}", GREETING_TASK_QUEUE);
            logger.info("📊 Worker status: RUNNING");
            
        } catch (Exception e) {
            logger.error("❌ Failed to start Temporal Worker: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to start Temporal Worker", e);
        }
    }
    
    /**
     * Dừng Worker khi Spring Boot application shutdown
     */
    @PreDestroy
    public void stopWorker() {
        if (workerFactory != null) {
            logger.info("🛑 Stopping Temporal Worker...");
            
            try {
                // Graceful shutdown với timeout
                workerFactory.shutdown();
                logger.info("✅ Temporal Worker stopped successfully!");
                
            } catch (Exception e) {
                logger.error("⚠️ Error during Worker shutdown: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Getter cho task queue name
     * Sử dụng bởi Client để biết gửi task đến queue nào
     * 
     * @return Task queue name
     */
    public static String getTaskQueue() {
        return GREETING_TASK_QUEUE;
    }
    
    /**
     * Kiểm tra trạng thái Worker
     * 
     * @return true nếu Worker đang chạy
     */
    public boolean isRunning() {
        return workerFactory != null && !workerFactory.isShutdown();
    }
}
