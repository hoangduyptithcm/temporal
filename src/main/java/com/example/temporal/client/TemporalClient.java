package com.example.temporal.client;

import com.example.temporal.workflow.GreetingWorkflow;
import com.example.temporal.worker.TemporalWorker;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Temporal Client Service
 * 
 * Client là thành phần dùng để:
 * - Khởi tạo workflows
 * - Tương tác với workflows đang chạy
 * - Lấy kết quả từ workflows
 * - Query workflow state
 * - Signal workflows
 * 
 * Client có thể chạy ở:
 * - Cùng process với Worker (như trong ví dụ này)
 * - Process riêng biệt
 * - Máy chủ khác hoàn toàn
 */
@Service
public class TemporalClient {
    
    private static final Logger logger = LoggerFactory.getLogger(TemporalClient.class);
    
    @Autowired
    private WorkflowClient workflowClient;
    
    /**
     * Khởi tạo workflow bất đồng bộ (async)
     * Workflow sẽ chạy trong background, method này return ngay lập tức
     * 
     * @param name Tên người dùng
     * @return Workflow ID để track workflow
     */
    public String startGreetingWorkflow(String name) {
        logger.info("🚀 Starting async greeting workflow for: {}", name);
        
        // Tạo workflow ID duy nhất
        String workflowId = "greeting-workflow-" + UUID.randomUUID().toString();
        
        try {
            // Cấu hình workflow options
            WorkflowOptions options = WorkflowOptions.newBuilder()
                    .setWorkflowId(workflowId)
                    .setTaskQueue(TemporalWorker.getTaskQueue())
                    .setWorkflowExecutionTimeout(Duration.ofMinutes(10))
                    .setWorkflowRunTimeout(Duration.ofMinutes(5))
                    .build();
            
            // Tạo workflow stub
            GreetingWorkflow workflow = workflowClient.newWorkflowStub(
                    GreetingWorkflow.class, options);
            
            // Bắt đầu workflow (async) - không đợi kết quả
            WorkflowClient.start(workflow::greetUser, name);
            
            logger.info("✅ Async workflow started with ID: {}", workflowId);
            return workflowId;
            
        } catch (Exception e) {
            logger.error("❌ Failed to start async workflow for: {}", name, e);
            throw new RuntimeException("Failed to start workflow: " + e.getMessage(), e);
        }
    }
    
    /**
     * Khởi tạo workflow đồng bộ (sync)
     * Method này sẽ đợi cho đến khi workflow hoàn thành
     * 
     * @param name Tên người dùng
     * @return Kết quả của workflow
     */
    public String startGreetingWorkflowSync(String name) {
        logger.info("🔄 Starting sync greeting workflow for: {}", name);
        
        // Tạo workflow ID duy nhất
        String workflowId = "greeting-workflow-sync-" + UUID.randomUUID().toString();
        
        try {
            // Cấu hình workflow options
            WorkflowOptions options = WorkflowOptions.newBuilder()
                    .setWorkflowId(workflowId)
                    .setTaskQueue(TemporalWorker.getTaskQueue())
                    .setWorkflowExecutionTimeout(Duration.ofMinutes(10))
                    .setWorkflowRunTimeout(Duration.ofMinutes(5))
                    .build();
            
            // Tạo workflow stub
            GreetingWorkflow workflow = workflowClient.newWorkflowStub(
                    GreetingWorkflow.class, options);
            
            // Chạy workflow và đợi kết quả (sync)
            logger.info("⏳ Waiting for workflow to complete...");
            String result = workflow.greetUser(name);
            
            logger.info("✅ Sync workflow completed with ID: {}", workflowId);
            return result;
            
        } catch (Exception e) {
            logger.error("❌ Failed to execute sync workflow for: {}", name, e);
            throw new RuntimeException("Failed to execute workflow: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lấy kết quả từ workflow đã chạy
     * 
     * @param workflowId ID của workflow
     * @return Kết quả của workflow
     */
    public String getWorkflowResult(String workflowId) {
        logger.info("📊 Getting result for workflow: {}", workflowId);
        
        try {
            // Tạo workflow stub từ workflow ID
            WorkflowStub workflowStub = workflowClient.newUntypedWorkflowStub(workflowId);
            
            // Lấy kết quả - sẽ block cho đến khi workflow hoàn thành
            String result = workflowStub.getResult(String.class);
            
            logger.info("✅ Retrieved result for workflow: {}", workflowId);
            return result;
            
        } catch (Exception e) {
            logger.error("❌ Failed to get result for workflow: {}", workflowId, e);
            throw new RuntimeException("Failed to get workflow result: " + e.getMessage(), e);
        }
    }
    
    /**
     * Kiểm tra trạng thái workflow
     * 
     * @param workflowId ID của workflow
     * @return Trạng thái workflow
     */
    public String getWorkflowStatus(String workflowId) {
        logger.info("🔍 Checking status for workflow: {}", workflowId);
        
        try {
            WorkflowStub workflowStub = workflowClient.newUntypedWorkflowStub(workflowId);
            
            // Kiểm tra xem workflow đã hoàn thành chưa
            if (workflowStub.getResult(String.class, Duration.ofMillis(100)) != null) {
                return "COMPLETED";
            } else {
                return "RUNNING";
            }
            
        } catch (Exception e) {
            logger.warn("⚠️ Could not determine status for workflow: {}", workflowId);
            return "UNKNOWN";
        }
    }
}
