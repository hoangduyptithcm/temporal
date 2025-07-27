package com.example.temporal.controller;

import com.example.temporal.client.TemporalClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Workflow REST Controller
 * 
 * Controller này cung cấp REST API để:
 * - Khởi tạo workflows
 * - Lấy kết quả workflows
 * - Kiểm tra trạng thái workflows
 * - Health check
 * 
 * Đây là entry point cho external systems để tương tác với Temporal workflows.
 */
@RestController
@RequestMapping("/api/workflows")
@CrossOrigin(origins = "*") // Cho phép CORS từ mọi origin (chỉ dùng cho development)
public class WorkflowController {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkflowController.class);
    
    @Autowired
    private TemporalClient temporalClient;
    
    /**
     * Khởi tạo greeting workflow bất đồng bộ
     * 
     * POST /api/workflows/greeting/async
     * Body: {"name": "John Doe"}
     * 
     * @param request Request body chứa tên người dùng
     * @return Response với workflow ID
     */
    @PostMapping("/greeting/async")
    public ResponseEntity<Map<String, Object>> startGreetingWorkflowAsync(
            @RequestBody Map<String, String> request) {
        
        String name = request.get("name");
        if (name == null || name.trim().isEmpty()) {
            logger.warn("⚠️ Received request with empty name");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Name is required and cannot be empty"));
        }
        
        logger.info("📥 Received async workflow request for: {}", name);
        
        try {
            String workflowId = temporalClient.startGreetingWorkflow(name);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("workflowId", workflowId);
            response.put("status", "STARTED");
            response.put("message", "Workflow started successfully");
            response.put("name", name);
            response.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("✅ Async workflow started successfully: {}", workflowId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Failed to start async workflow for: {}", name, e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Failed to start workflow: " + e.getMessage()));
        }
    }
    
    /**
     * Khởi tạo greeting workflow đồng bộ
     * 
     * POST /api/workflows/greeting/sync
     * Body: {"name": "John Doe"}
     * 
     * @param request Request body chứa tên người dùng
     * @return Response với kết quả workflow
     */
    @PostMapping("/greeting/sync")
    public ResponseEntity<Map<String, Object>> startGreetingWorkflowSync(
            @RequestBody Map<String, String> request) {
        
        String name = request.get("name");
        if (name == null || name.trim().isEmpty()) {
            logger.warn("⚠️ Received sync request with empty name");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Name is required and cannot be empty"));
        }
        
        logger.info("📥 Received sync workflow request for: {}", name);
        
        try {
            String result = temporalClient.startGreetingWorkflowSync(name);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            response.put("status", "COMPLETED");
            response.put("name", name);
            response.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("✅ Sync workflow completed successfully for: {}", name);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Failed to execute sync workflow for: {}", name, e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Failed to execute workflow: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy kết quả workflow
     * 
     * GET /api/workflows/result/{workflowId}
     * 
     * @param workflowId ID của workflow
     * @return Response với kết quả workflow
     */
    @GetMapping("/result/{workflowId}")
    public ResponseEntity<Map<String, Object>> getWorkflowResult(
            @PathVariable String workflowId) {
        
        if (workflowId == null || workflowId.trim().isEmpty()) {
            logger.warn("⚠️ Received request with empty workflow ID");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Workflow ID is required"));
        }
        
        logger.info("📊 Received request to get result for workflow: {}", workflowId);
        
        try {
            String result = temporalClient.getWorkflowResult(workflowId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("workflowId", workflowId);
            response.put("result", result);
            response.put("status", "COMPLETED");
            response.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("✅ Retrieved result successfully for workflow: {}", workflowId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Failed to get result for workflow: {}", workflowId, e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Failed to get workflow result: " + e.getMessage()));
        }
    }
    
    /**
     * Kiểm tra trạng thái workflow
     * 
     * GET /api/workflows/status/{workflowId}
     * 
     * @param workflowId ID của workflow
     * @return Response với trạng thái workflow
     */
    @GetMapping("/status/{workflowId}")
    public ResponseEntity<Map<String, Object>> getWorkflowStatus(
            @PathVariable String workflowId) {
        
        if (workflowId == null || workflowId.trim().isEmpty()) {
            logger.warn("⚠️ Received status request with empty workflow ID");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Workflow ID is required"));
        }
        
        logger.info("🔍 Received request to check status for workflow: {}", workflowId);
        
        try {
            String status = temporalClient.getWorkflowStatus(workflowId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("workflowId", workflowId);
            response.put("status", status);
            response.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("✅ Retrieved status successfully for workflow: {} - Status: {}", workflowId, status);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Failed to get status for workflow: {}", workflowId, e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Failed to get workflow status: " + e.getMessage()));
        }
    }
    
    /**
     * Health check endpoint
     * 
     * GET /api/workflows/health
     * 
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", "UP");
        response.put("service", "Temporal Learning API");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("endpoints", Map.of(
            "async_workflow", "POST /api/workflows/greeting/async",
            "sync_workflow", "POST /api/workflows/greeting/sync",
            "get_result", "GET /api/workflows/result/{workflowId}",
            "get_status", "GET /api/workflows/status/{workflowId}",
            "health", "GET /api/workflows/health"
        ));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Tạo error response
     * 
     * @param message Error message
     * @return Error response map
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }
}
