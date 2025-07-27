package com.example.temporal.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Greeting Workflow Interface
 * 
 * Workflow định nghĩa logic nghiệp vụ chính và điều phối các Activity.
 * 
 * Đặc điểm của Workflow:
 * - Deterministic: Phải cho kết quả giống nhau khi replay
 * - Không được thực hiện side effects trực tiếp
 * - Có thể chạy trong thời gian dài (ngày, tháng, năm)
 * - Có thể pause/resume
 * - Có thể versioning
 */
@WorkflowInterface
public interface GreetingWorkflow {
    
    /**
     * Workflow method chính để chào hỏi người dùng
     * 
     * @param name Tên người dùng
     * @return Kết quả cuối cùng của workflow
     */
    @WorkflowMethod
    String greetUser(String name);
}
