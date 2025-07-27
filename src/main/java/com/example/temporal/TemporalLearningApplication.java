package com.example.temporal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Application class cho Temporal Learning Project
 * 
 * Project này được thiết kế để học Temporal với Java Spring Boot
 * từ cơ bản đến nâng cao cho người mới bắt đầu.
 * 
 * @author Temporal Learning Team
 */
@SpringBootApplication
public class TemporalLearningApplication {

    private static final Logger logger = LoggerFactory.getLogger(TemporalLearningApplication.class);

    public static void main(String[] args) {
        logger.info("🚀 Starting Temporal Learning Application...");
        
        SpringApplication.run(TemporalLearningApplication.class, args);
        
        logger.info("✅ Temporal Learning Application started successfully!");
        logger.info("📖 Xem tài liệu hướng dẫn tại: README.md");
        logger.info("🌐 API Documentation: http://localhost:8081/api/workflows/health");
        logger.info("🔍 Temporal Web UI: http://localhost:8080");
    }
}
